package org.cobra.core.objects;

import org.cobra.core.RecordSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BlobHeaderContext {

    public static final int BLOB_HEADER_VERSION_ID = 2_000;

    private long originRandomizedTag;
    private long destinationRandomizedTag;
    private Collection<RecordSchema> schemas = new ArrayList<>();
    private Map<Integer, Class<?>> registeredResolverClassIds = new HashMap<>();

    public long getDestinationRandomizedTag() {
        return destinationRandomizedTag;
    }

    public void setDestinationRandomizedTag(long destinationRandomizedTag) {
        this.destinationRandomizedTag = destinationRandomizedTag;
    }

    public long getOriginRandomizedTag() {
        return originRandomizedTag;
    }

    public void setOriginRandomizedTag(long originRandomizedTag) {
        this.originRandomizedTag = originRandomizedTag;
    }

    public Map<Integer, Class<?>> getRegisteredResolverClassIds() {
        return registeredResolverClassIds;
    }

    public void setRegisteredResolverClassIds(Map<Integer, Class<?>> registeredResolverClassIds) {
        this.registeredResolverClassIds = registeredResolverClassIds;
    }

    public Collection<RecordSchema> getSchemas() {
        return schemas;
    }

    public void setSchemas(Collection<RecordSchema> schemas) {
        this.schemas = schemas;
    }
}
