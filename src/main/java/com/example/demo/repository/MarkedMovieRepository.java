package com.example.demo.repository;

import com.example.demo.domain.entity.MarkedMovie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarkedMovieRepository extends JpaRepository<MarkedMovie, Long> {
}
