package com.example.demo.controller;

import com.example.demo.service.DBupdateService;
import com.example.demo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/db")
@RequiredArgsConstructor
public class DBupdateController {

    private final DBupdateService dbupdateService;


    @PostMapping("/insert/testuser")
    public void insertTestUser() {
        dbupdateService.insertTestUser();
    }


    @PostMapping("/update/years")
    public ResponseEntity<Void> updateMovieYears() {
        dbupdateService.updateMovieYearsBatchAsync();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update/genres")
    public ResponseEntity<Void> updateMovieGenres() {
        dbupdateService.processMoviesAsync();
        return ResponseEntity.ok().build();
    }


    @PostMapping("/update-posters")
    public ResponseEntity<Void> updateMoviePosters(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "1") int size) {

        dbupdateService.updateMoviePostersAsync(page, size);

        return ResponseEntity.ok().build();
    }
    @PostMapping("/save-poster")
    public ResponseEntity<Void> updateMoviePosters(@RequestParam Long movieId){


        dbupdateService.updateMoviePoster(movieId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset/prority")
    public ResponseEntity<Void> resetPriority() {
        dbupdateService.resetPriority();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/title")
    public ResponseEntity<String> getTitle(@RequestParam String title) {
        return ResponseEntity.ok(dbupdateService.getTitleByTitle(title));
    }
    @PostMapping("/update/title")
    public ResponseEntity<Void> updateTitle(@RequestParam String title, @RequestParam Long movieId) {
        dbupdateService.updateTitle(title);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/movieId")
    public ResponseEntity<String> getTitle(@RequestParam Long movieId) {
        return ResponseEntity.ok(dbupdateService.getTitleByMovieId(movieId));
    }

}
