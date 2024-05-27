package com.example.demo.controller;

import com.example.demo.domain.dto.*;
import com.example.demo.domain.entity.Movie;
import com.example.demo.service.MovieMarkedService;
import com.example.demo.service.MovieService;
import com.example.demo.service.RecommandService;
import com.example.demo.task.MovieUpdateTask;
import io.swagger.v3.oas.annotations.Operation;
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

    private final RecommandService recommandService;

    private final MovieMarkedService movieMarkedService;

    private final MovieUpdateTask movieUpdateTask;



    //영화 이름으로 tmdb api로부터 영화 정보를 검색

    /*
    @DeleteMapping()
    public ResponseEntity<Void> deleteMovie() {
        movieRepository.deleteAll();
        return ResponseEntity.ok().build();
    }*/
@Operation(summary = "취향 영화 선택")
    @PostMapping("/choice")
    public ResponseEntity<Void> choiceMovie(@RequestBody MovieChoiceRequestDto dto) {
        recommandService.choiceMovie(dto);
        return ResponseEntity.ok().build();
    }
@Operation(summary = "취향 영화 가져오기-장르별로 검색오는 API로 대체됨")
    @GetMapping("/choice")
    public ResponseEntity<List<MoviePosterDto>> choiceMovie() {
        return ResponseEntity.ok(movieService.choiceRandomMovies());
    }
    @Operation(summary = "취향 영화 가져오기-장르별로")
    @GetMapping("/choice/genre")
    public ResponseEntity<MovieGenreDto> choiceGenreMovie(@RequestParam(
            value = "genre", defaultValue = "action") String genre) {
        return ResponseEntity.ok(movieService.choiceRandomMovies(genre));
    }
    @Operation(summary = "영화 피드 정보 가져오기")
    @GetMapping("/posters")
    public ResponseEntity<List<MoviePosterDto>> listMovies(@RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "8") int size) {
        return ResponseEntity.ok(movieService.getMovies(page, size));
    }

    @Operation(summary = "영화 상세 정보 가져오기")
//필터링을 적용하지 않은 영화 리스트를 페이징을 자굥ㅇ하여
    @GetMapping("")
    public ResponseEntity<List<MoviePosterDto>> listAllMovies(@RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "8") int size) {
        return ResponseEntity.ok(movieService.getAllMovies(page, size));
    }
    @Operation(summary = "영화 장르에 해당하는 영화")
    @GetMapping("/genre")
    public ResponseEntity<List<MoviePosterDto>> listGenreMovies(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                @RequestParam(value = "size", defaultValue = "8") int size,
                                                                 @RequestParam(value = "genre", defaultValue = "action") String genre) {
        return ResponseEntity.ok(movieService.getGenreMovies(page, size, genre));
    }

    @GetMapping("/posters/test")
    public ResponseEntity<List<Movie>> listMoviesTest(@RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "8") int size) {
        return ResponseEntity.ok(movieService.getMoviesTest(page, size));
    }
@Operation(summary = "나중에 볼 영화 등록")
    @PostMapping("/mark/{movieId}")
    public ResponseEntity<String> addMarkedMovie(@RequestParam(value = "movieId") Long movieId) {
        try {
            movieMarkedService.addMarkedMovie(movieId);
            return ResponseEntity.ok().body("Success to mark movie");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to mark movie.");
        }
    }
    @Operation(summary = "나중에 볼 영화 가져오기")
    @GetMapping("/mark")
    public ResponseEntity<List<MoviePosterDto>> getMarkedMovie() {
        return ResponseEntity.ok(movieService.getMarkedMovies());
    }

    @Operation(summary = "나중에 볼 영화 삭제")
    @DeleteMapping("/mark/{movieId}")
    public ResponseEntity<Void> removeMarkedMovie(@RequestParam(value = "movieId") Long movieId) {
        movieMarkedService.removeMarkedMovie(movieId);
        return ResponseEntity.ok().build();
    }
@Operation(summary = "검색")
    @GetMapping("/search/{type}")
    public ResponseEntity<List<MoviePosterDto>> searchMovie(@PathVariable(required = false) String type,
                                                            @RequestParam(value = "page", defaultValue = "0") int page,
                                                            @RequestParam(value = "size", defaultValue = "8") int size,
                                                            @RequestParam(value = "filterType", defaultValue = "year") String filterType,
                                                            @RequestParam(value = "ordering", defaultValue = "desc") String ordering,
                                                            @RequestParam(value = "keyword") String keyword) {

    movieService.validateKeyword(keyword);
    return ResponseEntity.ok(movieService.search(page, size, type, keyword, filterType, ordering));
    }


    @Operation(summary = "취향 영화 선택")
    @PostMapping("/choice/test")
    public ResponseEntity<Void> choiceMovieTest(@RequestBody MovieChoiceRequestDto dto) {
        recommandService.choiceMovie(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "영화 상세보기")
    @GetMapping("/details")
    public ResponseEntity<MovieDetailDto> getMovieDetails(@RequestParam Long movieId) {
        System.out.println("movieId: " + movieId);
        MovieDetailDto movieDetailDto = movieService.getMovieDetails(movieId);
        System.out.println("movieDto: " + movieDetailDto.getTitle());
        return ResponseEntity.ok(movieDetailDto);
    }

    @Operation(summary = "영화 상세보기 in DB")
    @GetMapping("/details/db/{movieId}")
    public ResponseEntity<MoviePosterDto> getMovieDetailsInDB(@PathVariable Long movieId) {
        System.out.println("movieId: " + movieId);
        MoviePosterDto movieDetailDto = movieService.getMovieDetailsInDB(movieId);
        System.out.println("movieDto: " + movieDetailDto.getTitle());
        return ResponseEntity.ok(movieDetailDto);
    }


    @Operation(summary = "현재 상영중인 영화 목록")
    @GetMapping("/now_playing")
    public ResponseEntity<List<MovieDetailDto>> nowPlayingMovie(@RequestParam(value = "tmdbPage", defaultValue = "1") int tmdbPage,
                                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                                @RequestParam(value = "size", defaultValue = "8") int size
                                                                ){
        return ResponseEntity.ok(movieService.getPlayingMovie(tmdbPage, page, size));
    }

    @Operation(summary = "주차별 인기 영화 목록")
    @GetMapping("/trending_week")
    public ResponseEntity<List<MovieDetailDto>> trendingWeekMovie(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "size", defaultValue = "8") int size){
        return ResponseEntity.ok(movieService.getTrendingMovie("week", page, size));
    }

    @Operation(summary = "일별 인기 영화 목록")
    @GetMapping("/trending_day")
    public ResponseEntity<List<MovieDetailDto>> trendingDayMovie(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                 @RequestParam(value = "size", defaultValue = "8") int size){
        return ResponseEntity.ok(movieService.getTrendingMovie("day",page, size));
    }

    @Operation(summary = "explore 영화 업데이트")
    @PostMapping("/explore/update")
    public ResponseEntity<Void> updateExploreMovies() {
        movieUpdateTask.updateMovies();
        return ResponseEntity.ok().build();
    }




}

