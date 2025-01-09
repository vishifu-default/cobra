package org.cobra.core.serialization;

import org.cobra.core.ModelSchema;

public interface RecordSerde {

    /**
     * Registers a schema to associated with a unique ID into serde instance.
     *
     * @param modelSchema schema of data type
     */
    void register(ModelSchema modelSchema);

    void register(Class<?> clazz, int id);

    /**
     * Serializes object into raw bytes.
     *
     * @param object target object
     * @return serialized raw byte
     */
    byte[] serialize(Object object);

    /**
     * Deserializes a raw byte array turning into object class
     *
     * @param bytes raw byte array
     * @param <T>   any type
     * @return deserialized object
     */
    <T> T deserialize(byte[] bytes);

    SerdeContext serdeContext();
}
