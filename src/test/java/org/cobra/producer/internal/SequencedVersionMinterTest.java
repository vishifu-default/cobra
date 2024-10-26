package org.cobra.producer.internal;

import org.cobra.producer.CobraProducer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequencedVersionMinterTest {

    @Test
    void mintNewVersion() {
        CobraProducer.VersionMinter versionMinter = new SequencedVersionMinter();
        long firstVersion = versionMinter.mint();
        long secondVersion = versionMinter.mint();

        assertEquals(firstVersion, secondVersion - 1);
    }

}