package com.example.demo.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class MovieResponseDto {
    private int page;
    private List<MovieDto> results;
    private int total_pages;
    private int total_results;

    // Getters and Setters

}