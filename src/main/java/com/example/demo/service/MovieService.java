package com.example.demo.service;

import com.example.demo.domain.entity.Genre;
import com.example.demo.domain.entity.Movie;
import com.example.demo.domain.entity.MovieGenre;
import com.example.demo.repository.GenreRepository;
import com.example.demo.repository.MovieGenreRepository;
import com.example.demo.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;

    private final GenreRepository genreRepository;

    private final MovieGenreRepository movieGenreRepository;

    public Movie getMovieByMovieId(Long movieId)
    {
        return movieRepository.findByMovieId(movieId).orElse(null);
    }





    public void updateMovieYearsBatchAsync() {
        int batchSize = 100;
        long totalMovies = movieRepository.count();
        int pages = (int) Math.ceil((double) totalMovies / batchSize);
        System.out.println("totalMovies: " + totalMovies + ", pages: " + pages);

        for (int i = 0; i < pages; i++) {
            final int page = i;
            CompletableFuture.runAsync(() -> updateMovieYearsBatch(page, batchSize));
        }
    }

    @Transactional
    public void updateMovieYearsBatch(int page, int batchSize) {
        int offset = page * batchSize;
        List<Movie> movies = movieRepository.findMoviesWithLimitOffset(batchSize, offset);
        Pattern pattern = Pattern.compile("\\((\\d{4})\\)$");

        for (Movie movie : movies) {
            Matcher matcher = pattern.matcher(movie.getTitle());
            if (matcher.find()) {
                String year = matcher.group(1);
                movie.setYear(year);
            }
        }
        System.out.println("page: " + page + ", offset: " + offset + ", movies: " + movies.size());
        movieRepository.saveAll(movies);
    }



    @Async
    public CompletableFuture<Void> processMoviesAsync() {
        int pageSize = 100;
        long totalMovies = movieRepository.count();
        int totalPages = (int) Math.ceil((double) totalMovies / pageSize);

        for (int page = 0; page < totalPages; page++) {
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<Movie> moviePage = movieRepository.findAll(pageable);

            // 각 영화에 대한 처리 로직을 수행합니다.
            extractAndSaveGenres(moviePage); // 변경된 메서드 호출
            setupMovieGenreRelationships(moviePage); // 변경된 메서드 호출

            // 진행 상황 로깅
            System.out.println("Page: " + (page + 1) + ", Offset: " + pageable.getOffset() + ", Movies: " + moviePage.getContent().size());
        }

        return CompletableFuture.completedFuture(null);
    }


    @Transactional
    public void extractAndSaveGenres(Page<Movie> moviePage) {
        HashSet<String> uniqueGenres = new HashSet<>();

        moviePage.forEach(movie -> {
            Arrays.stream(movie.getGenres().split("\\|")).forEach(uniqueGenres::add);
        });

        uniqueGenres.forEach(genreName -> {
            if (!genreRepository.existsByGenreName(genreName)) {
                Genre genre = new Genre();
                genre.setGenreName(genreName);
                genreRepository.save(genre);
            }
        });
    }

    @Transactional
    public void setupMovieGenreRelationships(Page<Movie> moviePage) {
        List<MovieGenre> movieGenres = new ArrayList<>();

        moviePage.forEach(movie -> {
            Arrays.stream(movie.getGenres().split("\\|")).forEach(genreName -> {
                Genre genre = genreRepository.findByGenreName(genreName);
                if (genre != null) {
                    MovieGenre movieGenre = new MovieGenre();
                    movieGenre.setMovie(movie);
                    movieGenre.setGenre(genre);
                    movieGenres.add(movieGenre);
                }
            });
        });

        movieGenreRepository.saveAll(movieGenres);
    }
}



