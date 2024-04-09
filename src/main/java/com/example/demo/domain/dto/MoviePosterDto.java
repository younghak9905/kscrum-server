package com.example.demo.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoviePosterDto {
    @JsonProperty("id")
    private long movieId;
    @JsonProperty("original_title")
    private String title;
    @JsonProperty("poster_path")
    private String posterPath;

    private String url;
    @Builder
    public MoviePosterDto(long movieId, String title, String posterPath,String url) {
        this.movieId = movieId;
        this.title = title;
        this.posterPath = posterPath;
        this.url = url;
    }
}
