package com.example.demo.service;

import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.entity.MarkedMovie;
import com.example.demo.domain.entity.Movie;
import com.example.demo.repository.MarkedMovieRepository;
import com.example.demo.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MovieMarkedService {

private final MovieRepository movieRepository;
private final MarkedMovieRepository markedMovieRepository;



    public void addMarkedMovie(Long movieId) {
        Optional<Movie> findMovie = movieRepository.findByMovieId(movieId);
        if (findMovie.isPresent()) {
            try {
                MarkedMovie markedMovie = new MarkedMovie();
                markedMovie.setMovie(findMovie.get());
                markedMovieRepository.save(markedMovie);
            } catch (Exception e) {
                // 저장 실패 시 예외 처리
                throw new RuntimeException("Favorites movie could not be saved.", e);
            }
        } else {
            // 영화를 찾지 못했을 때의 처리
            throw new IllegalArgumentException("Movie with ID " + movieId + " not found.");
        }
    }

    public List<Movie> getMarkedMovies(){
        List<MarkedMovie> movieList = markedMovieRepository.findAll();
        List<Movie> movies = new ArrayList<>();
        for (MarkedMovie markedMovie : movieList) {
            movies.add(markedMovie.getMovie());
        }
        return movies;
    }

    public boolean isMarkedMovie(Movie movie) {
       return markedMovieRepository.existsByMovie(movie);

    }


    @Transactional
    public void removeMarkedMovie(Long movieId) {
        Optional<Movie> findMovie = movieRepository.findByMovieId(movieId);
        if (findMovie.isPresent()) {
            markedMovieRepository.deleteByMovie(findMovie.get());
        } else {
            throw new IllegalArgumentException("Movie with ID " + movieId + " not found.");
        }
    }
}
