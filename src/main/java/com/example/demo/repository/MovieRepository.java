package com.example.demo.repository;

import com.example.demo.domain.entity.Genre;
import com.example.demo.domain.entity.Movie;
import com.example.demo.domain.entity.MovieGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {


    Optional<Movie> findByMovieId(Long movieId);



    @Query(value = "SELECT * FROM movies m LIMIT :batchSize OFFSET :offset", nativeQuery = true)
    List<Movie> findMoviesWithLimitOffset(@Param("batchSize") int batchSize, @Param("offset") int offset);

    List<Movie> findByMovieIdIn(List<Long> movieIds);

    List<Movie> findByTitleIn(List<String> recommendations);


    Page<Movie> findAll(Pageable pageable);


    @Query("SELECT m FROM Movie m ORDER BY m.priority DESC NULLS LAST, m.updateDate DESC NULLS LAST")
    List<Movie> findAllSortedByPriorityAndUpdateDate();

    @Query("SELECT m FROM Movie m ORDER BY m.priority DESC NULLS LAST, m.updateDate DESC NULLS LAST")
    Page<Movie> findAllSortedByPriorityAndUpdateDate(Pageable pageable);

    @Query(value = "SELECT m FROM Movie m ORDER BY RAND() LIMIT 4", nativeQuery = true)
    List<Movie> findRandomMovie();

    @Query(value = "SELECT * FROM (SELECT * FROM movies WHERE genres = :genre AND year >= 2013 ORDER BY RAND() LIMIT 4) AS subquery", nativeQuery = true)
    List<Movie> findRandomMoviesByGenre(@Param("genre") String genre);

    @Query(value = "SELECT * FROM (SELECT * FROM movies WHERE genres = :genre ORDER BY RAND() LIMIT 4) AS subquery", nativeQuery = true)
    List<Movie> findRandomMoviesByRomance(@Param("genre") String genre);
}
