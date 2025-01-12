package org.cobra.producer.state;

import org.cobra.commons.CobraConstants;
import org.cobra.commons.Jvm;
import org.cobra.commons.threads.CobraPlatformThreadExecutor;
import org.cobra.core.ModelSchema;
import org.cobra.core.encoding.Varint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class BlobWriterImpl implements BlobWriter {

    private static final Logger log = LoggerFactory.getLogger(BlobWriterImpl.class);
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

        dos.writeInt(modifiedStateWriteSet.size());

        try (
                final CobraPlatformThreadExecutor platformExecutor =
                        CobraPlatformThreadExecutor.ofDaemon(modifiedStateWriteSet.size(), "blob-writer", "write-delta")
        ) {
            for (final SchemaStateWrite schemaStateWrite : modifiedStateWriteSet) {
                platformExecutor.execute(schemaStateWrite::prepareBeforeWriting);
            }

            platformExecutor.await();
        } catch (ExecutionException | InterruptedException e) {
            log.error("interrupt execution; {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
        dos.writeInt(modifiedStateWriteSet.size());

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
        dos.writeInt(CobraConstants.BLOB_HEADER_VERSION_ID);
        dos.writeLong(this.stateWriteEngine.getOriginRandomizedTag());
        dos.writeLong(this.stateWriteEngine.getNextRandomizedTag());

        doWriteHeaderModifiedSchema(dos);
        doWriteHeaderClassRegistration(dos);

        // backward compatibility, will be skipped when reading
        varint.writeVarInt(dos, 0);
    }

    private void doWriteHeaderModifiedSchema(DataOutputStream dos) throws IOException {
        final Set<ModelSchema> modifiedSchemas = collectModifiedSchemas();
        final Set<ModelSchema> lastStateSchemas = this.stateWriteEngine.producerStateContext().lastSchemas;

        final Set<ModelSchema> newSchemas = modifiedSchemas
                .stream()
                .filter(x -> !lastStateSchemas.contains(x))
                .collect(Collectors.toSet());

        lastStateSchemas.addAll(newSchemas);

        dos.writeInt(newSchemas.size());
        for (final ModelSchema schema : newSchemas) {
            dos.writeUTF(schema.getClazzName());
        }
    }

    private void doWriteHeaderClassRegistration(DataOutputStream dos) throws IOException {
        Map<Class<?>, Integer> clazzRegistration = this.stateWriteEngine.producerStateContext()
                .getSerde()
                .serdeContext().collectRegistrations();

        Map<String, Integer> affectedRegistries = new HashMap<>();
        Set<Class<?>> lastClazzSet = stateWriteEngine.producerStateContext().lastRegisteredClazzes;
        for (Map.Entry<Class<?>, Integer> entry : clazzRegistration.entrySet()) {
            if (!lastClazzSet.contains(entry.getKey())) {
                affectedRegistries.put(entry.getKey().getName(), entry.getValue());
                lastClazzSet.add(entry.getKey());
            }
        }

        dos.writeInt(affectedRegistries.size());
        for (Map.Entry<String, Integer> entry : affectedRegistries.entrySet()) {
            dos.writeUTF(entry.getKey());
            dos.writeInt(entry.getValue());
        }
    }
}
