package com.example.demo.task;

import com.example.demo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class MovieUpdateTask {

    private final MovieService movieService;

    @Scheduled(cron = "0 7 9 * * *")
    public void updateMovies() {
        movieService.updateTrendingMovies();
        movieService.updatePayingMovies(1);
    }
}
