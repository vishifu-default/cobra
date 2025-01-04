package org.learn.springapi.service.producer;

import org.cobra.producer.CobraProducer;
import org.cobra.producer.fs.FilesystemAnnouncer;
import org.cobra.producer.fs.FilesystemBlobStagger;
import org.cobra.producer.fs.FilesystemPublisher;
import org.learn.springapi.models.datamodel.Actor;
import org.learn.springapi.models.datamodel.Movie;
import org.learn.springapi.models.datamodel.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProducerService {

    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);

    private final CobraProducer producer;

    public ProducerService() {
        String dir = System.getenv("PUBLISH_DIR");
        if (dir == null) {
            dir = "./misc/publish-dir";
        }

        Path publishDir = Paths.get(dir);
        log.info("Publishing directory: {}", publishDir);

        CobraProducer.BlobPublisher publisher = new FilesystemPublisher(publishDir);
        CobraProducer.Announcer announcer = new FilesystemAnnouncer(publishDir);
        CobraProducer.BlobStagger stagger = new FilesystemBlobStagger();

        producer = CobraProducer.fromBuilder()
                .withBlobPublisher(publisher)
                .withAnnouncer(announcer)
                .withLocalPort(7070)
                .withBlobStagger(stagger)
                .withBlobStorePath(publishDir)
                .buildSimple();

        producer.registerModel(Movie.class);
        producer.registerModel(Actor.class);
        producer.registerModel(Publisher.class);

        producer.bootstrapServer();
    }

    public void produce(int mode, int count) {
        producer.produce(task -> {
            for (Movie movie : generateNewMovies(count)) {
                task.addObject(String.valueOf(movie.id), movie);
            }
        });
    }

    public void revert(int version) {
        producer.pinVersion(version);
    }

    private int nextPump = 0;

    private List<Movie> generateNewMovies(int count) {
        List<Movie> movies = new ArrayList<>();

        for (int i = nextPump; i < nextPump + count; i++) {
            movies.add(Movie.generateRandomMovie(i));
        }

        nextPump += count;
        return movies;
    }
}
