package org.learn.springapi.controller;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.learn.springapi.form.FindMovieForm;
import org.learn.springapi.form.RollbackVersion;
import org.learn.springapi.models.datamodel.Movie;
import org.learn.springapi.service.consumer.MovieService;
import org.learn.springapi.service.producer.MutationObject;
import org.learn.springapi.service.producer.ProducerService;
import org.learn.springapi.service.producer.WorkbookHelp;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @GetMapping(value = "/home")
    public String home(Model model) {
        model.addAttribute("message", "Hello World! This is MVC");
        return "home";
    }

    private final MovieService movieService = new MovieService();

    @GetMapping(value = "/find")
    public String find(Model model) {
        model.addAttribute("findForm", new FindMovieForm());
        return "consumer/movie-find";
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

        return "consumer/movie-display";
    }

    @GetMapping(value = "/version")
    public String version(Model model) {
        model.addAttribute("version", movieService.getCurrentVersion());
        return "consumer/current-version";
    }

//
//    /* PRODUCER, this code should be disabled when build consumer */
//    private final ProducerService producerService = new ProducerService();
//
//    @GetMapping(value = "/movie-list")
//    public String movieList(Model model) {
//        List<Movie> movieList = producerService.getInMemoryMovies(0, 100);
//        model.addAttribute("movieList", movieList);
//        return "producer/movie-list";
//    }
//
//    @GetMapping(value = "/movie-submit")
//    public String submitMovie(Model model) {
//        model.addAttribute("movie", new Movie());
//        return "producer/movie-submit";
//    }
//
//    @PostMapping(value = "/movie-submit")
//    public String submitMovie(Model model, @ModelAttribute Movie movie) {
//        model.addAttribute("movie", movie);
//        producerService.addMutation(movie.getId(), movie);
//
//        return "producer/movie-submit-success";
//    }
//
//    @GetMapping("/mutations")
//    public String showProducerCycle(Model model) {
//        List<Movie> addMutations = new ArrayList<>();
//        List<Integer> deletedIds = new ArrayList<>();
//
//        for (Map.Entry<Integer, Object> entry : producerService.getMutations().entrySet()) {
//            if (entry.getValue() instanceof Movie) {
//                addMutations.add((Movie) entry.getValue());
//            } else if (entry.getValue() == MutationObject.DELETED) {
//                deletedIds.add(entry.getKey());
//            }
//        }
//
//        model.addAttribute("version", producerService.getCurrentVersion());
//        model.addAttribute("addMutations", addMutations);
//        model.addAttribute("deleteIds", deletedIds);
//
//        return "producer/mutations";
//    }
//
//    @GetMapping("/produce-new-version")
//    public String submitProducerCycle(Model model) {
//        producerService.produce();
//        return "redirect:/mutations";
//    }
//
//    @GetMapping(value = "/upload-file")
//    public String uploadFile(Model model) {
//        return "producer/upload-file";
//    }
//
//    @PostMapping(value = "/upload-file")
//    public String uploadAddition(
//            @RequestParam("addition-file") MultipartFile additionFile,
//            @RequestParam("deletion-file") MultipartFile deletionFile,
//            Model model) {
//        try {
//            if (additionFile.getOriginalFilename() != null && !additionFile.getOriginalFilename().isBlank()) {
//                InputStream is = additionFile.getInputStream();
//                Workbook workbook = new XSSFWorkbook(is);
//
//                List<Movie> movies = WorkbookHelp.readMoviesFromWorkbook(workbook);
//
//                workbook.close();
//                for (Movie movie : movies) {
//                    producerService.addMutation(movie.getId(), movie);
//                }
//            }
//
//            if (deletionFile.getOriginalFilename() != null && !deletionFile.getOriginalFilename().isBlank()) {
//                InputStream is = deletionFile.getInputStream();
//                Workbook workbook = new XSSFWorkbook(is);
//                List<Integer> ids = WorkbookHelp.readMovieIDsFromWorkbook(workbook);
//                workbook.close();
//
//                for (Integer id : ids) {
//                    producerService.addMutation(id, MutationObject.DELETED);
//                }
//            }
//
//            return "redirect:/mutations";
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @GetMapping("/rollback")
//    public String rollback(Model model) {
//        final long currentVersion = producerService.getCurrentVersion();
//        model.addAttribute("currentVersion", currentVersion);
//        model.addAttribute("form", new RollbackVersion(currentVersion));
//        return "producer/rollback-version";
//    }
//
//    @PostMapping(value = "/rollback")
//    public String rollback(@ModelAttribute RollbackVersion rollbackVersion, Model model) {
//        producerService.revert(rollbackVersion.getToVersion());
//        final long currentVersion = producerService.getCurrentVersion();
//        model.addAttribute("currentVersion", currentVersion);
//        model.addAttribute("form", new RollbackVersion(currentVersion));
//        return "producer/rollback-version";
//    }
}
