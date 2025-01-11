package org.cobra.sample.consumer.service;

import org.cobra.sample.models.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class MovieService {
    private static final Logger log = LoggerFactory.getLogger(MovieService.class);
    private final CacheService cacheService = new CacheService();

    public Movie getMovie(int id) {
        Movie movie = cacheService.api().query(String.valueOf(id));

        if (movie != null) {
            log.debug("Found movie with id {}", id);
        }

        return movie;
    }

    public long getCurrentVersion() {
        return cacheService.consumer().currentVersion();
    }
}
