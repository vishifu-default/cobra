package org.cobra.producer.state;

import org.cobra.commons.errors.CobraException;
import org.cobra.core.CobraContext;
import org.cobra.core.ModelSchema;
import org.cobra.core.memory.datalocal.AssocStore;
import org.cobra.core.serialization.RecordSerde;
import org.cobra.core.serialization.RecordSerdeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ProducerStateContext implements CobraContext {

    private static final Logger log = LoggerFactory.getLogger(ProducerStateContext.class);

    private static final String EXCEPTION_NOT_FOUND_SCHEMA_WRITE = "Could not find matched registered state-write";

    private RecordSerde serde = new RecordSerdeImpl();
    private AssocStore assoc = new AssocStore();
    private final Map<String, SchemaStateWrite> schemaStateWriteMap = new ConcurrentHashMap<>();

    /* last state */
    final Set<Class<?>> lastRegisteredClazzes = ConcurrentHashMap.newKeySet();
    final Set<ModelSchema> lastSchemas = ConcurrentHashMap.newKeySet();


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
        return schemaStateWriteMap.values().stream()
                .map(SchemaStateWrite::getSchema)
                .collect(Collectors.toSet());
    }

    public void registerModel(ModelSchema schema) {
        serde.register(schema);
        putSchemaWriteIfAbsent(schema);
    }

    public Collection<SchemaStateWrite> collectSchemaStateWrites() {
        return schemaStateWriteMap.values();
    }

    public void addObject(String key, Object obj) {
        SchemaStateWrite schemaStateWrite = schemaWrite(obj.getClass());
        try {
            schemaStateWrite.addRecord(key, obj);
        } catch (Throwable cause) {
            log.error("error while writing object; key: {}; clazz: {}", key, obj.getClass(), cause);
            throw cause;
        }
    }

    public void removeObject(String key, Class<?> clazz) {
        SchemaStateWrite schemaStateWrite = schemaWrite(clazz);
        try {
            schemaStateWrite.removeRecord(key);
        } catch (Throwable cause) {
            log.error("error while removing object; key: {}; clazz: {}", key, clazz, cause);
            throw cause;
        }
    }

    void restoreSerde(RecordSerde serde) {
        this.serde = serde;
    }

    void restoreAssoc(AssocStore assoc) {
        this.assoc.destroy();
        this.assoc = assoc;
    }

    void restoreSchemaStateWrite(Set<ModelSchema> schemas) {
        this.schemaStateWriteMap.clear();
        for (final ModelSchema schema : schemas) {
            putSchemaWriteIfAbsent(schema);
        }
    }

    private SchemaStateWrite schemaWrite(Type type) {
        SchemaStateWrite schemaStateWrite = schemaStateWriteMap.get(type.getTypeName());
        if (schemaStateWrite == null)
            throw new CobraException(EXCEPTION_NOT_FOUND_SCHEMA_WRITE);

        return schemaStateWrite;
    }

    private void putSchemaWriteIfAbsent(ModelSchema schema) {
        SchemaStateWrite schemaWrite = new SchemaStateWriteImpl(schema, this.serde, this);
        schemaStateWriteMap.putIfAbsent(schema.getClazzName(), schemaWrite);
    }
}
