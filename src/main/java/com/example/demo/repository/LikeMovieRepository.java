package com.example.demo.repository;

import com.example.demo.domain.entity.LikeMovie;
import com.example.demo.domain.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeMovieRepository extends JpaRepository<LikeMovie, Long> {


    @Query(value = "SELECT EXISTS (SELECT 1 FROM like_movie WHERE movie_id = :movieId)", nativeQuery = true)
    int existsByMovie(@Param("movieId") Long movieId);

    void deleteByMovie(Movie movie);


    boolean existsByMovie(Movie movie);
}
