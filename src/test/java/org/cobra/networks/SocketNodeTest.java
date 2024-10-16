package org.cobra.networks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SocketNodeTest {

    @Test
    public void test_getNormalId() {
        SocketNode node = new SocketNode("localhost", 9001);
        String id = node.id();

        assertEquals("127.0.0.1:9001", id, "localhost must be resolved to 127.0.0.1");
    }

    @Test
    public void test_unresolved() {
        SocketNode node = new SocketNode(".badhost", 9001);
        assertFalse(node.isResolved());
    }

}