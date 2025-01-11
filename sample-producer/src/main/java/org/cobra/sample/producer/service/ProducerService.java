package org.cobra.sample.producer.service;

import lombok.Getter;
import org.cobra.producer.CobraProducer;
import org.cobra.producer.fs.FilesystemAnnouncer;
import org.cobra.producer.fs.FilesystemBlobStagger;
import org.cobra.producer.fs.FilesystemPublisher;

import org.cobra.sample.models.Actor;
import org.cobra.sample.models.Movie;
import org.cobra.sample.models.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ProducerService {

    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);

    private final CobraProducer producer;

    @Getter
    private final ConcurrentHashMap<Integer, Object> mutations = new ConcurrentHashMap<>();

    @Getter
    private final ConcurrentHashMap<Integer, Movie> inMemoryMovies = new ConcurrentHashMap<>();

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

    public long getCurrentVersion() {
        return producer.currentVersion();
    }

    public List<Movie> getInMemoryMovies(int offset, int limit) {
        return inMemoryMovies.values().stream()
                .limit(limit)
                .skip(offset)
                .collect(Collectors.toList());
    }

    public void addMutation(Integer id, Object movie) {
        this.mutations.put(id, movie);
    }

    public void produce() {
        producer.produce(task -> {
            for (Map.Entry<Integer, Object> entry : mutations.entrySet()) {
                if (entry.getValue() == MutationObject.DELETED) {
                    task.removeObject(String.valueOf(entry.getKey()), Movie.class);
                } else {
                    task.addObject(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        });

        for (Map.Entry<Integer, Object> entry : mutations.entrySet()) {
            if (entry.getValue() == MutationObject.DELETED) {
                continue;
            }
            inMemoryMovies.put(entry.getKey(), (Movie) entry.getValue());
        }
        mutations.clear();
    }

    /* this method use to random */
    public void shuffle(int count) {
        producer.produce(task -> {
            for (Movie movie : generateMoviesWithBound(count)) {
                task.addObject(String.valueOf(movie.getId()), movie);
            }

            for (String s : generateRandomDelete()) {
                task.removeObject(s, Movie.class);
            }
        });
    }

    public void revert(long version) {
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

    private List<String> generateRandomDelete() {
        Random rand = new Random();
        List<String> delete = new ArrayList<>();
        for (int i = 0; i < rand.nextInt(0, 100); i++) {
            delete.add(String.valueOf(rand.nextInt(0, 2_000_000)));
        }
        return delete;
    }

    private static int bound = 5_000_000;

    private List<Movie> generateMoviesWithBound(int count) {
        Random rand = new Random();
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            movies.add(Movie.generateRandomMovie(rand.nextInt(1, bound)));
        }

        return movies;
    }
}
