package org.cobra.consumer.read;

import org.cobra.commons.pools.BytesPool;

public class StateReadEngine {

    private final ConsumerStateContext consumerStateContext;
    private final BytesPool bytesPool;

    private long originRandomizedTag;
    private long nextRandomizedTag;

    public StateReadEngine(ConsumerStateContext consumerStateContext, BytesPool bytesPool) {
        this.consumerStateContext = consumerStateContext;
        this.bytesPool = bytesPool;
    }

    public long getOriginRandomizedTag() {
        return originRandomizedTag;
    }

    public void setOriginRandomizedTag(long originRandomizedTag) {
        this.originRandomizedTag = originRandomizedTag;
    }

    public long getNextRandomizedTag() {
        return nextRandomizedTag;
    }

    public void setNextRandomizedTag(long nextRandomizedTag) {
        this.nextRandomizedTag = nextRandomizedTag;
    }

    public ConsumerStateContext consumerContext() {
        return consumerStateContext;
    }

    public void addObject(byte[] key, byte[] representation) {
        consumerStateContext.getStore().putObject(key, representation);
    }

    public void removeObject(byte[] key) {
        consumerStateContext.getStore().removeObject(key);
    }

    public SchemaStateReader getSchemaStateReader(String clazzName) {
        return consumerStateContext.schemaRead(clazzName);
    }
}
