package com.example.demo.repository;

import com.example.demo.domain.entity.MarkedMovie;
import com.example.demo.domain.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MarkedMovieRepository extends JpaRepository<MarkedMovie, Long> {

    boolean existsByMovie(Movie movie);
}
