package org.cobra.consumer.read;

import org.cobra.commons.Jvm;
import org.cobra.commons.utils.Utils;
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
    }

    private void readDeltaContent(BlobInput blobInput) throws IOException {
        final long start = System.nanoTime();

        final int mutationsCount = varint.readVarInt(blobInput);
        log.debug("mutation_count: {}", mutationsCount);

        readDeltaAdditional(blobInput);
        readDeltaRemoval(blobInput);

        final long elapsed = System.nanoTime() - start;
        log.debug("read delta content done; took: {}", Utils.formatElapsed(elapsed));
    }

    private void readDeltaAdditional(BlobInput blobInput) throws IOException {
        final int lenOfKeys = varint.readVarInt(blobInput);
        long keyOffset = blobInput.getCursor();

        blobInput.seek(blobInput.getCursor() + lenOfKeys);
        final int lenOfValues = varint.readVarInt(blobInput);
        long valueOffset = blobInput.getCursor();

        int position = 0;
        int count = 0; // todo: stats this
        while (position < lenOfKeys) {
            blobInput.seek(keyOffset);
            byte[] key = readBlock(blobInput);
            int skips = varint.sizeOfVarint(key.length) + key.length;
            keyOffset += skips;
            position += skips;

            blobInput.seek(valueOffset);
            byte[] value = readBlock(blobInput);
            valueOffset += varint.sizeOfVarint(value.length) + value.length;

            count++;

            stateReadEngine.addObject(key, value);
        }

        log.debug("read delta add values count {}", count);
    }

    private void readDeltaRemoval(BlobInput blobInput) throws IOException {
        final int lenOfKeys = varint.readVarInt(blobInput);
        int position = 0;
        int count = 0;

        while (position < lenOfKeys) {
            byte[] key = readBlock(blobInput);
            position += varint.sizeOfVarint(key.length) + key.length;
            count++;

            stateReadEngine.removeObject(key);
        }

        log.debug("read delta removal count {}", count);
    }

    private static byte[] readBlock(BlobInput blobInput) throws IOException {
        int needLen = varint.readVarInt(blobInput);
        byte[] block = new byte[needLen];
        blobInput.readNBytes(block, needLen);

        return block;
    }
}
