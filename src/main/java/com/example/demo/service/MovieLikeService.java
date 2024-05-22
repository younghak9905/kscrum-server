package com.example.demo.service;

import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.entity.LikeMovie;
import com.example.demo.domain.entity.MarkedMovie;
import com.example.demo.domain.entity.Movie;
import com.example.demo.repository.LikeMovieRepository;
import com.example.demo.repository.MovieRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
@Service
@RequiredArgsConstructor
public class MovieLikeService {

    private final MovieRepository movieRepository;

    private final LikeMovieRepository likeMovieRepository;


    public void addLikedMovie(Long movieId) {
        Optional<Movie> findMovie = movieRepository.findByMovieId(movieId);
        if (findMovie.isPresent()) {
            try {
                LikeMovie likeddMovie = new LikeMovie();
                likeddMovie.setMovie(findMovie.get());
                likeMovieRepository.save(likeddMovie);
               // recommandService.choiceMovie(findMovie.get());

            } catch (Exception e) {
                // 저장 실패 시 예외 처리
                throw new RuntimeException("Favorites movie could not be saved.", e);
            }
        } else {
            // 영화를 찾지 못했을 때의 처리
            throw new IllegalArgumentException("Movie with ID " + movieId + " not found.");
        }
    }

    public List<Movie> getLikedMovies(){
        List<LikeMovie> movieList = likeMovieRepository.findAll();
        Set<Movie> uniqueMovies = new HashSet<>();
        for (LikeMovie likeMovie : movieList) {
            uniqueMovies.add(likeMovie.getMovie());
        }
        return new ArrayList<>(uniqueMovies);
    }

    public boolean isLikedMovie(Movie movie) {
         return likeMovieRepository.existsByMovie(movie);
    }
    @Transactional
    public void removeLikedMovie(Long movieId) {
        Optional<Movie> findMovie = movieRepository.findByMovieId(movieId);
        if (findMovie.isPresent()) {
            likeMovieRepository.deleteByMovie(findMovie.get());
        } else {
            throw new IllegalArgumentException("Movie with ID " + movieId + " not found.");
        }
    }





}
