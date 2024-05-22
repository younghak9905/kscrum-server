package com.example.demo.controller;

import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.service.MovieLikeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movie/like")
@RequiredArgsConstructor
public class MovieLikeController {
    private final MovieLikeService movieMarkedService;

    @Operation(summary = "영화 좋아요")
    @PostMapping("/{movieId}")
    public ResponseEntity<Void> likeMovie(@PathVariable long movieId) {
        movieMarkedService.addLikedMovie(movieId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "좋아요한 영화 가져오기")
    @GetMapping("/list")
    public ResponseEntity<List<MoviePosterDto>> listLikedMovies() {
        return ResponseEntity.ok(movieMarkedService.getLikedMovies());
    }

    @Operation(summary="좋아요 취소")
    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> unlikeMovie(@PathVariable long movieId) {
        movieMarkedService.removeLikedMovie(movieId);
        return ResponseEntity.ok().build();
    }
}
