package com.example.demo.service;

import com.example.demo.domain.dto.MovieChoiceRequestDto;
import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;


    private final LinksRepository linksRepository;


    private final MovieGenreRepository movieGenreRepository;

    private final WebClient webClient;

    private final TmdbClient tmdbClient;

    private final PosterUrlRepository posterUrlRepository;


    public Movie getMovieByMovieId(Long movieId) {
        return movieRepository.findByMovieId(movieId).orElse(null);
    }


    public void choiceMovie(MovieChoiceRequestDto dto) {
        List<Long> movieIds = dto.getMovieIds();
        List<Movie> movies = movieRepository.findByMovieIdIn(movieIds);
        List<String> movieTitles = new ArrayList<>();
        for (Movie movie : movies) {
            movieTitles.add(movie.getTitle());
        }

        getRecommendationsAsync(movieTitles).thenAccept(recommendations -> {
            System.out.println("Recommendations: " + recommendations);

            // 비동기적으로 받은 추천 영화 처리
            updateMoviePriorities(recommendations);
        });
    }


    @Async
    public CompletableFuture<List<String>> getRecommendationsAsync(List<String> movieTitles) {
        String url = "/api/recommendation";
        System.out.println("Calling API URL: " + url);

        return webClient.post()
                .uri(url)
                .bodyValue(movieTitles)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("API call failed with status: " + response.statusCode()))
                )
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                })
                .doOnError(error -> System.out.println("API call error: " + error.getMessage()))
                .toFuture();
    }

    @Async
    public void updateMoviePriorities(List<String> recommendations) {
        List<Movie> movies = movieRepository.findByTitleIn(recommendations);
        movies.forEach(movie -> {
            movie.setPriority(movie.getPriority() == null ? 1 : movie.getPriority() + 1);
            movie.setUpdateDate(LocalDateTime.now());
            System.out.println("Movie: " + movie.getTitle() + ", Priority: " + movie.getPriority());
        });
        movieRepository.saveAll(movies);

    }

    public List<MoviePosterDto> choiceRandomMovies() {
        //List<Movie> randomMovies = movieRepository.findRandomMovie();

        List<MoviePosterDto> moviePosterDtos = new ArrayList<>();
        List<Genre> movieGenres = findAllGenres();

        for (Genre genre : movieGenres) {
            List<Movie> randomMovies = movieRepository.findRandomMoviesByGenre(genre.getGenreName());
            moviePosterDtos.addAll(movieToMoviePosterDto(randomMovies));
        }

        return moviePosterDtos;
    }

    public List<Genre> findAllGenres() {
        List<MovieGenre> movieGenres = movieGenreRepository.findAll();
        return movieGenres.stream()
                .map(MovieGenre::getGenre)
                .distinct()
                .limit(4)
                .collect(Collectors.toList());
    }


    public Page<Movie> getMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    //추천 알고리즘에 의한 영화 추천 리스트 반환
    public List<MoviePosterDto> getMovies(int pageNumber, int pageSize) {
        long startTime = System.currentTimeMillis();

        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviesPage = movieRepository.findAllSortedByPriorityAndUpdateDate(pageable);
        System.out.println("Page retrieval time: " + (System.currentTimeMillis() - startTime) + " ms");

        startTime = System.currentTimeMillis();
        List<MoviePosterDto> moviePosterDtos = movieToMoviePosterDto(moviesPage.getContent());
        System.out.println("Poster DTO conversion time: " + (System.currentTimeMillis() - startTime) + " ms");

        return moviePosterDtos;
    }


    public List<MoviePosterDto> getAllMovies(int pageNumber, int pageSize) {
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        List<MoviePosterDto> moviePosterDtos = movieToMoviePosterDto(getMovies(pageable).getContent());
        return moviePosterDtos;
    }


    public List<MoviePosterDto> movieToMoviePosterDto(List<Movie> movieList) {
        long startTime = System.currentTimeMillis();
        List<MoviePosterDto> result = movieList.stream()
                .map(this::createMoviePosterDto)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        System.out.println("Movie to Poster DTO processing time: " + (System.currentTimeMillis() - startTime) + " ms");
        return result;
    }

    private MoviePosterDto createMoviePosterDto(Movie movie) {
        long startTime = System.currentTimeMillis();
        String posterUrl = getPosterUrl(movie);
        System.out.println("Create MoviePosterDto time: " + (System.currentTimeMillis() - startTime) + " ms");
        return new MoviePosterDto(movie, posterUrl);
    }

    private String fetchAndSavePosterUrl(Movie movie) {
        Long tmdbId = getTmdbId(movie);
        String posterUrl = tmdbClient.getPosterUrl(tmdbId);
        System.out.println("Fetched poster URL: " + posterUrl);
        movie.setPosterUrl(posterUrl); // Save to entity (not saved to DB yet
        movieRepository.save(movie); // Save to DB
       //  savePosterUrl(movie, posterUrl); // Save to repository
        return posterUrl;
    }

    private void savePosterUrl(Movie movie, String posterUrl) {
        PosterUrl newPoster = new PosterUrl();
        newPoster.setMovieId(movie.getMovieId());
        newPoster.setPosterUrl(posterUrl);
        posterUrlRepository.save(newPoster); // Assuming this method exists
    }

    //  @Cacheable(value = "movieCache", key = "#movie.movieId")
    public Long getTmdbId(Movie movie) {
        long startTime = System.currentTimeMillis();
        Optional<Links> link = linksRepository.findByMovieId(movie);
        if (link.isPresent()) {
            System.out.println("findTmdbId: " + (System.currentTimeMillis() - startTime) + " ms");
            return link.get().getTmdbId();
        }
        return null;
    }

    //  @Cacheable(value = "posterUrls", key = "#movie.movieId")
    public String getPosterUrl(Movie movie) {
        if(movie.getPosterUrl() != null) {
            return movie.getPosterUrl();
        }
        return fetchAndSavePosterUrl(movie);
    }


    public List<Movie> getMoviesTest(int pageNumber, int pageSize) {
        long startTime = System.currentTimeMillis();

        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviesPage = movieRepository.findAllSortedByPriorityAndUpdateDate(pageable);
        System.out.println("Poster DTO conversion time: " + (System.currentTimeMillis() - startTime) + " ms");

        return moviesPage.getContent();


    }
}

