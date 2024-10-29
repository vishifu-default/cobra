package org.cobra.producer.state;

import org.cobra.core.CobraSchema;

import java.io.DataOutputStream;
import java.io.IOException;

public interface SchemaStateWrite {

    CobraSchema getSchema();

    boolean isModified();

    void moveToWritePhase();

    void moveToNextCycle();

    void writeObject(String key, Object object);

    void remoteObject(String key);

    void writeDelta(DataOutputStream dos) throws IOException;

    void writeReversedDelta(DataOutputStream dos) throws IOException;

    void setupForReversedDelta();
}
