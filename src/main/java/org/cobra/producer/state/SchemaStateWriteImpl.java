package org.cobra.producer.state;

import org.cobra.commons.Jvm;
import org.cobra.commons.pools.BytesPool;
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

    protected final Bytes toAdditionalRecordStore = OnHeapBytes.create(BytesPool.NONE);
    protected final Bytes toRemovalRecordStore = OnHeapBytes.create(BytesPool.NONE);
    protected final Bytes toReversedOfRemovalStore = OnHeapBytes.create(BytesPool.NONE);

    private final ProducerStateContext producerStateContext;

    private boolean isReversedDelta = false;

    private final Map<String, Object> mutations = new ConcurrentHashMap<>();

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
        return true; // todo: incremental write
    }

    @Override
    public void moveToWritePhase() {
        // todo: can we shrink?
        this.toAdditionalRecordStore.rewind();
        this.toRemovalRecordStore.rewind();
    }

    @Override
    public void moveToNextCycle() {
        mutations.clear();
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
    public void prepareWriteDelta() {
        for (Map.Entry<String, Object> entry : this.mutations.entrySet()) {

            if (entry.getValue() == DELETE_OBJECT) {
                doWriteRemovalData();
                return;
            }

            byte[] serialized = serde.serialize(entry.getValue());

            // todo: implement writing check, need to write?
            boolean mustWrite = true;

            if (!mustWrite)
                return;

            byte[] rawKey = entry.getKey().getBytes();

            /* a. record key */
            varint.writeVarInt(this.toAdditionalRecordStore, rawKey.length);
            this.toAdditionalRecordStore.write(rawKey);

            /* b. record bytes */
            varint.writeVarInt(this.toAdditionalRecordStore, serialized.length);
            this.toAdditionalRecordStore.write(serialized);

            /* put object to data repo */
            this.producerStateContext.getLocalData().putObject(rawKey, serialized);
        }
    }

    @Override
    public void writeDelta(DataOutputStream dos) throws IOException {
        this.isReversedDelta = false;
        writeBlobContent(dos);
    }

    @Override
    public void prepareWriteReversedDelta() {
        for (Map.Entry<String, Object> entry : this.mutations.entrySet()) {
            if (entry.getValue() != DELETE_OBJECT) continue;

            byte[] rawKey = entry.getKey().getBytes();

            /* remove object from data repo */
            byte[] removalData = this.producerStateContext.getLocalData().removeObject(rawKey);

            if (removalData == null)
                return;

            /* a. removal record key */
            varint.writeVarInt(this.toRemovalRecordStore, rawKey.length);
            this.toRemovalRecordStore.write(rawKey);

            /* b. removal record bytes, (used for reversed) */
            varint.writeVarInt(this.toRemovalRecordStore, removalData.length);
            this.toRemovalRecordStore.write(removalData);
        }
    }

    private void doWriteAdditionalData() {

    }

    private void doWriteRemovalData() {

    }

    @Override
    public void writeReversedDelta(DataOutputStream dos) throws IOException {
        this.isReversedDelta = true;
        writeBlobContent(dos);
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
        doWriteBlobOutputStream(dos, this.toAdditionalRecordStore);
        doWriteBlobOutputStream(dos, this.toRemovalRecordStore);
    }

    private void doWriteReversedDelta(DataOutputStream dos) throws IOException {
        doWriteBlobOutputStream(dos, this.toRemovalRecordStore);
        doWriteBlobOutputStream(dos, this.toAdditionalRecordStore);
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
