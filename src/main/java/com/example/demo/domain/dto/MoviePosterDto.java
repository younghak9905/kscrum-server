package com.example.demo.domain.dto;

import com.example.demo.domain.entity.Movie;
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

    private boolean isMarked;

    private boolean isLiked;


    @Builder
    public MoviePosterDto(long movieId, String title, String posterPath,String url) {
        this.movieId = movieId;
        this.title = title;
        this.posterPath = posterPath;
        this.url = url;
    }

    public MoviePosterDto(Movie movie, String posterPath) {
        this.movieId = movie.getMovieId();
        this.title = movie.getTitle();
        this.posterPath = posterPath;
        this.url = "https://image.tmdb.org/t/p/w500"+posterPath;

    }

    public MoviePosterDto(Movie movie) {
        this.movieId = movie.getMovieId();
        this.title = movie.getTitle();
        this.posterPath = movie.getPosterUrl();
        this.url = "https://image.tmdb.org/t/p/w500"+movie.getPosterUrl();

    }

    public MoviePosterDto(Movie movie, String posterPath,boolean isMarked,boolean isLiked) {
        this.movieId = movie.getMovieId();
        this.title = movie.getTitle();
        this.posterPath = posterPath;
        this.url = "https://image.tmdb.org/t/p/w500"+posterPath;
        this.isMarked = isMarked;
        this.isLiked = isLiked;

    }
}
