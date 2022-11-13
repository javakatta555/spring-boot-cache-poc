package com.example.cache.aerospike.configuration;

import com.aerospike.client.async.AsyncClientPolicy;
import com.aerospike.client.policy.*;
import org.springframework.beans.factory.annotation.Value;

public class AerospikeConfig {

    ClientPolicy clientPolicy;
    private AsyncClientPolicy asyncClientPolicy;
    WritePolicy writePolicy      ;
    QueryPolicy readPolicy       ;
    BatchPolicy batchPolicy      ;

    @Value(value="${cache.aerospike.enabled}")
    private boolean             aeroEnabled ;

    @Value(value="${cache.aerospike.namespace}")
    private String             nameSpace ;

    @Value(value="${cache.aerospike.host}")
    private  String         aeroSpikeHost ;

    @Value(value="${cache.aerospike.port}")
    private  int          aerospikePort ;

    @Value(value="${cache.aerospike.maxThreads}")
    private  int          maxThreads      ;

    @Value(value="${cache.aerospike.timeOut}")
    private  int          timeOut ;

    @Value(value="${cache.aerospike.asyncMaxCommands}")
    private  int          asyncMaxCommands ;

    @Value(value="${cache.aerospike.log.enabled}")
    protected boolean      logEnabled ;

    protected void setDefaultClientPolicies() {
        clientPolicy = new ClientPolicy();
        clientPolicy.connPoolsPerNode = maxThreads;
        clientPolicy.queryPolicyDefault = getDefaultQueryPolicy();
        clientPolicy.readPolicyDefault = getDefaultReadPolicy();
        clientPolicy.writePolicyDefault = getDefaultWritePolicy();
        clientPolicy.batchPolicyDefault = getDefaultBatchPolicy();
    }

    private Policy getDefaultReadPolicy() {
        return getDefaultQueryPolicy();
    }

    protected BatchPolicy getDefaultBatchPolicy() {
        batchPolicy = new BatchPolicy();
        batchPolicy.totalTimeout = timeOut;
        return batchPolicy;
    }


    protected WritePolicy getDefaultWritePolicy() {
        writePolicy = new WritePolicy();
        writePolicy.totalTimeout = timeOut;
        writePolicy.maxRetries = 1;
        writePolicy.sleepBetweenRetries = 50;
        return writePolicy;
    }

    protected QueryPolicy getDefaultQueryPolicy() {
        readPolicy = new QueryPolicy();
        readPolicy.totalTimeout = timeOut;
        readPolicy.maxRetries = 1;
        readPolicy.sleepBetweenRetries = 50;
        return readPolicy;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getNameSpace() {
        return this.nameSpace;
    }

    public void setAsyncClientPolicy() {
        asyncClientPolicy = new AsyncClientPolicy();
        asyncClientPolicy.asyncMaxCommands = asyncMaxCommands;
        asyncClientPolicy.asyncWritePolicyDefault = getDefaultWritePolicy();
        asyncClientPolicy.asyncBatchPolicyDefault = getDefaultBatchPolicy();
        asyncClientPolicy.asyncReadPolicyDefault = getDefaultQueryPolicy();
    }

    public ClientPolicy getClientPolicy() {
        return clientPolicy;
    }

    public AsyncClientPolicy getAsyncClientPolicy() {
        return asyncClientPolicy;
    }

    public WritePolicy getWritePolicy() {
        return writePolicy;
    }

    public Policy getReadPolicy() {
        return readPolicy;
    }

    public BatchPolicy getBatchPolicy() {
        return batchPolicy;
    }

    public boolean isAeroEnabled() {
        return aeroEnabled;
    }

    public String getAeroSpikeHost() {
        return aeroSpikeHost;
    }

    public int getAerospikePort() {
        return aerospikePort;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public int getAsyncMaxCommands() {
        return asyncMaxCommands;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }
}

