package com.example.demo.controller;

import com.example.demo.service.DBupdateService;
import lombok.RequiredArgsConstructor;
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
}
