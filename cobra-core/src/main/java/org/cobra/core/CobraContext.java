package org.cobra.core;

import org.cobra.core.memory.datalocal.AssocStore;
import org.cobra.core.serialization.RecordSerde;

import java.util.Set;

public interface CobraContext {

    RecordSerde getSerde();

    AssocStore getStore();

    Set<ModelSchema> getModelSchemas();
}
