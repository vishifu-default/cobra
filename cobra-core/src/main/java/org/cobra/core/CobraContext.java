package org.cobra.core;

import org.cobra.core.memory.datalocal.RecordRepository;
import org.cobra.core.serialization.RecordSerde;

import java.util.Set;

public interface CobraContext {

    RecordSerde getSerde();

    RecordRepository getStore();

    Set<ModelSchema> getModelSchemas();
}
