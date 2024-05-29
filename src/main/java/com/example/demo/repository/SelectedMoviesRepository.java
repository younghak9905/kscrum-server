package com.example.demo.repository;

import com.example.demo.domain.entity.Movie;
import com.example.demo.domain.entity.SelectedMovies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelectedMoviesRepository extends JpaRepository<SelectedMovies, Long> {

    @Query(value = "SELECT * FROM (SELECT * FROM selected_movies WHERE genres LIKE %:genre% AND movie_id is not null ORDER BY RAND() LIMIT 4) AS subquery", nativeQuery = true)
    List<SelectedMovies> findRandomMoviesByRomance(String genre);



    Page<SelectedMovies> findAll(Pageable pageable);

    @Query("SELECT sm.id FROM SelectedMovies sm GROUP BY sm.id HAVING COUNT(sm.id) > 1")
    List<Long> findDuplicateIds();

    List<SelectedMovies> findAllById(Long id);

    boolean existsByMovie(Movie movie);
}
