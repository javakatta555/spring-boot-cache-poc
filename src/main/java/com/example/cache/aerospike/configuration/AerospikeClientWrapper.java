package com.example.cache.aerospike.configuration;

import com.aerospike.client.*;
import com.aerospike.client.policy.WritePolicy;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Configuration
public class AerospikeClientWrapper extends AerospikeConfig {

    protected AerospikeClient client;
    private ObjectMapper mapper = new ObjectMapper();

    public void createConn() {
        if(isAeroEnabled()){
            Host[] hosts = new Host[] {new Host(getAeroSpikeHost(), getAerospikePort())};
            setDefaultClientPolicies();
            client = new AerospikeClient(clientPolicy, hosts);
        }
    }

    public AerospikeClient getClient() {
        return this.client;
    }

    public void closeClient() {
        if (this.client != null) {
            this.client.close();
        }
    }

    protected void isConnected() {
        this.client.isConnected();
    }

    public void write(String set, String keyName, String colName, String colValue) {
        Key key = new Key(getNameSpace(), set, keyName);
        Bin bin = new Bin(colName, colValue);
        client.put(writePolicy, key, bin);
    }

    public void cache(String set, int ttl, String keyName, String colName, String colValue) {
        Key key = new Key(getNameSpace(), set, keyName);
        Bin bin = new Bin(colName, colValue);
        WritePolicy writePolicy = getDefaultWritePolicy();
        writePolicy.expiration = ttl;
        client.put(writePolicy, key, bin);

    }


    public void cache(String set, int ttl, String keyName, String colName, Object colValue) {
        Key key = new Key(getNameSpace(), set, keyName);
        Bin bin = new Bin(colName, Value.getAsBlob(colValue));
        WritePolicy writePolicy = getDefaultWritePolicy();
        writePolicy.expiration = ttl;
        client.put(writePolicy, key, bin);
    }

    public void write(String set, String keyName, Map<String, ? extends Object> binData) throws IOException {
        Key key = new Key(getNameSpace(), set, keyName);
        Bin[] bins = new Bin[binData.size()];
        int array = 0;
        for (Map.Entry<String, ? extends Object> entry : binData.entrySet()) {
            Bin bin = new Bin(entry.getKey(), mapper.readTree((JsonParser) entry.getValue()).toString());
            bins[array] = bin;
            array++;
        }
        client.put(writePolicy, key, bins);
    }

    public void write(String set, String keyName, Map<String, ? extends Object> binData, int ttl) throws IOException {
        Key key = new Key(getNameSpace(), set, keyName);
        Bin[] bins = new Bin[binData.size()];
        int array = 0;
        for (Map.Entry<String, ? extends Object> entry : binData.entrySet()) {
            Bin bin = new Bin(entry.getKey(), mapper.readTree((JsonParser) entry.getValue()).toString());
            bins[array] = bin;
            array++;
        }
        WritePolicy writePolicy = getDefaultWritePolicy();
        writePolicy.expiration = ttl;
        client.put(writePolicy, key, bins);
    }

    public Record read(String set, String keyName) {
        Key key = new Key(getNameSpace(), set, keyName);
        return client.get(readPolicy, key);
    }

    public boolean exists(String set, String keyName) {
        Key key = new Key(getNameSpace(), set, keyName);
        return client.exists(readPolicy, key);
    }

    public Record read(String set, String keyName, String bin) {
        Key key = new Key(getNameSpace(), set, keyName);
        return client.get(readPolicy, key, bin);
    }

    public Record[] read(String set, List<String> keyNames) {
        int size = keyNames.size();
        Key[] keys = new Key[size];
        for (int i = 0; i < size; i++) {
            keys[i] = new Key(getNameSpace(), set, i + 1);
        }
        return client.get(batchPolicy, keys);
    }

    public Record read(String set, String keyName, String[] binArray) {
        Key key = new Key(getNameSpace(), set, keyName);
        return client.get(readPolicy, key, binArray);
    }

    public void delete(String set, String keyName) {
        Key key = new Key(getNameSpace(), set, keyName);
        client.delete(writePolicy, key);
    }

    public Record increment(String set, String keyName, String binName) {
        Key key = new Key(getNameSpace(), set, keyName);
        Bin bin = new Bin(binName, 1);
        return client.operate(writePolicy, key, Operation.add(bin));
    }

    public Record multiOps(String set, String keyName, Operation[] operations) {
        Key key = new Key(getNameSpace(), set, keyName);
        return client.operate(null, key, operations);
    }

}

