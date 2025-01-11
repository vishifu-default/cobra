package org.cobra.sample.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
@NoArgsConstructor
public class Publisher {
    int id;
    private String name;
    private String country;
    private int establishedYear;
    private long revenue;
    private int numberOfBooksPublished;
    private boolean isPublicCompany;
    private double marketShare;
    private short numberOfEmployees;
    private float averageBookPrice;
    private char publisherInitial;










































    public static Publisher generateRandomPublisher(int id) {
        Random random = new Random();
        Publisher publisher = new Publisher();

        publisher.id = id;
        publisher.name = "Publisher " + random.nextInt(1000);
        publisher.country = random.nextBoolean() ? "USA" : "UK";
        publisher.establishedYear = 1900 + random.nextInt(120);
        publisher.revenue = 1000000 + random.nextInt(100000000);
        publisher.numberOfBooksPublished = 100 + random.nextInt(5000);
        publisher.isPublicCompany = random.nextBoolean();
        publisher.marketShare = random.nextDouble();
        publisher.numberOfEmployees = (short) (50 + random.nextInt(5000));
        publisher.averageBookPrice = 5 + random.nextFloat() * 50;
        publisher.publisherInitial = (char) ('A' + random.nextInt(26));

        return publisher;
    }

}
