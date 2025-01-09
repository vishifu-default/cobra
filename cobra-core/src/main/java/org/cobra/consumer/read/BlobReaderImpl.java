package org.cobra.consumer.read;

import org.cobra.commons.CobraConstants;
import org.cobra.commons.Jvm;
import org.cobra.commons.utils.Utils;
import org.cobra.core.ModelSchema;
import org.cobra.core.memory.MemoryMode;
import org.cobra.core.objects.BlobInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class BlobReaderImpl implements BlobReader {

    private static final Logger log = LoggerFactory.getLogger(BlobReaderImpl.class);

    private final MemoryMode memoryMode;
    private final StateReadEngine stateReadEngine;

    public BlobReaderImpl(MemoryMode memoryMode, StateReadEngine stateReadEngine) {
        this.memoryMode = memoryMode;
        this.stateReadEngine = stateReadEngine;
    }

    @Override
    public void applyHeader(BlobInput blobInput) throws IOException {
        doApplyHeader(blobInput);
    }

    @Override
    public void applyHeader(InputStream is) throws IOException {
        BlobInput input = BlobInput.serial(is);
        applyHeader(input);
    }

    @Override
    public void applyDelta(BlobInput blobInput) throws IOException {
        doApplyDelta(blobInput);
    }

    @Override
    public void applyDelta(InputStream is) throws IOException {
        BlobInput input = BlobInput.serial(is);
        applyDelta(input);
    }

    private void doApplyHeader(BlobInput blobInput) throws IOException {
        final long start = System.nanoTime();

        doReadHeader(blobInput);

        final long elapsed = System.nanoTime() - start;
        log.debug("applied header took: {}", Utils.formatElapsed(elapsed));
    }

    private void doApplyDelta(BlobInput blobInput) throws IOException {
        final long start = System.nanoTime();

        readAndCheckBlobRandomizedTag(blobInput);

        int numSchemas = blobInput.readInt();
        for (int i = 0; i < numSchemas; i++) {
            readSchemaStateDelta(blobInput);
        }

        final long elapsed = System.nanoTime() - start;
        log.debug("applied delta content took {}", Utils.formatElapsed(elapsed));
    }

    private void readAndCheckBlobRandomizedTag(BlobInput blobInput) throws IOException {
        long inputOriginRandomizedTag = blobInput.readLong();
        long inputNextRandomizedTag = blobInput.readLong();

//        if (inputOriginRandomizedTag != stateReadEngine.getOriginRandomizedTag()
//                && inputNextRandomizedTag != stateReadEngine.getNextRandomizedTag()) {
//            throw new IllegalStateException("Attempt to apply delta state that is not origin with state-engine");
//        }
    }

    private void readSchemaStateDelta(BlobInput blobInput) throws IOException {
        ModelSchema modelSchema = ModelSchema.readFrom(blobInput);
        SchemaStateReader schemaStateReader = stateReadEngine.getSchemaStateReader(modelSchema.getClazzName());
        if (schemaStateReader != null) {
            schemaStateReader.applyDelta(blobInput);
        }
        log.debug("DELTA read schema: {}", modelSchema.getClazzName());
    }

    private void doReadHeader(BlobInput blobInput) throws IOException {
        readRandomizedTag(blobInput);
        readHeaderSchemas(blobInput);
        readHeaderClassRegistration(blobInput);
        skipCompatibilityBytes(blobInput);
    }

    private void readRandomizedTag(BlobInput blobInput) throws IOException {
        /* 1. BLOB read headerId, randomized_tag (long, long) */
        final int headerId = blobInput.readInt();
        if (headerId != CobraConstants.BLOB_HEADER_VERSION_ID)
            throw new IllegalStateException("This blob is not a header");

        long inputOriginRandomizedTag = blobInput.readLong();
        long inputNextRandomizedTag = blobInput.readLong();

        stateReadEngine.setOriginRandomizedTag(inputOriginRandomizedTag);
        stateReadEngine.setNextRandomizedTag(inputNextRandomizedTag);

    }

    private void readHeaderSchemas(BlobInput blobInput) throws IOException {
        /* 2. BLOB_HEADER read model */
        final int numSchemas = blobInput.readInt();
        for (int i = 0; i < numSchemas; i++) {
            ModelSchema modelSchema = ModelSchema.readFrom(blobInput);
            stateReadEngine.consumerContext().register(new SchemaStateReaderImpl(modelSchema, stateReadEngine));
        }
    }

    private void readHeaderClassRegistration(BlobInput blobInput) throws IOException {
        /* 3. BLOB_HEADER read registrations */
        final int registrationSize = blobInput.readInt();
        for (int i = 0; i < registrationSize; i++) {
            final String typeName = blobInput.readUtf();
            final int id = blobInput.readInt();

            Class<?> clazz = Utils.classLoader(typeName);
            stateReadEngine.consumerContext().registerClassRegistration(clazz, id);

            log.debug("registered class: {}; id {}", typeName, id);
        }
    }

    private static void skipCompatibilityBytes(BlobInput blobInput) throws IOException {
        int skipLen = Jvm.varint().readVarInt(blobInput);

        while (skipLen > 0) {
            int skipped = (int) blobInput.skipNBytes(skipLen);
            if (skipped < 0)
                throw new EOFException("End of input, no bytes can be skip");

            skipLen -= skipped;
        }
    }
}
