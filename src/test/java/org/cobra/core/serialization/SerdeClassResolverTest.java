package org.cobra.core.serialization;

import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerdeClassResolverTest {

    private SerdeClassResolver serdeClassResolver;

    @BeforeEach
    void setUp() {
        serdeClassResolver = new SerdeClassResolver();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void register_notThrows() {
        serdeClassResolver.register(new Registration(Sample1.class,
                new DefaultSerializers.VoidSerializer(), 100));
        serdeClassResolver.register(new Registration(Sample2.class,
                new DefaultSerializers.VoidSerializer(), 200));

        Set<SerdeRegistration> registrations = serdeClassResolver.registrationEntries();
        Set<Integer> ids = registrations.stream()
                .map(SerdeRegistration::getRegisteredId).collect(Collectors.toSet());
        Set<Class<?>> classes = registrations.stream()
                .map(SerdeRegistration::getRegisteredClazz).collect(Collectors.toSet());

        assertFalse(registrations.isEmpty());
        assertTrue(ids.contains(100));
        assertTrue(ids.contains(200));
        assertTrue(classes.contains(Sample1.class));
        assertTrue(classes.contains(Sample2.class));

    }

    private static class Sample1 {
    }

    private static class Sample2 {

    }
}