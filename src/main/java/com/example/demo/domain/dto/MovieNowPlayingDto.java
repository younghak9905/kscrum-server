package com.example.demo.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class MovieNowPlayingDto {
    private long id;
    @JsonProperty(value = "original_title")
    private String originalTitle;
    @JsonProperty(value = "genre_ids")
    private List<Integer> genreIds;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("release_date")
    private String releaseDate;
    @JsonProperty(value = "vote_average")
    private double voteAverage;
}
