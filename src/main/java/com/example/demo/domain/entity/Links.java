package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "links",indexes = {
        @Index(name = "idx_tmdb_link", columnList = "tmdbId"),
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
