package org.cobra.core.objects;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface StreamingBlob {

    InputStream input() throws IOException;

    default File file() throws IOException {
        throw new UnsupportedOperationException("Default access to file for StreamingBlob implementation");
    }
}
