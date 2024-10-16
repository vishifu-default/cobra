package org.cobra.networks;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SendNetworkTest {

    @Test
    public void testCompleteSend() throws IOException {
        byte[] randBytes1 = new byte[64];
        new Random().nextBytes(randBytes1);
        ByteBuffer buffer1 = ByteBuffer.wrap(randBytes1);
        SendByteBuffer bufferSend = new SendByteBuffer(buffer1);

        SendNetwork networkSend = new SendNetwork("test", bufferSend);
        TransferableChannel channel = Mockito.mock(TransferableChannel.class);
        ArgumentCaptor<ByteBuffer[]> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer[].class);
        Mockito.when(channel.write(bufferCaptor.capture()))
                .thenAnswer(invocation -> {
                    byte[] dest = new byte[randBytes1.length];
                    buffer1.get(dest);
                    return (long) dest.length;
                });

        networkSend.writeTo(channel);

        assertTrue(networkSend.isCompleted());
    }
}
