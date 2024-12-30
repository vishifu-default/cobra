package org.cobra.consumer.read;

import org.cobra.core.memory.datalocal.RecordRepository;
import org.cobra.core.serialization.RecordSerde;
import org.cobra.core.serialization.RecordSerdeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerStateContext {

    private static final Logger log = LoggerFactory.getLogger(ConsumerStateContext.class);

    private final RecordSerde serde = new RecordSerdeImpl();
    private final Map<String, SchemaStateReader> schemaStateReaderMap = new ConcurrentHashMap<>();
    private final RecordRepository localData = new RecordRepository();

    public void register(SchemaStateReader stateReader) {
        putSchemaReadIfAbsent(stateReader);
    }

    public void registerClassRegistration(Class<?> clazz, int id) {
        serde.register(clazz, id);
    }

    public RecordRepository localData() {
        return localData;
    }

    public SchemaStateReader schemaRead(String typeName) {
        SchemaStateReader schemaStateReader = schemaStateReaderMap.get(typeName);
        if (schemaStateReader == null) {
            throw new IllegalStateException("No SchemaStateReader found for type " + typeName);
        }

        return schemaStateReader;
    }

    private void putSchemaReadIfAbsent(SchemaStateReader schemaStateReader) {
        schemaStateReaderMap.putIfAbsent(schemaStateReader.getSchema().getClazzName(),
                schemaStateReader);
    }
}
