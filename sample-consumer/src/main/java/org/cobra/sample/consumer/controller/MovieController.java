package org.cobra.sample.consumer.controller;

import org.cobra.sample.consumer.form.FindMovieForm;
import org.cobra.sample.consumer.service.MovieService;
import org.cobra.sample.models.Movie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping(value = "/find")
    public String find(Model model) {
        model.addAttribute("findForm", new FindMovieForm());
        return "movie-find";
    }

    @PostMapping(value = "/find")
    public String find(@ModelAttribute FindMovieForm findForm) {
        return "redirect:/movie/" + findForm.getMovieId();
    }

    @GetMapping(value = "/movie/{id}")
    public String displayMovie(Model model, @PathVariable int id) {
        Movie movie = movieService.getMovie(id);

        if (movie == null)
            return "movie-not-found";

        model.addAttribute("movie", movie);

        return "movie-display";
    }

    @GetMapping(value = "/version")
    public String version(Model model) {
        model.addAttribute("version", movieService.getCurrentVersion());
        return "current-version";
    }
}
