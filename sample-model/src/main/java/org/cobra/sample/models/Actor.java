package org.cobra.sample.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
@NoArgsConstructor
public class Actor {
    public int id;

    // Basic Information
    private String firstName;
    private String lastName;
    private int age;
    private String gender;
    private String nationality;

    // Career Information
    private int yearsOfExperience;
    private int numberOfMovies;
    private int numberOfAwards;
    private String debutMovie;
    private String mostFamousRole;

    // Physical Attributes
    private double height; // in cm
    private double weight; // in kg
    private String hairColor;
    private String eyeColor;

    // Contact Information
    private String email;
    private String phoneNumber;
    private String address;

    // Social Media
    private String instagramHandle;
    private String twitterHandle;
    private String facebookPage;

    // Skills
    private boolean canSing;
    private boolean canDance;
    private boolean canAct;
    private boolean canDirect;
    private boolean canWrite;

    // Preferences
    private String favoriteGenre;
    private String favoriteDirector;
    private String favoriteActor;

    // Miscellaneous
    private boolean isMarried;
    private int numberOfChildren;
    private String almaMater;
    private boolean hasWonOscar;
    private boolean hasWonEmmy;
    private boolean hasWonGoldenGlobe;

    // Financials
    private double netWorth; // in million USD
    private double annualIncome; // in million USD

    // Availability
    private boolean isAvailableForNextProject;
    private String currentProject;

    // Additional Attributes
    private String agentName;
    private String agentContact;

    // Getters and setters for all fields (can be generated using IDE)

    // Generator Method
    public static Actor generateRandomActor(int id) {
        Random random = new Random();
        Actor actor = new Actor();

        actor.id = id;
        actor.firstName = "FirstName" + random.nextInt(100);
        actor.lastName = "LastName" + random.nextInt(100);
        actor.age = 18 + random.nextInt(50);
        actor.gender = random.nextBoolean() ? "Male" : "Female";
        actor.nationality = "Nationality" + random.nextInt(10);

        actor.yearsOfExperience = random.nextInt(30);
        actor.numberOfMovies = random.nextInt(100);
        actor.numberOfAwards = random.nextInt(20);
        actor.debutMovie = "DebutMovie" + random.nextInt(50);
        actor.mostFamousRole = "Role" + random.nextInt(50);

        actor.height = 150 + random.nextDouble() * 50;
        actor.weight = 50 + random.nextDouble() * 50;
        actor.hairColor = "HairColor" + random.nextInt(5);
        actor.eyeColor = "EyeColor" + random.nextInt(5);

        actor.email = "actor" + random.nextInt(1000) + "@example.com";
        actor.phoneNumber = "123-456-789" + random.nextInt(10);
        actor.address = "Address" + random.nextInt(100);

        actor.instagramHandle = "@actor" + random.nextInt(1000);
        actor.twitterHandle = "@actor" + random.nextInt(1000);
        actor.facebookPage = "facebook.com/actor" + random.nextInt(1000);

        actor.canSing = random.nextBoolean();
        actor.canDance = random.nextBoolean();
        actor.canAct = true; // Assume all actors can act
        actor.canDirect = random.nextBoolean();
        actor.canWrite = random.nextBoolean();

        actor.favoriteGenre = "Genre" + random.nextInt(10);
        actor.favoriteDirector = "Director" + random.nextInt(50);
        actor.favoriteActor = "Actor" + random.nextInt(50);

        actor.isMarried = random.nextBoolean();
        actor.numberOfChildren = random.nextInt(5);
        actor.almaMater = "University" + random.nextInt(50);
        actor.hasWonOscar = random.nextBoolean();
        actor.hasWonEmmy = random.nextBoolean();
        actor.hasWonGoldenGlobe = random.nextBoolean();

        actor.netWorth = random.nextDouble() * 100;
        actor.annualIncome = random.nextDouble() * 10;

        actor.isAvailableForNextProject = random.nextBoolean();
        actor.currentProject = "Project" + random.nextInt(50);

        actor.agentName = "Agent" + random.nextInt(50);
        actor.agentContact = "123-456-789" + random.nextInt(10);

        return actor;
    }

}
