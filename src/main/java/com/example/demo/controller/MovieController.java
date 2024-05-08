package com.example.demo.controller;

import com.example.demo.domain.dto.*;
import com.example.demo.domain.entity.Movie;
import com.example.demo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movie")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;



    //영화 이름으로 tmdb api로부터 영화 정보를 검색

    /*
    @DeleteMapping()
    public ResponseEntity<Void> deleteMovie() {
        movieRepository.deleteAll();
        return ResponseEntity.ok().build();
    }*/

    @PostMapping("/choice")
    public ResponseEntity<Void> choiceMovie(@RequestBody MovieChoiceRequestDto dto) {
        movieService.choiceMovie(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/choice")
    public ResponseEntity<List<MoviePosterDto>> choiceMovie() {
        return ResponseEntity.ok(movieService.choiceRandomMovies());
    }
    @GetMapping("/choice/genre")
    public ResponseEntity<MovieGenreDto> choiceGenreMovie(@RequestParam(
            value = "genre", defaultValue = "action") String genre) {
        return ResponseEntity.ok(movieService.choiceRandomMovies(genre));
    }

    @GetMapping("/posters")
    public ResponseEntity<List<MoviePosterDto>> listMovies(@RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "8") int size) {
        return ResponseEntity.ok(movieService.getMovies(page, size));
    }
//필터링을 적용하지 않은 영화 리스트를 페이징을 자굥ㅇ하여
    @GetMapping("")
    public ResponseEntity<List<MoviePosterDto>> listAllMovies(@RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "8") int size) {
        return ResponseEntity.ok(movieService.getAllMovies(page, size));
    }

    @GetMapping("/genre")
    public ResponseEntity<List<MoviePosterDto>> listGenreMovies(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                @RequestParam(value = "size", defaultValue = "8") int size,
                                                                 @RequestParam(value = "genre", defaultValue = "action") String genre) {
        return ResponseEntity.ok(movieService.getGerneMovies(page, size, genre));
    }

    @GetMapping("/posters/test")
    public ResponseEntity<List<Movie>> listMoviesTest(@RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "8") int size) {
        return ResponseEntity.ok(movieService.getMoviesTest(page, size));
    }

    @PostMapping("/mark/{movieId}")
    public ResponseEntity<String> addMarkedMovie(@RequestParam(value = "movieId") Long movieId) {
        try {
            movieService.addMarkedMovie(movieId);
            return ResponseEntity.ok().body("Success to mark movie");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to mark movie.");
        }
    }

    @GetMapping("/mark")
    public ResponseEntity<List<MoviePosterDto>> getMarkedMovie() {
        return ResponseEntity.ok(movieService.getMarkedMovies());
    }

}

