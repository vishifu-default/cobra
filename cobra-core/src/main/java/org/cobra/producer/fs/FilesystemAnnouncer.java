package org.cobra.producer.fs;

import org.apache.commons.io.FileUtils;
import org.cobra.commons.CobraConstants;
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

    private final Path announcementPath;
    private final Path tmpPath;
    private final ReentrantLock lock = new ReentrantLock();

    private boolean isModified = true;
    private volatile long cacheVersion = 0;

    public FilesystemAnnouncer(Path path) {
        this.announcementPath = path.resolve(ANNOUNCEMENT_FILENAME);
        this.tmpPath = path.resolve(ANNOUNCEMENT_FILENAME_TEMP);
    }

    @Override
    public void bootstrap(boolean shouldRestore) {
        boolean fileExists = Files.exists(announcementPath);
        try {
            // touch if not exist
            if (!fileExists) {
                FileUtils.touch(announcementPath.toFile());
            }

            if (shouldRestore && fileExists) {
                // nop
                return;
            }

            // add init version content
            Files.write(announcementPath, String.valueOf(CobraConstants.VERSION_NULL).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void announce(long version) {
        try {
            lock.lock();
            Files.write(tmpPath, String.valueOf(version).getBytes());
            Files.move(tmpPath, announcementPath, StandardCopyOption.REPLACE_EXISTING);

            this.cacheVersion = version;
            this.isModified = true;

            log.info("announce version to {}", version);
        } catch (IOException e) {
            throw new CobraException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long reclaimVersion() {
        if (!isModified) {
            return this.cacheVersion;
        }

        lock.lock();
        try (BufferedReader br = new BufferedReader(new FileReader(announcementPath.toFile()))) {
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
