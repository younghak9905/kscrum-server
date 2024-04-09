package com.example.demo.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


import java.util.List;
@Getter
@Setter
public class MovieDto {
    private boolean adult;
    private String backdrop_path;
    @JsonProperty("genre_ids")
    private List<Integer> genreIds;
    private long id;
    @JsonProperty("original_language")
    private String originalLanguage;
    @JsonProperty("original_title")
    private String originalTitle;
    private String overview;
    private double popularity;
    @JsonProperty("poster_path")
    private String posterPath;
    private String release_date;
    private String title;
    private boolean video;
    @JsonProperty("vote_average")
    private double voteAverage;
    @JsonProperty("vote_count")
    private int voteCount;

    // Getters and Setters
    // 예시:
    public boolean isAdult() {
        return adult;
    }

    // 모든 필드에 대한 나머지 getters and setters 추가...
}
