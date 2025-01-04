package org.learn.springapi.models.datamodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
@NoArgsConstructor
public class Movie {
    public int id;
    private String title;
    private String genre;
    private int releaseYear;
    private double rating;
    private boolean isAvailable;
    private long budget;
    private long boxOfficeCollection;
    private float duration;
    private byte ageRating;
    private char leadActorInitial;
    private short numberOfAwards;
    private boolean isSequel;
    private int sequelNumber;
    private long productionCost;
    private float criticScore;
    private double audienceScore;
    private boolean isBasedOnBook;
    private int numberOfScenes;
    private byte movieRating;
    private char directorInitial;
    private boolean isIMAXSupported;
    private double soundScore;
    private boolean isDubbed;
    private long dubbingCost;
    private int numberOfDubbedLanguages;
    private byte dubbingQualityRating;
    private char composerInitial;
    private short numberOfSoundTracks;
    private boolean hasPostCreditScene;
    private float postCreditSceneScore;
    private double overallExperienceScore;
    private boolean isIndieProduction;
    private long indieProductionCost;
    private int numberOfIndieAwards;
    private byte indieRating;
    private char writerInitial;
    private short numberOfScriptDrafts;
    private boolean hasAlternateEnding;
    private float alternateEndingScore;
    private Publisher publisher;
    private Actor actor;












    public static Movie generateRandomMovie(int id) {
        Random random = new Random();
        Movie movie = new Movie();

        movie.id = id;
        movie.title = "Movie " + random.nextInt(1000);
        movie.genre = random.nextBoolean() ? "Action" : "Drama";
        movie.releaseYear = 1990 + random.nextInt(30);
        movie.rating = 1 + (10 - 1) * random.nextDouble();
        movie.isAvailable = random.nextBoolean();
        movie.budget = 1000000 + random.nextInt(100000000);
        movie.boxOfficeCollection = 5000000 + random.nextInt(500000000);
        movie.duration = 60 + random.nextFloat() * 120;
        movie.ageRating = (byte) (random.nextInt(4) + 1); // G, PG, PG-13, R
        movie.leadActorInitial = (char) ('A' + random.nextInt(26));
        movie.numberOfAwards = (short) random.nextInt(50);
        movie.isSequel = random.nextBoolean();
        movie.sequelNumber = random.nextInt(5);
        movie.productionCost = 1000000 + random.nextInt(100000000);
        movie.criticScore = 1 + (10 - 1) * random.nextFloat();
        movie.audienceScore = 1 + (10 - 1) * random.nextDouble();
        movie.isBasedOnBook = random.nextBoolean();
        movie.numberOfScenes = 50 + random.nextInt(100);
        movie.movieRating = (byte) (random.nextInt(5) + 1); // 1 to 5
        movie.directorInitial = (char) ('A' + random.nextInt(26));
        movie.isIMAXSupported = random.nextBoolean();
        movie.soundScore = 1 + (10 - 1) * random.nextDouble();
        movie.isDubbed = random.nextBoolean();
        movie.dubbingCost = 10000 + random.nextInt(500000);
        movie.numberOfDubbedLanguages = random.nextInt(20);
        movie.dubbingQualityRating = (byte) (random.nextInt(5) + 1);
        movie.composerInitial = (char) ('A' + random.nextInt(26));
        movie.numberOfSoundTracks = (short) random.nextInt(30);
        movie.hasPostCreditScene = random.nextBoolean();
        movie.postCreditSceneScore = 1 + (10 - 1) * random.nextFloat();
        movie.overallExperienceScore = 1 + (10 - 1) * random.nextDouble();
        movie.isIndieProduction = random.nextBoolean();
        movie.indieProductionCost = 100000 + random.nextInt(10000000);
        movie.numberOfIndieAwards = random.nextInt(50);
        movie.indieRating = (byte) (random.nextInt(5) + 1);
        movie.writerInitial = (char) ('A' + random.nextInt(26));
        movie.numberOfScriptDrafts = (short) random.nextInt(20);
        movie.hasAlternateEnding = random.nextBoolean();
        movie.alternateEndingScore = 1 + (10 - 1) * random.nextFloat();
        movie.publisher = Publisher.generateRandomPublisher(id);
        movie.actor = Actor.generateRandomActor(id);

        return movie;
    }

}
