package org.cobra.core.serialization;

import org.cobra.core.ModelSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RecordSerdeImplTest {

    private RecordSerdeImpl serde;

    @BeforeEach
    void setUp() {
        serde = new RecordSerdeImpl();
    }

    @Test
    void register_shouldNotThrow() {
        serde.register(new ModelSchema(ClassA.class));
    }

    @Test
    void serializeAndDeserialize() {
        ClassA a = new ClassA();
        a.bMember = new ClassB();
        a.bMember.arrInt = new int[] {1,2,3};
        a.bMember.cMember = new ClassC();
        a.bMember.cMember.boxLongVal = 100L;

        serde.register(new ModelSchema(ClassA.class));
        byte[] bytes = serde.serialize(a);

        assertNotNull(bytes);

        ClassA aa = serde.deserialize(bytes);
        assertNotNull(aa);
    }

    public static class ClassA {
        ClassB bMember;
        Set<Integer> setInt;

    }

    public static class ClassB {
        ClassC cMember;
        int[] arrInt;
    }

    public static class ClassC {
        int intVal;
        Long boxLongVal;
    }

}