package com.example.demo.service;

import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.entity.MarkedMovie;
import com.example.demo.domain.entity.Movie;
import com.example.demo.repository.MarkedMovieRepository;
import com.example.demo.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MovieMarkedService {

private final MovieRepository movieRepository;
private final MarkedMovieRepository markedMovieRepository;

private final MovieService movieService;


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

    public List<MoviePosterDto> getMarkedMovies(){
        List<MarkedMovie> movieList = markedMovieRepository.findAll();
        Set<Movie> uniqueMovies = new HashSet<>();
        for (MarkedMovie markedMovie : movieList) {
            uniqueMovies.add(markedMovie.getMovie());
        }
        return movieService.movieToMoviePosterDto(new ArrayList<>(uniqueMovies));
    }

    public boolean isMarkedMovie(Long movieId) {
        Optional<Movie> findMovie = movieRepository.findByMovieId(movieId);
        if (findMovie.isPresent()) {
            return markedMovieRepository.existsByMovie(findMovie.get());
        } else {
            return false;
        }
    }

}
