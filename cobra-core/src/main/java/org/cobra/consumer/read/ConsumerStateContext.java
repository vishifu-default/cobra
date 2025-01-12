package org.cobra.consumer.read;

import org.cobra.core.CobraContext;
import org.cobra.core.ModelSchema;
import org.cobra.core.memory.datalocal.AssocStore;
import org.cobra.core.serialization.RecordSerde;
import org.cobra.core.serialization.RecordSerdeImpl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConsumerStateContext implements CobraContext {

    private final RecordSerde serde = new RecordSerdeImpl();
    private final Map<String, SchemaStateReader> schemaStateReaderMap = new ConcurrentHashMap<>();
    private final AssocStore assoc = new AssocStore();

    public void register(SchemaStateReader stateReader) {
        putSchemaReadIfAbsent(stateReader);
    }

    public void registerClassRegistration(Class<?> clazz, int id) {
        serde.register(clazz, id);
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

    @Override
    public RecordSerde getSerde() {
        return serde;
    }

    @Override
    public AssocStore getStore() {
        return assoc;
    }

    @Override
    public Set<ModelSchema> getModelSchemas() {
        return schemaStateReaderMap.values().stream()
                .map(SchemaStateReader::getSchema)
                .collect(Collectors.toSet());
    }
}
