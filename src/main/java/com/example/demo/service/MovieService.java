package com.example.demo.service;

import com.example.demo.domain.entity.Movie;
import com.example.demo.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;

    public Movie getMovieByMovieId(Long movieId)
    {
        return movieRepository.findByMovieId(movieId).orElse(null);
    }
}
