package com.example.demo.controller;

import com.example.demo.domain.dto.MovieChoiceRequestDto;
import com.example.demo.domain.dto.MovieDto;
import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.dto.MovieResponseDto;
import com.example.demo.domain.entity.Movie;
import com.example.demo.repository.MovieRepository;
import com.example.demo.service.MovieService;
import com.example.demo.service.TmdbClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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


    //영화 이름으로 tmdb api로부터 영화 정보를 검색
    @GetMapping("/search")
    public ResponseEntity<MovieResponseDto> searchMovies(@RequestParam String query) {
        return ResponseEntity.ok(tmdbClient.searchMovies(query));
    }
    //tmdbID를 이용하여 영화 정보를 검색
    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovieDetails(@PathVariable Long movieId) {
        return ResponseEntity.ok(tmdbClient.getMovieDetails(movieId));
    }
    //영화 이름으로 tmdb api로부터 영화 포스터 정보를 검색
    @GetMapping("/poster")
    public ResponseEntity<MoviePosterDto> getMoviePoster(@RequestParam String query) {
        MoviePosterDto movies = tmdbClient. searchMoviePoster(query);
        return ResponseEntity.ok(movies);
    }
    /*
    @DeleteMapping()
    public ResponseEntity<Void> deleteMovie() {
        movieRepository.deleteAll();
        return ResponseEntity.ok().build();
    }*/

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

    @GetMapping("/posters")
    public ResponseEntity<List<MoviePosterDto>> listMovies(@RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(movieService.getMovies(page, size));
    }
//필터링을 적용하지 않은 영화 리스트를 페이징을 자굥ㅇ하여
    @GetMapping("/all")
    public ResponseEntity<List<MoviePosterDto>> listAllMovies(@RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(movieService.getAllMovies(page, size));
    }
}

