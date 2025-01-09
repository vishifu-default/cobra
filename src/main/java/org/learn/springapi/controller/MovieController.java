package org.learn.springapi.controller;

import org.learn.springapi.models.datamodel.Movie;
import org.learn.springapi.service.consumer.MovieService;
import org.learn.springapi.service.producer.ProducerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MovieController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }


//    public MovieService movieService = new MovieService();
//
//    @GetMapping("/movie/{id}")
//    public Movie getMovie(@PathVariable int id) {
//        Movie movie = movieService.getMovie(id);
//        return movie;
//    }
//
//
//    private ProducerService producerService = new ProducerService();
//
//    @GetMapping("/dump/{count}")
//    public String produce(@PathVariable int count) {
//        producerService.produce(1, count);
//        return "success";
//    }
//
//    @GetMapping("/shuffle/{count}")
//    public String shuffle(@PathVariable int count) {
//        producerService.shuffle(count);
//        return "success";
//    }
//
//    @GetMapping("/revert/{version}")
//    public String revert(@PathVariable int version) {
//        producerService.revert(version);
//
//        return "success";
//    }
}
