package com.example.demo.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieGenreDto {

    private String genre;
    private List<MoviePosterDto> movieList;

    public MovieGenreDto(String genre, List<MoviePosterDto> movieList) {
        this.genre = genre;
        this.movieList = movieList;
    }
}
