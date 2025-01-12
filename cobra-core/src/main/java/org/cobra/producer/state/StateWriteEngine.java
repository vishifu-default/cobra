package org.cobra.producer.state;

import lombok.extern.slf4j.Slf4j;
import org.cobra.commons.threads.CobraPlatformThreadExecutor;
import org.cobra.commons.utils.Rands;
import org.cobra.consumer.read.ConsumerStateContext;
import org.cobra.core.serialization.RecordSerde;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class StateWriteEngine {

    private static final String EXECUTOR_GROUP = "engine-writer";
    private static final String EXECUTOR_GROUP_WRITE_PHASE_DESC = "write-phase";
    private static final String EXECUTOR_GROUP_NEXT_CYCLE_DESC = "next-cycle";

    private final ProducerStateContext producerStateContext;

    private long originRandomizedTag = -1L;
    private long nextRandomizedTag;

    private Phasing phase = Phasing.NEXT_CYCLE;

    public StateWriteEngine(ProducerStateContext producerStateContext) {
        this.producerStateContext = producerStateContext;
    }

    private enum Phasing {
        WRITING_PHASE,
        NEXT_CYCLE;
    }

    public ProducerStateContext producerStateContext() {
        return this.producerStateContext;
    }

    public void moveToWritePhase() {
        if (this.phase.equals(Phasing.WRITING_PHASE)) {
            return;
        }

        try (
                final CobraPlatformThreadExecutor platformExecutor =
                        CobraPlatformThreadExecutor.ofDaemon(8, EXECUTOR_GROUP, EXECUTOR_GROUP_WRITE_PHASE_DESC)
        ) {
            for (final SchemaStateWrite stateWrite : producerStateContext.collectSchemaStateWrites()) {
                platformExecutor.execute(stateWrite::moveToWritePhase);
            }

            platformExecutor.await();
        } catch (ExecutionException | InterruptedException e) {
            log.error("interrupt execution; {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        this.phase = Phasing.WRITING_PHASE;
    }

    public void moveToNextCycle() {
        if (this.phase.equals(Phasing.NEXT_CYCLE)) {
            return;
        }

        this.originRandomizedTag = this.nextRandomizedTag;
        this.nextRandomizedTag = mintRandomizedTag();

        try (
                final CobraPlatformThreadExecutor platformExecutor =
                        CobraPlatformThreadExecutor.ofDaemon(8, EXECUTOR_GROUP, EXECUTOR_GROUP_NEXT_CYCLE_DESC)
        ) {
            for (final SchemaStateWrite schemaStateWrite : this.producerStateContext.collectSchemaStateWrites()) {
                platformExecutor.execute(schemaStateWrite::moveToNextCycle);
            }
        }

        this.phase = Phasing.NEXT_CYCLE;
    }

    public void revertToLastState() {
        // todo: implement me
    }

    public boolean isModified() {
        for (final SchemaStateWrite schemaStateWrite : this.producerStateContext.collectSchemaStateWrites()) {
            if (schemaStateWrite.isModified())
                return true;
        }

        return false;
    }

    public void restore(ConsumerStateContext readContext) {
        producerStateContext.restoreSerde(readContext.getSerde());
        producerStateContext.restoreAssoc(readContext.getStore());
        producerStateContext.restoreSchemaStateWrite(readContext.getModelSchemas());
    }

    long getOriginRandomizedTag() {
        return this.originRandomizedTag;
    }

    long getNextRandomizedTag() {
        return this.nextRandomizedTag;
    }

    Set<SchemaStateWrite> collectAffectedSchemaStateWrite() {
        return this.producerStateContext.collectSchemaStateWrites()
                .stream()
                .filter(SchemaStateWrite::isModified)
                .collect(Collectors.toSet());
    }

    private long mintRandomizedTag() {
        return Rands.randLong();
    }
}
