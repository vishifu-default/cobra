package org.cobra.commons.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class IOx {
    /**
     * Create a new file if not existed
     *
     * @param file file
     * @return true if new file is created, false if exists or fail to create.
     */
    public static boolean touch(File file) throws IOException {
        if (file.exists())
            return false;

        return file.createNewFile();
    }

    public static boolean mkdirs(File file) {
        if (file.exists())
            return false;

        return file.mkdirs();
    }

    public static boolean touch(Path path) throws IOException {
        return path.toFile().createNewFile();
    }

    public static boolean mkdirs(Path path) {
        return path.toFile().mkdirs();
    }

}
