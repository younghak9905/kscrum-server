package com.example.demo.repository;

import com.example.demo.domain.entity.LikeMovie;
import com.example.demo.domain.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeMovieRepository extends JpaRepository<LikeMovie, Long> {
    boolean existsByMovie(Movie movie);

    void deleteByMovie(Movie movie);
}
