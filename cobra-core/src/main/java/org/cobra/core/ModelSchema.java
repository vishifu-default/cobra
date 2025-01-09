package org.cobra.core;

import org.cobra.commons.errors.CobraException;
import org.cobra.commons.utils.Utils;
import org.cobra.core.objects.BlobInput;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class ModelSchema {

    private final Class<?> clazz;

    public ModelSchema(@NotNull Class<?> clazz) {
        this.clazz = Objects.requireNonNull(clazz, "clazz is null");
    }

    public static ModelSchema readFrom(BlobInput blobInput) throws IOException {
        String type = blobInput.readUtf();
        return new ModelSchema(Utils.classLoader(type));
    }

    /**
     * Writes this clazz name into output destination
     */
    public void write(OutputStream os) {
        try (DataOutputStream dataOs = new DataOutputStream(os)) {
            dataOs.writeUTF(getClazzName());
        } catch (IOException e) {
            throw new CobraException(e);
        }
    }

    public Class<?> getClazz() {
        return this.clazz;
    }

    public String getClazzName() {
        return this.clazz.getTypeName();
    }

    public final boolean isDefaultExtractor() {
        return true; // todo: we will pass extractor function here.
    }

    @Override
    public String toString() {
        return "RecordSchema(clazz=%s)".formatted(clazz);
    }
}
