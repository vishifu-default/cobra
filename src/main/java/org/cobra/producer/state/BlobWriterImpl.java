package org.cobra.producer.state;

import org.cobra.commons.Jvm;
import org.cobra.commons.threads.CobraThreadExecutor;
import org.cobra.core.ModelSchema;
import org.cobra.core.encoding.Varint;
import org.cobra.producer.internal.HeaderBlob;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BlobWriterImpl implements BlobWriter {

    private static final String PROC_DESC_WRITE_DELTA = "blob_writer_impl.delta";
    private static final String PROC_DESC_WRITE_REVERSED_DELTA = "blob_writer_impl.reversed_delta";

    private static final Varint varint = Jvm.varint();

    private final StateWriteEngine stateWriteEngine;

    public BlobWriterImpl(StateWriteEngine stateWriteEngine) {
        this.stateWriteEngine = stateWriteEngine;
    }

    @Override
    public void writeHeader(OutputStream os) throws IOException {
        this.stateWriteEngine.moveToWritePhase();
        DataOutputStream dos = new DataOutputStream(os);

        doWriteHeader(dos);

        os.flush();
    }

    @Override
    public void writeDelta(OutputStream os) throws IOException {
        this.stateWriteEngine.moveToWritePhase();
        DataOutputStream dos = new DataOutputStream(os);

        dos.writeLong(this.stateWriteEngine.getOriginRandomizedTag());
        dos.writeLong(this.stateWriteEngine.getNextRandomizedTag());

        Set<SchemaStateWrite> modifiedStateWriteSet = collectModifiedSchemaStateWrite();

        try (
                final CobraThreadExecutor executor = CobraThreadExecutor.ofPhysicalProcessor(getClass(),
                        PROC_DESC_WRITE_DELTA)
        ) {
            for (final SchemaStateWrite stateWrite : modifiedStateWriteSet) {
                executor.execute(stateWrite::prepareWriteDelta);
            }
        }

        for (final SchemaStateWrite stateWrite : modifiedStateWriteSet) {
            stateWrite.writeDelta(dos);
        }

        os.flush();
    }

    @Override
    public void writeReversedDelta(OutputStream os) throws IOException {
        this.stateWriteEngine.moveToWritePhase();
        DataOutputStream dos = new DataOutputStream(os);

        dos.writeLong(this.stateWriteEngine.getNextRandomizedTag());
        dos.writeLong(this.stateWriteEngine.getOriginRandomizedTag());

        Set<SchemaStateWrite> modifiedStateWriteSet = collectModifiedSchemaStateWrite();

        try (
                final CobraThreadExecutor executor = CobraThreadExecutor.ofPhysicalProcessor(getClass(),
                        PROC_DESC_WRITE_REVERSED_DELTA)
        ) {
            for (final SchemaStateWrite stateWrite : modifiedStateWriteSet) {
                executor.execute(stateWrite::prepareWriteReversedDelta);
            }
        }

        for (final SchemaStateWrite stateWrite : modifiedStateWriteSet) {
            stateWrite.writeReversedDelta(dos);
        }

        os.flush();
    }

    private Set<SchemaStateWrite> collectModifiedSchemaStateWrite() {
        return this.stateWriteEngine.collectAffectedSchemaStateWrite();
    }

    private Set<ModelSchema> collectModifiedSchemas() {
        return this.stateWriteEngine.collectAffectedSchemaStateWrite()
                .stream()
                .map(SchemaStateWrite::getSchema)
                .collect(Collectors.toSet());
    }

    private void doWriteHeader(DataOutputStream dos) throws IOException {
        /* write version_id, tag */
        dos.writeInt(HeaderBlob.VERSION_ID);
        dos.writeLong(this.stateWriteEngine.getOriginRandomizedTag());
        dos.writeLong(this.stateWriteEngine.getNextRandomizedTag());

        doWriteHeaderModifiedSchema(dos);
        doWriteHeaderClassRegistration(dos);

        // backward compatibility, will be skipped when reading
        varint.writeVarInt(dos, 0);
    }

    private void doWriteHeaderModifiedSchema(DataOutputStream dos) throws IOException {
        for (final ModelSchema schema : collectModifiedSchemas()) {
            dos.writeUTF(schema.getClazzName());
        }
    }

    private void doWriteHeaderClassRegistration(DataOutputStream dos) throws IOException {
        Map<String, Integer> clazzRegistrationEntries = this.stateWriteEngine.producerStateContext()
                .serdeClassResolver()
                .registrationClassTypeEntries();

        dos.writeInt(clazzRegistrationEntries.size());
        for (Map.Entry<String, Integer> entry : clazzRegistrationEntries.entrySet()) {
            dos.writeUTF(entry.getKey());
            dos.writeInt(entry.getValue());
        }
    }
}
