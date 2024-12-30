package org.cobra.consumer.read;

import org.cobra.commons.Jvm;
import org.cobra.core.ModelSchema;
import org.cobra.core.encoding.Varint;
import org.cobra.core.objects.BlobInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SchemaStateReaderImpl implements SchemaStateReader {

    private static final Logger log = LoggerFactory.getLogger(SchemaStateReaderImpl.class);
    private static final Varint varint = Jvm.varint();

    protected final ModelSchema modelSchema;
    protected final StateReadEngine stateReadEngine;

    public SchemaStateReaderImpl(ModelSchema modelSchema, StateReadEngine stateReadEngine) {
        this.modelSchema = modelSchema;
        this.stateReadEngine = stateReadEngine;
    }

    @Override
    public ModelSchema getSchema() {
        return modelSchema;
    }

    @Override
    public void applyDelta(BlobInput blobInput) throws IOException {
        readDeltaContent(blobInput);
        log.debug("DELTA applied mode: {}", modelSchema);
    }

    private void readDeltaContent(BlobInput blobInput) throws IOException {
        readDeltaAdditional(blobInput);
        readDeltaRemoval(blobInput);
    }

    private void readDeltaAdditional(BlobInput blobInput) throws IOException {
        final int varintAdditionSize = varint.readVarInt(blobInput);
        int pointer = 0;

        while (pointer < varintAdditionSize) {

            /* a. record key */
            int keyLen = varint.readVarInt(blobInput);
            byte[] key = new byte[keyLen];
            blobInput.readNBytes(key, keyLen);
            pointer += varint.sizeOfVarint(keyLen) + keyLen;

            /* b. record bytes */
            int valueLen = varint.readVarInt(blobInput);
            byte[] value = new byte[valueLen];
            blobInput.readNBytes(value, valueLen);
            pointer += varint.sizeOfVarint(valueLen) + valueLen;

            stateReadEngine.addObject(key, value);
        }
    }

    private void readDeltaRemoval(BlobInput blobInput) throws IOException {
        final int varintRemovalSize = varint.readVarInt(blobInput);
        int pointer = 0;
        while (pointer < varintRemovalSize) {

            /* a. record key */
            int keyLen = varint.readVarInt(blobInput);
            byte[] key = new byte[keyLen];
            blobInput.readNBytes(key, keyLen);
            pointer += varint.sizeOfVarint(keyLen);
            pointer += keyLen;

            /* b. record bytes */
            int valueLen = varint.readVarInt(blobInput);
            blobInput.skipNBytes(valueLen);
            pointer += varint.sizeOfVarint(valueLen) + valueLen;

            stateReadEngine.removeObject(key);
        }
    }
}
