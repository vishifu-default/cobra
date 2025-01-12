package org.cobra.producer;

public class CobraSimpleProducer extends AbstractProducer {

    public CobraSimpleProducer(Builder builder) {
        super(builder);
    }

    @Override
    public long populate(Populator populator) {
        return runCycle(populator);
    }

    @Override
    public boolean pinVersion(long version) {
        return moveToVersion(version);
    }
}
