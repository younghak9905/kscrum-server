package com.example.demo.repository;

import com.example.demo.domain.entity.Genre;
import com.example.demo.domain.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
}
