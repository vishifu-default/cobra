package org.cobra.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelSchemaTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void write() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ModelSchema schema = new ModelSchema(Sample.class);

        schema.write(os);
        os.flush();

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(os.toByteArray()));
        String readClazz = dis.readUTF();
        assertEquals(Sample.class.getTypeName(), readClazz);
    }

    @Test
    void getClazzName() {
        ModelSchema schema = new ModelSchema(Sample.class);
        assertEquals(Sample.class.getTypeName(), schema.getClazzName());
    }

    private static final class Sample {
    }
}