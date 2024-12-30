package org.cobra.networks.server.internal.handler;

import org.cobra.networks.requests.FetchVersionResponse;
import org.cobra.networks.server.RequestChanel;
import org.cobra.networks.server.internal.SimpleRequest;
import org.cobra.producer.fs.FilesystemAnnouncer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FetchVersionRequestApi {

    public static Path announcedPath = Paths.get("src", "test", "resources", "publish-dir");

    public static void handle(SimpleRequest simpleRequest, RequestChanel requestChanel) {

        // todo: hardcode
        long version;
        Path filepath = announcedPath.resolve(FilesystemAnnouncer.ANNOUNCEMENT_FILENAME);
        try {
            String read = Files.readAllLines(filepath).get(0);
            version = Long.parseLong(read);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FetchVersionResponse response = new FetchVersionResponse(version);
        requestChanel.sendResponse(simpleRequest, response, Optional.empty());
    }
}
