package org.cobra.core.serialization;

import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.MapSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultRegistrations {
    public static Set<SerdeRegistration> DEFAULT_SERDE_REGISTRATIONS;

    static {
        Set<SerdeRegistration> defines = new HashSet<>();

        defines.add(SerdeRegistration.of(Set.class, new CollectionSerializer<>(), 1_000));
        defines.add(SerdeRegistration.of(HashSet.class, new CollectionSerializer<>(), 1_001));
        defines.add(SerdeRegistration.of(Map.class, new MapSerializer<>(), 1_002));
        defines.add(SerdeRegistration.of(HashMap.class, new MapSerializer<>(), 1_003));
        defines.add(SerdeRegistration.of(Collection.class, new CollectionSerializer<>(), 1_004));
        defines.add(SerdeRegistration.of(List.class, new DefaultSerializers.ArraysAsListSerializer(), 1_005));
        defines.add(SerdeRegistration.of(ArrayList.class, new DefaultSerializers.ArraysAsListSerializer(), 1_006));
        defines.add(SerdeRegistration.of(LinkedList.class, new CollectionSerializer<>(), 1_007));

        /* wrapper */
        defines.add(SerdeRegistration.of(Boolean.class, new DefaultSerializers.BooleanSerializer(), 2000));
        defines.add(SerdeRegistration.of(String.class, new DefaultSerializers.StringSerializer(), 2001));
        defines.add(SerdeRegistration.of(Integer.class, new DefaultSerializers.IntSerializer(), 2002));
        defines.add(SerdeRegistration.of(Long.class, new DefaultSerializers.LongSerializer(), 2003));
        defines.add(SerdeRegistration.of(Double.class, new DefaultSerializers.DoubleSerializer(), 2004));
        defines.add(SerdeRegistration.of(Float.class, new DefaultSerializers.FloatSerializer(), 2005));
        defines.add(SerdeRegistration.of(Byte.class, new DefaultSerializers.ByteSerializer(), 2006));
        defines.add(SerdeRegistration.of(Short.class, new DefaultSerializers.ShortSerializer(), 2007));
        defines.add(SerdeRegistration.of(Character.class, new DefaultSerializers.CharSerializer(), 2008));

        /* array */
        defines.add(SerdeRegistration.of(boolean[].class, new DefaultArraySerializers.BooleanArraySerializer(), 2009));
        defines.add(SerdeRegistration.of(char[].class, new DefaultArraySerializers.CharArraySerializer(), 2010));
        defines.add(SerdeRegistration.of(byte[].class, new DefaultArraySerializers.ByteArraySerializer(), 2011));
        defines.add(SerdeRegistration.of(short[].class, new DefaultArraySerializers.ShortArraySerializer(), 2012));
        defines.add(SerdeRegistration.of(int[].class, new DefaultArraySerializers.IntArraySerializer(), 2013));
        defines.add(SerdeRegistration.of(long[].class, new DefaultArraySerializers.LongArraySerializer(), 2014));
        defines.add(SerdeRegistration.of(double[].class, new DefaultArraySerializers.DoubleArraySerializer(), 2015));
        defines.add(SerdeRegistration.of(float[].class, new DefaultArraySerializers.FloatArraySerializer(), 2016));
        defines.add(SerdeRegistration.of(String[].class, new DefaultArraySerializers.StringArraySerializer(), 2017));

        DEFAULT_SERDE_REGISTRATIONS = defines;
    }
}
