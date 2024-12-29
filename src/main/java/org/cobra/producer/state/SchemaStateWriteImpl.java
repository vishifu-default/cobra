package org.cobra.producer.state;

import org.cobra.commons.Jvm;
import org.cobra.commons.pools.BytesPool;
import org.cobra.core.ModelSchema;
import org.cobra.core.bytes.Bytes;
import org.cobra.core.bytes.OnHeapBytes;
import org.cobra.core.encoding.Varint;
import org.cobra.core.memory.datalocal.RecordRepository;
import org.cobra.core.serialization.RecordSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;

public class SchemaStateWriteImpl implements SchemaStateWrite {

    private static final Logger log = LoggerFactory.getLogger(SchemaStateWriteImpl.class);

    private static final Varint varint = Jvm.varint();

    protected final ModelSchema modelSchema;
    protected final RecordSerde serde;

    protected final Bytes toAdditionalRecordStore = OnHeapBytes.create(BytesPool.NONE);
    protected final Bytes toRemovalRecordStore = OnHeapBytes.create(BytesPool.NONE);

    protected final RecordRepository recordRepository = new RecordRepository();

    private boolean isReversedDelta = false;
    private boolean isModifiedLast = true;

    public SchemaStateWriteImpl(ModelSchema modelSchema, RecordSerde serde) {
        this.modelSchema = modelSchema;
        this.serde = serde;
    }

    @Override
    public ModelSchema getSchema() {
        return this.modelSchema;
    }

    @Override
    public boolean isModified() {
        // todo: HARDCODE tmp running
        return this.isModifiedLast;
    }

    @Override
    public void moveToWritePhase() {
        // todo: implement
        log.info("implement me");
    }

    @Override
    public void moveToNextCycle() {
        // todo: must reset
        this.toAdditionalRecordStore.rewind();
        this.toRemovalRecordStore.rewind();
    }

    @Override
    public void writeObject(String key, Object object) {
        byte[] serialized = serde.serialize(object);

        // todo: implement writing check, need to write?
        boolean mustWrite = true;

        if (!mustWrite)
            return;

        byte[] keyBytes = key.getBytes();

        /* a. record key */
        varint.writeVarInt(this.toAdditionalRecordStore, keyBytes.length);
        this.toAdditionalRecordStore.write(keyBytes);

        /* b. record bytes */
        varint.writeVarInt(this.toRemovalRecordStore, serialized.length);
        this.toRemovalRecordStore.write(serialized);

        /* put object to data repo */
        this.recordRepository.putObject(key, serialized);
    }

    @Override
    public void removeObject(String key) {
        byte[] keyBytes = key.getBytes();

        /* remove object from data repo */
        byte[] removalData = this.recordRepository.removeObject(key);

        /* a. removal record key */
        varint.writeVarInt(this.toRemovalRecordStore, keyBytes.length);
        this.toRemovalRecordStore.write(keyBytes);

        /* b. removal record bytes, (used for reversed) */
        varint.writeVarInt(this.toAdditionalRecordStore, removalData.length);
        this.toAdditionalRecordStore.write(removalData);
    }

    @Override
    public void prepareWriteDelta() {
    }

    @Override
    public void writeDelta(DataOutputStream dos) throws IOException {
        this.isReversedDelta = false;
        writeBlobContent(dos);
    }

    @Override
    public void prepareWriteReversedDelta() {
    }

    @Override
    public void writeReversedDelta(DataOutputStream dos) throws IOException {
        this.isReversedDelta = true;
        writeBlobContent(dos);
    }

    private void writeBlobContent(DataOutputStream dos) throws IOException {
        /* BLOB write schema type */
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
        if (bytes.position() == 0)
            return; // end if nothing to write

        /* varint_len of addition */
        varint.writeVarInt(dos, (int) bytes.position());

        byte[] dataOfBytes = new byte[(int) bytes.position()];
        bytes.readAt(0, dataOfBytes);

        dos.write(dataOfBytes);
    }
}
