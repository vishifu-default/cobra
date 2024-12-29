package org.cobra.producer.state;

import org.cobra.commons.threads.CobraThreadExecutor;
import org.cobra.commons.utils.Rands;

import java.util.Set;
import java.util.stream.Collectors;

public class StateWriteEngine {

    private static final String PROC_DESC_STATE_WRITE_ENGINE_WRITING = "state_write_engine.writing";
    private static final String PROC_DESC_STATE_WRITE_ENGINE_NEXT_CYCLE = "state_write_engine.next_cycle";

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
                final CobraThreadExecutor executor = CobraThreadExecutor.ofPhysicalProcessor(getClass(),
                        PROC_DESC_STATE_WRITE_ENGINE_WRITING)
        ) {
            for (final SchemaStateWrite schemaStateWrite : this.producerStateContext.collectSchemaStateWrites()) {
                executor.execute(schemaStateWrite::moveToWritePhase);
            }
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
                final CobraThreadExecutor executor = CobraThreadExecutor.ofPhysicalProcessor(getClass(),
                        PROC_DESC_STATE_WRITE_ENGINE_NEXT_CYCLE)
        ) {
            for (final SchemaStateWrite schemaStateWrite : this.producerStateContext.collectSchemaStateWrites()) {
                executor.execute(schemaStateWrite::moveToNextCycle);
            }
        }

        this.phase = Phasing.NEXT_CYCLE;
    }

    public boolean isModified() {
        for (final SchemaStateWrite schemaStateWrite : this.producerStateContext.collectSchemaStateWrites()) {
            if (schemaStateWrite.isModified())
                return true;
        }

        return false;
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
