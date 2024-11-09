package org.cobra.core.hashing;

import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class HashingTableTest {

    HashingTable lookupTable;

    @BeforeEach
    void setUp() {
        lookupTable = new HashingTable();
    }

    @AfterEach
    void tearDown() {
        System.out.println(lookupTable.dumpSensor());
    }

    @Test
    void put_get() {
        long v1 = 359_003L;
        lookupTable.put(1, v1);
        assertEquals(v1, lookupTable.get(1));

        lookupTable.put(5, v1);
        assertEquals(v1, lookupTable.get(5));

        assertEquals(2, lookupTable.size());
        assertEquals(-1, lookupTable.get(10));
    }

    @Test
    void collision() {
        // 2 % 128 = 2
        // 130 % 128 = 2
        long v1 = 100L;
        lookupTable.put(2, v1);
        lookupTable.put(130, v1);

        assertEquals(lookupTable.get(2), lookupTable.get(130));
    }

    @Test
    void remove() {
        long v1 = 359_003L;
        lookupTable.put(1, v1);
        lookupTable.put(5, v1);

        assertEquals(v1, lookupTable.get(1));

        long removeVal = lookupTable.remove(1);
        assertEquals(v1, removeVal);
        assertEquals(-1, lookupTable.get(1));
        assertEquals(1, lookupTable.size());
    }

    @Test
    void resize() {
        assertResize(300);
    }

    @Test
    void resize_more() {
        int count = 1_000_000;
        int i = 0;
        while (i < count) {
           int key = TestUtils.randInt(0, 20_000_000);
           lookupTable.put(key, i);
           i++;
        }

    }

    @Test
    void concurrency() throws InterruptedException {
        this.lookupTable = new HashingTable(32);
        Thread th1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                lookupTable.put(i, i);
            }
        });
        Thread th2 = new Thread(() -> {
            for (int i = 10; i < 30; i++) {
                lookupTable.put(i, i);
            }
        });
        Thread th3 = new Thread(() -> {
            for (int i = 30; i < 60; i++) {
                lookupTable.put(i, i);
            }
        });

        th1.start();
        th2.start();
        th3.start();

        th1.join();
        th2.join();
        th3.join();

        for (int i = 0; i < 60; i++) {
            long ret = lookupTable.get(i);
            if (i != ret)
                fail("expected = %d; actual = %d".formatted(i, ret));
        }
    }

    private void assertResize(int count) {
        for (int i = 0; i < count; i++) {
            lookupTable.put(i, i);
        }
        assertEquals(count, lookupTable.size());


        for (int i = 0; i < count; i++) {
            long ret = lookupTable.get(i);
            if (i != ret)
                fail("resize make failed; expected = %d; actual = %d".formatted(i, ret));
        }
    }
}