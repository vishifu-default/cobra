package org.cobra.core.objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersioningBlobTest {

    @Test
    void isDelta() {
        VersioningBlob versioningBlob = new VersioningBlob(1, 2) {
        };

        assertEquals(BlobType.DELTA_BLOB, versioningBlob.blobType());
        assertTrue(versioningBlob.isDeltaBlob());
    }

    @Test
    void isReverseDelta() {
        VersioningBlob versioningBlob = new VersioningBlob(2, 1) {
        };

        assertEquals(BlobType.REVERSED_DELTA_BLOB, versioningBlob.blobType());
        assertTrue(versioningBlob.isReversedDeltaBlob());
    }
}