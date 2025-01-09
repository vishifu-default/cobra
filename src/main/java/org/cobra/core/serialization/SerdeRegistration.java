package org.cobra.core.serialization;

import com.esotericsoftware.kryo.Serializer;
import lombok.Getter;

public class SerdeRegistration {

    @Getter
    private final Class<?> clazz;
    @Getter
    private final Serializer<?> serializer;
    @Getter
    private final int id;

    public SerdeRegistration(Class<?> clazz, Serializer<?> serializer, int id) {
        this.clazz = clazz;
        this.serializer = serializer;
        this.id = id;
    }

    public static SerdeRegistration of(Class<?> clazz, Serializer<?> serializer, int id) {
        return new SerdeRegistration(clazz, serializer, id);
    }
}
