package org.cobra.networks;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class ReceiveNetworkTest {

    @Test
    public void testReadBytes() throws IOException {
        ReceiveNetwork networkReceive = new ReceiveNetwork("test");
        assertEquals(0, networkReceive.readBytes());

        ScatteringByteChannel channel = Mockito.mock(ScatteringByteChannel.class);
        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
        Mockito.when(channel.read(bufferCaptor.capture()))
                .thenAnswer(invocation -> {
                    bufferCaptor.getValue().putInt(128);
                    return 4;
                })
                .thenReturn(0);

        assertEquals(4, networkReceive.readFrom(channel));
        assertEquals(4, networkReceive.readBytes());
        assertFalse(networkReceive.isCompleted());

        Mockito.reset(channel);
        Mockito.when(channel.read(bufferCaptor.capture()))
                .thenAnswer(invocation -> {
                    byte[] randBytes = new byte[64];
                    new Random().nextBytes(randBytes);
                    bufferCaptor.getValue().put(randBytes);
                    return 64;
                });
        assertEquals(64, networkReceive.readFrom(channel));
        assertEquals(68, networkReceive.readBytes());
        assertFalse(networkReceive.isCompleted());

        Mockito.reset(channel);
        Mockito.when(channel.read(bufferCaptor.capture()))
                .thenAnswer(invocation -> {
                    byte[] randBytes = new byte[64];
                    new Random().nextBytes(randBytes);
                    bufferCaptor.getValue().put(randBytes);
                    return 64;
                });

        assertEquals(64, networkReceive.readFrom(channel));
        assertEquals((68 + 64), networkReceive.readBytes());
        assertTrue(networkReceive.isCompleted());
    }
}
