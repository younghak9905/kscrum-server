package com.example.demo.controller;

import com.example.demo.domain.dto.*;
import com.example.demo.repository.MovieRepository;
import com.example.demo.service.MovieService;
import com.example.demo.service.TmdbClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movie")
@RequiredArgsConstructor
public class MovieController {

    private final TmdbClient tmdbClient;

    private final MovieRepository movieRepository;

    private final MovieService movieService;



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
        MoviePosterDto movies = tmdbClient. searchMoviePoster(query);
        return ResponseEntity.ok(movies);
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteMovie() {
        movieRepository.deleteAll();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update/years")
    public ResponseEntity<Void> updateMovieYears() {
        movieService.updateMovieYearsBatchAsync();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update/genres")
    public ResponseEntity<Void> updateMovieGenres() {
        movieService.processMoviesAsync();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/choice")
    public ResponseEntity<Void> choiceMovie(@RequestBody MovieChoiceRequestDto dto) {
        movieService.choiceMovie(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/choice")
    public ResponseEntity<List<MoviePosterDto>> choiceMovie() {
        return ResponseEntity.ok(movieService.choiceRandomMovies());
    }
}
