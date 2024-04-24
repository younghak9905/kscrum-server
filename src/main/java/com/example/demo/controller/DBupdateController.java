package com.example.demo.controller;

import com.example.demo.service.DBupdateService;
import com.example.demo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
