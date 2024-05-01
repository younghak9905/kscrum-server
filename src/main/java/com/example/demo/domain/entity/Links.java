package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "links",indexes = {
        @Index(name = "idx_movie_id", columnList = "movie_id"), // 'movie_id' 컬럼에 대한 인덱스
        @Index(name = "idx_tmdb_id", columnList = "tmdbId") // 'tmdbId' 컬럼에 대한 인덱스
})
public class Links {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long linkId;

    @ManyToOne
    @JoinColumn(name = "movie_id") // 데이터베이스의 실제 컬럼 이름과 일치시켜야 합니다.
    private Movie movieId;

    private Long tmdbId;
    private Long imdbId;
}
