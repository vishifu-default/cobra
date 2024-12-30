package org.cobra.consumer.read;

import org.cobra.core.ModelSchema;
import org.cobra.core.objects.BlobInput;

import java.io.IOException;

public interface SchemaStateReader {
    ModelSchema getSchema();

    void applyDelta(BlobInput blobInput) throws IOException;
}
