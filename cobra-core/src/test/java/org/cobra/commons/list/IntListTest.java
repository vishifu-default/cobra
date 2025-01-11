package org.cobra.commons.list;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class IntListTest {

    private IntList intList;

    @BeforeEach
    void setUp() {
        intList = new IntList();
    }

    @AfterEach
    void tearDown() {
        intList.clear();
    }

    @Test
    void add_get() {
        intList.add(1);
        intList.add(2);

        assertEquals(2, intList.size());
        assertEquals(1, intList.get(0));
        assertEquals(2, intList.get(1));

        intList.set(0, 10);
        assertEquals(10, intList.get(0));
        assertEquals(2, intList.size());
    }

    @Test
    void sort() {
        intList.add(10);
        intList.add(1);
        intList.add(5);

        intList.sort();
        assertArrayEquals(new int[]{1, 5, 10}, intList.copyRangeOf(0, 3));

        intList.reverse();
        assertArrayEquals(new int[]{10, 5, 1}, intList.copyRangeOf(0, 3));
    }

    @Test
    void expand() {
        for (int i = 0; i < 100; i++) {
            intList.add(i);
        }

        for (int i = 0; i < 100; i++) {
            if (i != intList.get(i))
                fail();
        }
    }

    @Test
    void clear() {
        intList.add(10);
        intList.add(1);

        intList.clear();

        assertEquals(0, intList.size());
    }
}