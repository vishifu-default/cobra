package org.cobra.producer.state;

import org.cobra.core.ModelSchema;

import java.io.DataOutputStream;
import java.io.IOException;

public interface SchemaStateWrite {

    ModelSchema getSchema();

    boolean isModified();

    void moveToWritePhase();

    void moveToNextCycle();

    void writeObject(String key, Object object);

    void removeObject(String key);

    void prepareWriteDelta();

    void writeDelta(DataOutputStream dos) throws IOException;

    void prepareWriteReversedDelta();

    void writeReversedDelta(DataOutputStream dos) throws IOException;
}
