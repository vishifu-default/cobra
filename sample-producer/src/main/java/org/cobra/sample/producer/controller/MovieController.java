package org.cobra.sample.producer.controller;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cobra.sample.models.Movie;
import org.cobra.sample.producer.form.RollbackVersion;
import org.cobra.sample.producer.service.MutationObject;
import org.cobra.sample.producer.service.ProducerService;
import org.cobra.sample.producer.service.WorkbookHelp;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class MovieController {


    /* PRODUCER, this code should be disabled when build consumer */
    private final ProducerService producerService = new ProducerService();

    @GetMapping(value = "/movie-list")
    public String movieList(Model model) {
        List<Movie> movieList = producerService.getInMemoryMovies(0, 100);
        model.addAttribute("movieList", movieList);
        return "movie-list";
    }

    @GetMapping(value = "/movie-submit")
    public String submitMovie(Model model) {
        model.addAttribute("movie", new Movie());
        return "movie-submit";
    }

    @PostMapping(value = "/movie-submit")
    public String submitMovie(Model model, @ModelAttribute Movie movie) {
        model.addAttribute("movie", movie);
        producerService.addMutation(movie.getId(), movie);

        return "movie-submit-success";
    }

    @GetMapping("/mutations")
    public String showProducerCycle(Model model) {
        List<Movie> addMutations = new ArrayList<>();
        List<Integer> deletedIds = new ArrayList<>();

        for (Map.Entry<Integer, Object> entry : producerService.getMutations().entrySet()) {
            if (entry.getValue() instanceof Movie) {
                addMutations.add((Movie) entry.getValue());
            } else if (entry.getValue() == MutationObject.DELETED) {
                deletedIds.add(entry.getKey());
            }
        }

        model.addAttribute("version", producerService.getCurrentVersion());
        model.addAttribute("addMutations", addMutations);
        model.addAttribute("deleteIds", deletedIds);

        return "mutations";
    }

    @GetMapping("/produce-new-version")
    public String submitProducerCycle(Model model) {
        producerService.produce();
        return "redirect:/mutations";
    }

    @GetMapping(value = "/upload-file")
    public String uploadFile(Model model) {
        return "upload-file";
    }

    @PostMapping(value = "/upload-file")
    public String uploadAddition(
            @RequestParam("addition-file") MultipartFile additionFile,
            @RequestParam("deletion-file") MultipartFile deletionFile,
            Model model) {
        try {
            if (additionFile.getOriginalFilename() != null && !additionFile.getOriginalFilename().isBlank()) {
                InputStream is = additionFile.getInputStream();
                Workbook workbook = new XSSFWorkbook(is);

                List<Movie> movies = WorkbookHelp.readMoviesFromWorkbook(workbook);

                workbook.close();
                for (Movie movie : movies) {
                    producerService.addMutation(movie.getId(), movie);
                }
            }

            if (deletionFile.getOriginalFilename() != null && !deletionFile.getOriginalFilename().isBlank()) {
                InputStream is = deletionFile.getInputStream();
                Workbook workbook = new XSSFWorkbook(is);
                List<Integer> ids = WorkbookHelp.readMovieIDsFromWorkbook(workbook);
                workbook.close();

                for (Integer id : ids) {
                    producerService.addMutation(id, MutationObject.DELETED);
                }
            }

            return "redirect:/mutations";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/rollback")
    public String rollback(Model model) {
        final long currentVersion = producerService.getCurrentVersion();
        model.addAttribute("currentVersion", currentVersion);
        model.addAttribute("form", new RollbackVersion(currentVersion));
        return "rollback-version";
    }

    @PostMapping(value = "/rollback")
    public String rollback(@ModelAttribute RollbackVersion rollbackVersion, Model model) {
        producerService.revert(rollbackVersion.getToVersion());
        final long currentVersion = producerService.getCurrentVersion();
        model.addAttribute("currentVersion", currentVersion);
        model.addAttribute("form", new RollbackVersion(currentVersion));
        return "rollback-version";
    }
}