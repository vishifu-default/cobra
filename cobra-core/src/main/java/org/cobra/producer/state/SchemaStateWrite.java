package org.cobra.producer.state;

import org.cobra.core.ModelSchema;

import java.io.DataOutputStream;
import java.io.IOException;

public interface SchemaStateWrite {

    ModelSchema getSchema();

    boolean isModified();

    int mutationCount();

    void moveToWritePhase();

    void moveToNextCycle();

    void addRecord(String key, Object object);

    void removeRecord(String key);

    void prepareBeforeWriting();

    void writeDelta(DataOutputStream dos) throws IOException;

    void writeReversedDelta(DataOutputStream dos) throws IOException;
}
