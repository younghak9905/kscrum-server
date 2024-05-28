package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@Table(name = "selected_movies",indexes = { @Index(name = "idx_movies_id", columnList = "id"),


       })
public class SelectedMovies {
    @Id
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String genres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Movie movie;

    private String korTitle;

}
