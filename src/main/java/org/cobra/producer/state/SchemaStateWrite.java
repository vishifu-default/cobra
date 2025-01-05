package org.cobra.producer.state;

import org.cobra.core.ModelSchema;

import java.io.DataOutputStream;
import java.io.IOException;

public interface SchemaStateWrite {

    ModelSchema getSchema();

    boolean isModified();

    void moveToWritePhase();

    void moveToNextCycle();

    void addRecord(String key, Object object);

    void removeRecord(String key);

    void prepareWriteDelta();

    void writeDelta(DataOutputStream dos) throws IOException;

    void prepareWriteReversedDelta();

    void writeReversedDelta(DataOutputStream dos) throws IOException;
}
