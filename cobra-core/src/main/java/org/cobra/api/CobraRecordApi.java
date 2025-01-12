package org.cobra.api;

import org.cobra.RecordApi;
import org.cobra.consumer.CobraConsumer;
import org.cobra.core.memory.datalocal.AssocStore;

public class CobraRecordApi implements RecordApi {

    private final CobraConsumer consumer;

    public CobraRecordApi(CobraConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public byte[] getRaw(String key) {
        return records().getData(key);
    }

    @Override
    public <T> T query(String key) {
        byte[] raw = records().getData(key);
        if (raw == null) {
            return null;
        }
        return consumer.context().getSerde().deserialize(getRaw(key));
    }

    private AssocStore records() {
        return consumer.context().getStore();
    }
}
