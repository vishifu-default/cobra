package org.cobra.producer.state;

import org.cobra.core.CobraSchema;

import java.io.DataOutputStream;
import java.io.IOException;

public class SchemaStateWriteImpl implements SchemaStateWrite {
    @Override
    public CobraSchema getSchema() {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public boolean isModified() {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void moveToWritePhase() {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void moveToNextCycle() {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void writeObject(String key, Object object) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void remoteObject(String key) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void writeDelta(DataOutputStream dos) throws IOException {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void writeReversedDelta(DataOutputStream dos) throws IOException {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void setupForReversedDelta() {
        throw new UnsupportedOperationException("implement me");
    }
}
