package org.cobra.manual.producer;

import org.cobra.manual.datamodel.Movie;
import org.cobra.producer.CobraProducer;
import org.cobra.producer.fs.FilesystemAnnouncer;
import org.cobra.producer.fs.FilesystemBlobStagger;
import org.cobra.producer.fs.FilesystemPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Producer {
    private static final Logger log = LoggerFactory.getLogger(Producer.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        Path publishDir = Paths.get("./src/main/resources/publish-dir");


        CobraProducer.BlobPublisher publisher = new FilesystemPublisher(publishDir);
        CobraProducer.Announcer announcer = new FilesystemAnnouncer(publishDir);
        CobraProducer.BlobStagger stagger = new FilesystemBlobStagger();

        CobraProducer producer = CobraProducer.fromBuilder()
                .withBlobPublisher(publisher)
                .withAnnouncer(announcer)
                .withBlobStagger(stagger)
                .withBlobStorePath(publishDir)
                .buildSimple();

        producer.registerModel(Movie.class);
        producer.bootstrapServer();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            int mode = scanner.nextInt();

            if (mode == 1) {
                producer.produce(task -> {
                    for (Movie movie : generateMovies())
                        task.addObject(String.valueOf(movie.id), movie);
                });
            } else {
                continue;
            }
        }

    }

    private static final Random rand = new Random();

    private static List<Movie> generateMovies() {
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            Movie movie = new Movie(rand.nextInt(), rand.nextInt(), rand.nextInt(), rand.nextInt(), rand.nextFloat(),
                    generateRandomString(), generateRandomString(), generateRandomString());
            movies.add(movie);
        }

        return movies;
    }

    private static String generateRandomString() {
        StringBuilder str = new StringBuilder();
        int nameChars = rand.nextInt(20) + 5;

        for (int j = 0; j < nameChars; j++) {
            str.append((char) (rand.nextInt(26) + 97));
        }
        return str.toString();
    }
}
