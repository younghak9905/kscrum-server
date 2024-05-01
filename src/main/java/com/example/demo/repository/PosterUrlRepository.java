package com.example.demo.repository;

import com.example.demo.domain.entity.Movie;
import com.example.demo.domain.entity.PosterUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosterUrlRepository extends JpaRepository<PosterUrl, Long> {

   @Query(value = "SELECT * FROM poster_url WHERE movie_id = :movieId", nativeQuery = true)
    Optional<PosterUrl> findByMovieId(Long movieId);
    @Query("SELECT p.posterUrl FROM PosterUrl p WHERE p.movieId = :movieId")
    Optional<String> findPosterUrlByMovieId(@Param("movieId") Long movieId);
}
