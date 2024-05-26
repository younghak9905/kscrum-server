package com.example.demo.domain.entity;

import com.example.demo.domain.dto.MovieDetailDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "trending_movies",indexes = {
        @Index(name = "idx_trending_movies_update_date", columnList = "updateDate"),
        @Index(name = "idx_trending_movies_id", columnList = "id"),
        @Index(name = "idx_trending_movies_movie_type", columnList = "movieType"),
    })
public class TrendingMovie {


    private String posterPath;

    private String originalTitle;
    private String title;
    private String releaseDate;
    private double voteAverage;
    private int runtime;
    private String genreString;
    private String tagline;
    @Column(name = "overview", length = 2000)
    private String overview;
    @Id
    private long id;
    private LocalDateTime updateDate;

    private String movieType;


    public TrendingMovie(MovieDetailDto dto)
    {
        this.posterPath = dto.getPosterPath();
        this.originalTitle = dto.getOriginalTitle();
        this.title = dto.getTitle();
        this.releaseDate = dto.getReleaseDate();
        this.voteAverage = dto.getVoteAverage();
        this.runtime = dto.getRuntime();
        this.genreString = dto.getGenreString();
        this.tagline = dto.getTagline();
        this.overview = dto.getOverview();
        this.id = dto.getId();
        this.updateDate = LocalDateTime.now();
    }

    public TrendingMovie(MovieDetailDto dto,String option)
    {
        this.posterPath = dto.getPosterPath();
        this.originalTitle = dto.getOriginalTitle();
        this.title = dto.getTitle();
        this.releaseDate = dto.getReleaseDate();
        this.voteAverage = dto.getVoteAverage();
        this.runtime = dto.getRuntime();
        this.genreString = dto.getGenreString();
        this.tagline = dto.getTagline();
        this.overview = dto.getOverview();
        this.id = dto.getId();
        this.updateDate = LocalDateTime.now();
        this.movieType = option;
    }

    public TrendingMovie() {
    }



}
