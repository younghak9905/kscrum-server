package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "movies",indexes = {
        @Index(name = "idx_movies_title", columnList = "title")
})
public class Movie {
    //현재 테이블
    @Id
    private Long movieId;
    private String title;
    private String genres;

    //추가할 컬럼
    private String year;
    private Long score;
    private Long userScore;

    @OneToMany(mappedBy = "movie")
    private List<MovieGenre> movieGenres;


}

