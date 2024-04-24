package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.stereotype.Repository;
@Entity
@Getter
@Setter
@Table(name = "poster_url", indexes = {
        @Index(name = "idx_movie_id", columnList = "movieId")
})
public class PosterUrl {

    @Id
    private Long movieId;

    private String posterUrl;



    @Builder
    public PosterUrl(Long movieId, String posterUrl) {
        this.movieId = movieId;
        this.posterUrl = posterUrl;
    }

    public PosterUrl() {

    }
}

