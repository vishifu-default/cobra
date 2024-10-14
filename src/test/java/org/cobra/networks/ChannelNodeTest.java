package org.cobra.networks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChannelNodeTest {

    @Test
    public void test_getNormalId() {
        ChannelNode  node = new ChannelNode("localhost", 9001);
        String id = node.id();

        assertEquals("127.0.0.1:9001", id, "localhost must be resolved to 127.0.0.1");
    }

    @Test
    public void test_unresolved() {
        ChannelNode node = new ChannelNode(".badhost", 9001);
        assertFalse(node.isResolved());
    }

}