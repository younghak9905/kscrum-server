package com.example.demo.controller;

import com.example.demo.service.DBupdateService;
import com.example.demo.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
@RequiredArgsConstructor
public class DBupdateController {

    private final DBupdateService dbupdateService;
@Operation(summary = "테스트 유저 데이터 넣기")
    @PostMapping("/insert/testuser")
    public void insertTestUser() {
        dbupdateService.insertTestUser();
    }

@Operation(summary = "영화 년도 업데이트")
    @PostMapping("/update/years")
    public ResponseEntity<Void> updateMovieYears() {
        dbupdateService.updateMovieYearsBatchAsync();
        return ResponseEntity.ok().build();
    }
@Operation(summary = "영화 장르 업데이트")
    @PostMapping("/update/genres")
    public ResponseEntity<Void> updateMovieGenres() {
        dbupdateService.processMoviesAsync();
        return ResponseEntity.ok().build();
    }

@Operation(summary = "영화 포스터 업데이트")
    @PostMapping("/update-posters")
    public ResponseEntity<Void> updateMoviePosters(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "1") int size) {

        dbupdateService.updateMoviePostersAsync(page, size);

        return ResponseEntity.ok().build();
    }
    @Operation(summary = "영화 포스터 db에 저장")
    @PostMapping("/save-poster")
    public ResponseEntity<Void> updateMoviePosters(@RequestParam Long movieId){


        dbupdateService.updateMoviePoster(movieId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "추천도 초기화")
    @PostMapping("/reset/prority")
    public ResponseEntity<Void> resetPriority() {
        dbupdateService.resetPriority();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "영화 제목으로 영화 찾기")
    @GetMapping("/title")
    public ResponseEntity<String> getTitle(@RequestParam String title) {
        return ResponseEntity.ok(dbupdateService.getTitleByTitle(title));
    }
    @Operation(summary = "영화 제목 업데이트")
    @PostMapping("/update/title")
    public ResponseEntity<Void> updateTitle(@RequestParam String title, @RequestParam Long movieId) {
        dbupdateService.updateTitle(title);
        return ResponseEntity.ok().build();
    }

@Operation(summary = "영화 아이디로 영화 찾기")
    @GetMapping("/movieId")
    public ResponseEntity<String> getTitle(@RequestParam Long movieId) {
        return ResponseEntity.ok(dbupdateService.getTitleByMovieId(movieId));
    }

}
