package org.cobra.commons.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigDefTest {

    @Test
    public void testSetAndGet() {
        ConfigDef def = new ConfigDef()
                .define("1", 1)
                .define("2", 2);

        assertEquals(1, (int) def.valueOf("1"));
        assertEquals(2, (int) def.valueOf("2"));
    }

    @Test
    public void testMerge() {
        ConfigDef def = new ConfigDef()
                .define("1", 1)
                .define("2", 2);

        def.merge(new ConfigKey("1", 11));
        assertEquals(11, (int) def.valueOf("1"));
        assertEquals(2, (int) def.valueOf("2"));
    }

    @Test
    void testMerge_nonExisted() {
        ConfigDef def = new ConfigDef()
                .define("1", 1)
                .define("2", 2);

        assertThrows(IllegalStateException.class, () -> def.merge(new ConfigKey("test", 1)));
    }
}
