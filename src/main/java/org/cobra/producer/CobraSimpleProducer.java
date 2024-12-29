package org.cobra.producer;

public class CobraSimpleProducer extends AbstractProducer {

    public CobraSimpleProducer(Builder builder) {
        super(builder);
    }

    @Override
    public long produce(Populator populator) {
        return runProduce(populator);
    }
}
