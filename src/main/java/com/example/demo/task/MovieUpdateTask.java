package com.example.demo.task;

import com.example.demo.service.MovieLikeService;
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
    private final MovieLikeService movieLikeService;

    @Scheduled(cron = "0 7 9 * * *")
    public void updateMovies() {
        movieService.updateTrendingMovies();
        movieService.updatePayingMovies(1);
    }
    //5분간격으로 실행
   // @Scheduled(fixedDelay = 300000)
    public void likeRecommandMovie() {
        movieLikeService.updateMovieRecommand();
    }



}
