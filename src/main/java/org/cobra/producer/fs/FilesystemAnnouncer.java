package org.cobra.producer.fs;

import org.cobra.commons.errors.CobraException;
import org.cobra.producer.CobraProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantLock;

public class FilesystemAnnouncer implements CobraProducer.Announcer {

    public static final String ANNOUNCEMENT_FILENAME = "_version";
    public static final String ANNOUNCEMENT_FILENAME_TEMP = "_version.tmp";
    private static final Logger log = LoggerFactory.getLogger(FilesystemAnnouncer.class);

    private final Path path;
    private final ReentrantLock lock = new ReentrantLock();

    private boolean isModified = false;
    private volatile long cacheVersion;

    public FilesystemAnnouncer(Path path) {
        this.path = path;
    }

    @Override
    public void announce(long version) {
        Path announcePath = path.resolve(ANNOUNCEMENT_FILENAME);
        Path announcePathTmp = path.resolve(ANNOUNCEMENT_FILENAME_TEMP);
        try {
            lock.lock();
            Files.write(announcePathTmp, String.valueOf(version).getBytes());
            Files.move(announcePathTmp, announcePath, StandardCopyOption.REPLACE_EXISTING);

            this.cacheVersion = version;
            this.isModified = true;
        } catch (IOException e) {
            throw new CobraException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long retrieve() {
        if (!isModified) {
            return this.cacheVersion;
        }

        lock.lock();
        Path announcePath = path.resolve(ANNOUNCEMENT_FILENAME);

        try (BufferedReader br = new BufferedReader(new FileReader(announcePath.toFile()))) {
            String line = br.readLine();
            long version = Long.parseLong(line);
            log.debug("Retrieve version : {}", version);
            return version;
        } catch (IOException e) {
            throw new CobraException(e);
        } finally {
            this.isModified = false;
            lock.unlock();
        }
    }
}
