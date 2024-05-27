package com.example.demo.controller;

import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.service.MovieLikeService;
import com.example.demo.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movie/like")
@RequiredArgsConstructor
public class MovieLikeController {
    private final MovieLikeService movieLikeService;

    private final MovieService movieService;

    @Operation(summary = "영화 좋아요")
    @PostMapping("/{movieId}")
    public ResponseEntity<Void> likeMovie(@PathVariable long movieId) {
        movieLikeService.addLikedMovie(movieId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "좋아요한 영화 가져오기")
    @GetMapping("/list")
    public ResponseEntity<List<MoviePosterDto>> listLikedMovies() {
        return ResponseEntity.ok(movieService.getLikedMovies());
    }

    @Operation(summary="좋아요 취소")
    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> unlikeMovie(@PathVariable long movieId) {
        movieLikeService.removeLikedMovie(movieId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "offset이 false인 영화가 2개 이상인지 확인")
    @GetMapping("/check-offset")
    public ResponseEntity<Boolean> checkOffset() {
        return ResponseEntity.ok(movieLikeService.checkOffset());
    }

    @Operation(summary = "check-offset이 true일때, tmdbId 리스트 가져오기")
    @GetMapping("/tmdbId-list")
    public ResponseEntity<List<Long>> tmdbIdList() {
        return ResponseEntity.ok(movieLikeService.getTmdbIdList());
    }
}
