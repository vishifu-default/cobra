package org.cobra.core;

import org.cobra.commons.errors.CobraException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class CobraSchema {

    private final String clazzname;

    public CobraSchema(String clazzname) {
        Objects.requireNonNull(clazzname, "clazz name is null");
        this.clazzname = clazzname;
    }

    /**
     * Writes this clazz name into output destination
     */
    public void write(OutputStream os) {
        try (DataOutputStream dataOs = new DataOutputStream(os)) {
            dataOs.writeUTF(this.clazzname);
        } catch (IOException e) {
            throw new CobraException(e);
        }
    }

    public final String clazzName() {
        return this.clazzname;
    }

    public final boolean isDefaultExtractor() {
        return true; // todo: we will pass extractor function here.
    }
}
