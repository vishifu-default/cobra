package org.cobra.producer.state;

import org.cobra.commons.Jvm;
import org.cobra.commons.pools.BytesPool;
import org.cobra.commons.utils.Elapsed;
import org.cobra.core.ModelSchema;
import org.cobra.core.bytes.Bytes;
import org.cobra.core.bytes.OnHeapBytes;
import org.cobra.core.encoding.Varint;
import org.cobra.core.serialization.RecordSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SchemaStateWriteImpl implements SchemaStateWrite {

    private static final Logger log = LoggerFactory.getLogger(SchemaStateWriteImpl.class);

    private static final Object DELETE_OBJECT = new Object();
    private static final Varint varint = Jvm.varint();

    protected final ModelSchema modelSchema;
    protected final RecordSerde serde;
    protected final ProducerStateContext producerStateContext;
    protected final Map<String, Object> mutations = new ConcurrentHashMap<>();

    private final Bytes toAdditionalKeyBytes = OnHeapBytes.create(BytesPool.NONE);
    private final Bytes toAdditionalObjectBytes = OnHeapBytes.create(BytesPool.NONE);
    private final Bytes toRemovalKeyBytes = OnHeapBytes.create(BytesPool.NONE);
    private final Bytes toRemovalObjectForReverseBytes = OnHeapBytes.create(BytesPool.NONE);

    private boolean isReversedDelta = false;

    public SchemaStateWriteImpl(ModelSchema modelSchema, RecordSerde serde, ProducerStateContext producerStateContext) {
        this.modelSchema = modelSchema;
        this.serde = serde;
        this.producerStateContext = producerStateContext;
    }

    @Override
    public ModelSchema getSchema() {
        return this.modelSchema;
    }

    @Override
    public boolean isModified() {
        return !this.mutations.isEmpty();
    }

    @Override
    public int mutationCount() {
        return this.mutations.size();
    }

    @Override
    public void moveToWritePhase() {
        // todo: can we shrink?
    }

    @Override
    public void moveToNextCycle() {
        mutations.clear();

        toAdditionalKeyBytes.rewind();
        toAdditionalObjectBytes.rewind();

        toRemovalKeyBytes.rewind();
        toRemovalObjectForReverseBytes.rewind();
    }

    @Override
    public void addRecord(String key, Object object) {
        this.mutations.put(key, object);
    }

    @Override
    public void removeRecord(String key) {
        this.mutations.put(key, DELETE_OBJECT);
    }

    @Override
    public void prepareBeforeWriting() {
        final long start = System.nanoTime();
        for (Map.Entry<String, Object> entry : this.mutations.entrySet()) {

            if (entry.getValue() == DELETE_OBJECT) {
                doPrepareRemovalData(entry.getKey());
                continue; // continue process other entry
            }

            doPrepareAdditionalData(entry.getKey(), entry.getValue());
        }

        log.debug("prepare before writing schema state {}; elapsed: {}", modelSchema.getClazzName(),
                Elapsed.toStr(System.nanoTime() - start));
    }

    @Override
    public void writeDelta(DataOutputStream dos) throws IOException {
        final long start = System.nanoTime();
        this.isReversedDelta = false;
        writeBlobContent(dos);

        log.debug("write delta {}; elapsed: {}", modelSchema.getClazzName(), Elapsed.toStr(System.nanoTime() - start));
    }

    @Override
    public void writeReversedDelta(DataOutputStream dos) throws IOException {
        final long start = System.nanoTime();
        this.isReversedDelta = true;
        writeBlobContent(dos);

        log.debug("write reversed-delta {}; elapsed: {}", modelSchema.getClazzName(),
                Elapsed.toStr(System.nanoTime() - start));
    }

    private void doPrepareAdditionalData(String key, Object object) {
        byte[] rawKey = key.getBytes();
        byte[] serializedObject = serde.serialize(object);

        /* a. object key */
        writeBlock(toAdditionalKeyBytes, rawKey);
        /* b. object bytes */
        writeBlock(toAdditionalObjectBytes, serializedObject);

        /* put object to data repo */
        this.producerStateContext.getLocalData().putObject(rawKey, serializedObject);
    }

    private void doPrepareRemovalData(String key) {
        byte[] rawKey = key.getBytes();
        byte[] removalData = this.producerStateContext.getLocalData().removeObject(rawKey);

        if (removalData == null || removalData.length == 0) {
            return; // return if none
        }

        /* a. removal key */
        writeBlock(toRemovalKeyBytes, rawKey);
        /* setup for reversed */
        writeBlock(toRemovalObjectForReverseBytes, removalData);
    }

    private static void writeBlock(Bytes bytes, byte[] block) {
        varint.writeVarInt(bytes, block.length); // write var_block_length
        bytes.write(block); // write block_bytes
    }

    private void writeBlobContent(DataOutputStream dos) throws IOException {
        /* BLOB_DELTA write schema type */
        dos.writeUTF(this.modelSchema.getClazzName());

        if (!this.isReversedDelta) {
            doWriteDelta(dos);
        } else {
            doWriteReversedDelta(dos);
        }
    }

    private void doWriteDelta(DataOutputStream dos) throws IOException {
        varint.writeVarInt(dos, mutationCount());

        doWriteBlobOutputStream(dos, toAdditionalKeyBytes);
        doWriteBlobOutputStream(dos, toAdditionalObjectBytes);

        doWriteBlobOutputStream(dos, toRemovalKeyBytes);
    }

    private void doWriteReversedDelta(DataOutputStream dos) throws IOException {
        varint.writeVarInt(dos, mutationCount());

        doWriteBlobOutputStream(dos, toRemovalKeyBytes);
        doWriteBlobOutputStream(dos, toRemovalObjectForReverseBytes);

        doWriteBlobOutputStream(dos, toAdditionalKeyBytes);
    }

    private static void doWriteBlobOutputStream(DataOutputStream dos, Bytes bytes) throws IOException {
        if (bytes.position() == 0) {
            varint.writeVarInt(dos, 0);
            return; // end if nothing to write
        }

        /* varint_len of bytes */
        varint.writeVarInt(dos, (int) bytes.position());

        byte[] dataOfBytes = new byte[(int) bytes.position()];
        bytes.readAt(0, dataOfBytes);

        dos.write(dataOfBytes);
    }
}
