package com.example.cache.aerospike.services;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Record;
import com.example.cache.aerospike.configuration.AerospikeClientWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service("cacheService")
public class AerospikeCacheServiceImpl implements IAerospikeCacheService {

    @Value("${cache.aerospike.ttl}")
    private  int    cacheTtl;

    @Value("${cache.aerospike.enabled}")
    private boolean isCacheOn;

    @Autowired
    private AerospikeClientWrapper aeroWrapper;

    private static final Logger LOG = LoggerFactory.getLogger(AerospikeCacheServiceImpl.class);

    @Autowired
    public AerospikeCacheServiceImpl(AerospikeClientWrapper aeroWrapper) {
        if (isCacheOn) {
            this.aeroWrapper = aeroWrapper;
        }
    }

    @Override
    public Object get(String broker,String set, String key, TypeReference typeReference) {
        try {
            if (isCacheOn) {
                Record record = getRecord(set, key);
                if (null != record) {
                    Map<String, Object> map = new HashMap<>();
                    Map<String, Object> bins = record.bins;
                    if (bins != null && bins.size() > 0) {
                        Iterator itr = bins.entrySet().iterator();
                        while (itr.hasNext()) {
                            Map.Entry pairs = (Map.Entry) itr.next();
                            String binName = (String) pairs.getKey();
                            String value = (String) pairs.getValue();
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode jsonObject = mapper.readValue(value, JsonNode.class);
                            Object object = mapper.convertValue(jsonObject, typeReference);
                            map.put(binName, object);
                        }
                    }
                    return map;
                }
            }
        }
        catch (Exception e) {
            LOG.debug(e.getMessage());
        }
        return null;
    }

    private Record getRecord(String set, String key) {
        try {
            return aeroWrapper.read(set, key);
        }
        catch (Exception e) {
            LOG.error("Unable to get content from aerospike", e);
        } return null;
    }

    @Override
    public void put(String broker,String set, String key, String binName, Object value) {
        try {
            if (isCacheOn) {
                ObjectMapper mapper = new ObjectMapper();

                aeroWrapper.cache(set, cacheTtl, key, binName, mapper.valueToTree(value).toString());
            }else
                LOG.info("Aerospike cache is off");
        }
        catch (Exception e) {
            LOG.error("Encountered exception in putting record in cache - {}",e);
        }

    }

    @Override
    public void put(String broker,String set, int ttl, String key, String binName, Object value) {
        try {
            if (isCacheOn)
                aeroWrapper.cache(set, ttl, key, binName, value);
        }
        catch (Exception e) {
            LOG.debug(e.getMessage());
        }
    }

    @Override
    public void put(String broker,String set, int ttl, String key, String binName, String value) {
        try {
            if (isCacheOn)
                aeroWrapper.cache(set, ttl, key, binName, value);
        }
        catch (Exception e) {
            LOG.debug(e.getMessage());
        }
    }

    @Override
    public void put(String broker,String set, String key, Map<String, ? extends Object> dataMap) {
        try {
            if (isCacheOn)
                aeroWrapper.write(set, key, dataMap);
        }
        catch (Exception e) {
            LOG.debug(e.getMessage());
        }
    }

    @Override
    public boolean put(String broker,String set, String key, Map<String, ? extends Object> dataMap, int ttl) {
        try {
            if (isCacheOn) {
                aeroWrapper.write(set, key, dataMap, ttl);
                return true;
            }
            return false;
        }
        catch (Exception e) {
            LOG.debug(e.getMessage());
            return false;
        }
    }

    @Override
    public Object get(String broker,String set, String key, String bin) {
        try {
            if (isCacheOn) {
                Record record = aeroWrapper.read(set, key);
                if (record != null) {
                    return record.getValue(bin);
                }
            }
        }
        catch (Exception e) {
            LOG.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean delete(String broker,String set, String key) {
        try {
            if (isCacheOn) {
                return delete(set, key);
            }
        }
        catch (Exception e) {
            LOG.debug(e.getMessage());
        }
        return false;
    }

    private boolean delete(String set, String key) {
        try {
            aeroWrapper.delete(set, key);
            return true;
        }
        catch (AerospikeException e) {
            LOG.error("Error: ", e);
            return false;
        }
    }

    @Override
    public Object get(String broker,String set, String key, String bin, TypeReference typeReference) {
        try {
            LOG.info("Aerospike cache get method");

            if (isCacheOn) {
                Record record = aeroWrapper.read(set, key, bin);
                if (null != record && null != record.getValue(bin)) {
                    final Record finalRecord = record;
                    String string = finalRecord.getValue(bin).toString();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonObject = mapper.readValue(string, JsonNode.class);
                    return mapper.convertValue(jsonObject, typeReference);
                }
            }else
                LOG.info("Aerospike cache is off");
        }
        catch (Exception e) {
            LOG.error("Encountred exception while getting data froo cache - {}",e);
        }
        return null;
    }
}

