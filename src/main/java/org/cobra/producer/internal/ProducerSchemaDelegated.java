package org.cobra.producer.internal;

import org.cobra.core.CobraSchema;
import org.cobra.producer.state.SchemaStateWrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ProducerSchemaDelegated {

    private static final Logger log = LoggerFactory.getLogger(ProducerSchemaDelegated.class);

    private final Map<String, SchemaStateWrite> typenameToStateWrites;

    public ProducerSchemaDelegated() {
        this.typenameToStateWrites = new ConcurrentHashMap<>();
    }

    public void registerModels(Set<Class<?>> classRegistrations) {
        // todo:  register to serde instance
        throw new UnsupportedOperationException("implement me");
    }

    public Map<Integer, Class<?>> getRegisteredResolverClasses() {
        // todo: retrieve all resolver class in serde
        throw new UnsupportedOperationException("implement me");
    }

    /**
     * @return the current map of {@link SchemaStateWrite}
     */
    public Map<String, SchemaStateWrite> getStateWrites() {
        return this.typenameToStateWrites;
    }

    /**
     * Turns a map of {@link SchemaStateWrite} into a set of {@link CobraSchema}
     *
     * @return current set of {@link CobraSchema} in context
     */
    public Set<CobraSchema> getSchemas() {
        return this.typenameToStateWrites.values().stream()
                .map(SchemaStateWrite::getSchema)
                .collect(Collectors.toSet());
    }

    /**
     * Adds an object with associated key (unique) into state
     *
     * @param key    identity key
     * @param object object data
     */
    public void addObject(String key, Object object) {
        throw new UnsupportedOperationException("implement me");
    }

    /**
     * Removes an object from current state
     *
     * @param key   identity key
     * @param clazz class type
     */
    public void removeObject(String key, Class<?> clazz) {
        throw new UnsupportedOperationException("implement me");
    }

    private SchemaStateWrite schemaStateWrite(Type type) {
        final SchemaStateWrite stateWrite = this.typenameToStateWrites.get(type.getTypeName());
        if (stateWrite == null) {
            log.error("Failed to find state-write for type {}; maybe need to register model for this (using #registerModel())", type);
            throw new IllegalStateException("Could not find SchemaStateWrite for " + type.getTypeName());
        }

        return stateWrite;
    }


    @Override
    public String toString() {
        return "ProducerStatesContext()";
    }
}
