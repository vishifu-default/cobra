package org.cobra.producer.fs;

import org.cobra.producer.CobraProducer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FilesystemAnnouncer implements CobraProducer.Announcer {

    public static final String ANNOUNCEMENT_FILENAME = "_version";
    public static final String ANNOUNCEMENT_FILENAME_TEMP = "_version.tmp";

    private final Path path;

    public FilesystemAnnouncer(Path path) {
        this.path = path;
    }

    @Override
    public void announce(long version) {
        Path announcePath = path.resolve(ANNOUNCEMENT_FILENAME);
        Path announcePathTmp = path.resolve(ANNOUNCEMENT_FILENAME_TEMP);
        try {
            Files.write(announcePathTmp, String.valueOf(version).getBytes());
            Files.move(announcePathTmp, announcePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
