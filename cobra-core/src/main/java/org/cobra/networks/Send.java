package org.cobra.networks;

import java.io.IOException;
import java.nio.channels.GatheringByteChannel;

public interface Send {
    boolean isCompleted();

    Apikey apikey();

    void writeTo(GatheringByteChannel channel) throws IOException;
}
