package org.cobra.core.serialization;

import org.cobra.core.ModelSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerdeContextTest {

    @Test
    void register() {
        SerdeContext serdeContext = new SerdeContext();

        serdeContext.register(ClassA.class, 1);
        serdeContext.register(ClassB.class, 2);
        serdeContext.register(ClassC.class, 3);
        serdeContext.register(ClassD.class, 4);
        serdeContext.register(ClassE.class, 5);


        assertEquals(5, serdeContext.getClazzRegistries().size());
    }

    @Test
    void registerModelSchema() {
        SerdeContext serdeContext = new SerdeContext();

        serdeContext.register(new ModelSchema(ClassA.class));

        assertEquals(5, serdeContext.getClazzRegistries().size());
    }

    @Test
    void registerModelSchema_multiple() {
        SerdeContext serdeContext = new SerdeContext();
        serdeContext.register(new ModelSchema(ClassA.class));
        serdeContext.register(new ModelSchema(ClassA2.class));

        assertEquals(6, serdeContext.getClazzRegistries().size());
    }

    static class ClassA {
        ClassB b;
        ClassC c;
    }

    static class ClassA2 {
        ClassB b;
        ClassC c;
        ClassD d;
    }

    static class ClassB {
        ClassD d;
    }
    static class ClassC {
        ClassE e;
    }
    static class ClassD {}
    static class ClassE {}

}