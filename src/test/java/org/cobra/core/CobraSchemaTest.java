package org.cobra.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CobraSchemaTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void write() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String clazz = "test.sample.clazz";
        CobraSchema schema = new CobraSchema(clazz);

        schema.write(os);
        os.flush();

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(os.toByteArray()));
        String readClazz = dis.readUTF();
        assertEquals(clazz, readClazz);
    }

    @Test
    void clazzName() {
        String clazz = "test.sample.clazz";
        CobraSchema schema = new CobraSchema(clazz);

        assertEquals(clazz, schema.clazzName());
    }
}