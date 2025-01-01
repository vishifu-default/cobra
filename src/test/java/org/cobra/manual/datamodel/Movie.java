package org.cobra.manual.datamodel;

public class Movie {
    public int id;
    public int durations;
    public int publishYear;
    public int views;
    public float rating;
    public String title;
    public String publisher;
    public String description;


    public Movie() {
    }

    public Movie(int id, int durations, int publishYear, int views, float rating, String title, String publisher, String description) {
        this.id = id;
        this.durations = durations;
        this.publishYear = publishYear;
        this.views = views;
        this.rating = rating;
        this.title = title;
        this.publisher = publisher;
        this.description = description;
    }
}
