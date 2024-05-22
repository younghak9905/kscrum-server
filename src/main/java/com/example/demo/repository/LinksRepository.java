package com.example.demo.repository;

import com.example.demo.domain.entity.Links;
import com.example.demo.domain.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface LinksRepository extends JpaRepository<Links, Long> {
    Optional<Links> findByMovieId(Movie movieId);
    @Query("SELECT l FROM Links l WHERE l.tmdbId = :tmdbId")
    Links findByTmdbId(Long tmdbId);

    List<Links> findByMovieIdIn(List<Movie> movieIds);
}
