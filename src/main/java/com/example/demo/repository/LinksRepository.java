package com.example.demo.repository;

import com.example.demo.domain.entity.Links;
import com.example.demo.domain.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface LinksRepository extends JpaRepository<Links, Long> {
    List<Links> findTmdbIdByMovieId (Movie movie);
}
