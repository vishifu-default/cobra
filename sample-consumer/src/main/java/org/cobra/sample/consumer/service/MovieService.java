package org.cobra.sample.consumer.service;

import org.cobra.sample.models.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieService {
    private static final Logger log = LoggerFactory.getLogger(MovieService.class);
    private CacheService cacheService = new CacheService();

    public Movie getMovie(int id) {
        Movie movie = cacheService.api().get(String.valueOf(id));

        if (movie != null) {
            log.debug("Found movie with id {}", id);
        }

        return movie;
    }

    public long getCurrentVersion() {
        return cacheService.consumer().currentVersion();
    }
}
