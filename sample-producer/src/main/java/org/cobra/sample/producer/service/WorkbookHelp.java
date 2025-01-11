package org.cobra.sample.producer.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.cobra.sample.models.Movie;

import java.util.ArrayList;
import java.util.List;

public class WorkbookHelp {
    public static List<Movie> readMoviesFromWorkbook(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        List<Movie> movies = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }

            Movie movie = new Movie();
            movie.setId((int) row.getCell(0).getNumericCellValue());
            movie.setReleaseYear((int) row.getCell(1).getNumericCellValue());
            movie.setRating(row.getCell(2).getNumericCellValue());
            movie.setDuration((int) row.getCell(3).getNumericCellValue());
            movie.setTitle(row.getCell(4).getStringCellValue());
            movie.setDirector(row.getCell(5).getStringCellValue());
            movie.setGenre(row.getCell(6).getStringCellValue());
            movie.setCountry(row.getCell(8).getStringCellValue());
            movie.setAvailableOnStreaming(row.getCell(9).getBooleanCellValue());

            Cell urlCell = row.getCell(7);
            if (urlCell != null) {
                String url = urlCell.getStringCellValue();
                if (url != null && !url.isBlank()) {
                    movie.setImageUrl(url);
                }
            }

            movies.add(movie);
        }

        return movies;
    }

    public static List<Integer> readMovieIDsFromWorkbook(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        List<Integer> movieIDs = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }

            int id = (int) row.getCell(0).getNumericCellValue();
            movieIDs.add(id);
        }
        return movieIDs;
    }
}
