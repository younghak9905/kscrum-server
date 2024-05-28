package com.example.demo.domain.entity;

import com.example.demo.domain.dto.MovieDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "movies",indexes = {
        @Index(name = "idx_movies_title", columnList = "title"),
        @Index(name = "idx_movies_priority", columnList = "priority"),
        @Index(name="idx_movies_update_date", columnList = "updateDate"),
        @Index(name="idx_movies_priority_update_date", columnList = "priority, updateDate"),
        @Index(name="idx_movies_kor_title", columnList = "korTitle"),
})
public class Movie {
    //현재 테이블
    @Id
    private Long movieId;
    private String title;
    private String korTitle;
    private String genres;

    //추가할 컬럼
    private String year;
    private Long priority;
    private Long userScore;

    @OneToMany(mappedBy = "movie")
    private List<MovieGenre> movieGenres;

    private LocalDateTime updateDate;

    private String posterUrl;

    public Movie() {
    }

    public Movie(MovieDto dto,Long id)
    {
        this.movieId = id;
        this.title = dto.getOriginalTitle();
        this.posterUrl = dto.getPosterPath();
        this.year = dto.getReleaseDate().substring(0,4);
        this.korTitle = dto.getTitle();
    }


}

