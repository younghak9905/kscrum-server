package com.example.demo.controller;

import com.example.demo.domain.dto.MovieDto;
import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.dto.MovieResponseDto;
import com.example.demo.repository.MovieRepository;
import com.example.demo.service.TmdbClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movie")
@RequiredArgsConstructor
public class MovieController {

    private final TmdbClient tmdbClient;

    private final MovieRepository movieRepository;



    @GetMapping("/search")
    public ResponseEntity<MovieResponseDto> searchMovies(@RequestParam String query) {
        return ResponseEntity.ok(tmdbClient.searchMovies(query));
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovieDetails(@PathVariable Long movieId) {
        return ResponseEntity.ok(tmdbClient.getMovieDetails(movieId));
    }

    @GetMapping("/poster")
    public ResponseEntity<MoviePosterDto> getMoviePoster(@RequestParam String query) {
        MovieResponseDto movies = tmdbClient. searchMoviePoster(query);
        MoviePosterDto posterDto = MoviePosterDto.builder()
                .movieId(movies.getResults().get(0).getId())
                .title(movies.getResults().get(0).getOriginalTitle())
                .posterPath(movies.getResults().get(0).getPosterPath())
                .url("https://image.tmdb.org/t/p/w500/"+movies.getResults().get(0).getPosterPath())
                .build();

        return ResponseEntity.ok(posterDto);
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteMovie() {
        movieRepository.deleteAll();
        return ResponseEntity.ok().build();
    }
}
