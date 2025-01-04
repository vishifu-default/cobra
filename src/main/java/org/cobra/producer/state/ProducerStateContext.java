package org.cobra.producer.state;

import org.cobra.commons.errors.CobraException;
import org.cobra.core.ModelSchema;
import org.cobra.core.serialization.RecordSerde;
import org.cobra.core.serialization.RecordSerdeImpl;
import org.cobra.core.serialization.SerdeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ProducerStateContext {

    private static final Logger log = LoggerFactory.getLogger(ProducerStateContext.class);

    private static final String EXCEPTION_NOT_FOUND_SCHEMA_WRITE = "Could not find matched registered state-write";

    private final RecordSerde serde = new RecordSerdeImpl();
    private final Map<String, SchemaStateWrite> schemaStateWriteMap = new ConcurrentHashMap<>();

    /* last state */
    final Set<Class<?>> lastRegisteredClazzes = ConcurrentHashMap.newKeySet();
    final Set<ModelSchema> lastSchemas = ConcurrentHashMap.newKeySet();

    public SerdeContext serdeContext() {
        return this.serde.serdeContext();
    }

    public void registerModel(ModelSchema schema) {
        serde.register(schema);
        putSchemaWriteIfAbsent(schema);
    }

    public Set<ModelSchema> collectRegisteredSchemas() {
        return schemaStateWriteMap.values()
                .stream()
                .map(SchemaStateWrite::getSchema)
                .collect(Collectors.toSet());
    }

    public Collection<SchemaStateWrite> collectSchemaStateWrites() {
        return schemaStateWriteMap.values();
    }

    public void addObject(String key, Object obj) {
        SchemaStateWrite schemaStateWrite = schemaWrite(obj.getClass());
        try {
            schemaStateWrite.writeObject(key, obj);
        } catch (Throwable cause) {
            log.error("error while writing object; key: {}; clazz: {}", key, obj.getClass(), cause);
            throw cause;
        }
    }

    public void removeObject(String key, Class<?> clazz) {
        SchemaStateWrite schemaStateWrite = schemaWrite(clazz);
        try {
            schemaStateWrite.removeObject(key);
        } catch (Throwable cause) {
            log.error("error while removing object; key: {}; clazz: {}", key, clazz, cause);
            throw cause;
        }
    }

    private SchemaStateWrite schemaWrite(Type type) {
        SchemaStateWrite schemaStateWrite = schemaStateWriteMap.get(type.getTypeName());
        if (schemaStateWrite == null)
            throw new CobraException(EXCEPTION_NOT_FOUND_SCHEMA_WRITE);

        return schemaStateWrite;
    }

    private void putSchemaWriteIfAbsent(ModelSchema schema) {
        SchemaStateWrite schemaWrite = new SchemaStateWriteImpl(schema, this.serde);
        schemaStateWriteMap.putIfAbsent(schema.getClazzName(), schemaWrite);
    }
}
