package com.example.demo.service;

import com.example.demo.domain.dto.MovieChoiceRequestDto;
import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.entity.Genre;
import com.example.demo.domain.entity.Links;
import com.example.demo.domain.entity.Movie;
import com.example.demo.domain.entity.MovieGenre;
import com.example.demo.repository.GenreRepository;
import com.example.demo.repository.LinksRepository;
import com.example.demo.repository.MovieGenreRepository;
import com.example.demo.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
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

    private final WebClient webClient;

    private final TmdbClient tmdbClient;


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
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
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
        List<Movie> randomMovies = movieRepository.findRandomMovie();

        List<MoviePosterDto> moviePosterDtos = new ArrayList<>();
        for(Movie movie : randomMovies) {
            Long tmdbId =getTmdbId(movie);
            System.out.println("tmdbId = " + tmdbId);
            MoviePosterDto moviePosterDto = tmdbClient.searchMoviePoster(tmdbId);
            moviePosterDtos.add(moviePosterDto);
        }
        return moviePosterDtos;
    }


    public Page<Movie> getMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    //추천 알고리즘에 의한 영화 추천 리스트 반환
    public List<MoviePosterDto> getMovies(int pageNumber, int pageSize) {
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviesPage = movieRepository.findAllSortedByPriorityAndUpdateDate(pageable);
        List<MoviePosterDto> moviePosterDtos = movieToMoviePosterDto(moviesPage.getContent());
        // PageImpl을 사용하여 Page<MoviePosterDto> 객체 생성
        return moviePosterDtos;
    }

    public List<MoviePosterDto> getAllMovies(int pageNumber, int pageSize) {
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        List<MoviePosterDto> moviePosterDtos = movieToMoviePosterDto(getMovies(pageable).getContent());
        return moviePosterDtos;
    }

    public List<MoviePosterDto> movieToMoviePosterDto(List<Movie> movieList) {

        List<MoviePosterDto> moviePosterDtos = movieList.stream()
                // getTmdbId(movie)가 null이 아닌 경우에만 처리
                .filter(movie -> getTmdbId(movie) != null)
                .map(movie -> tmdbClient.searchMoviePoster(getTmdbId(movie)))
                .collect(Collectors.toList());
        return moviePosterDtos;

    }



    public Long getTmdbId (Movie movie)
    {
        Optional<Links> link=linksRepository.findByMovieId(movie);
        if(link.isPresent())
        {
            return link.get().getTmdbId();
        }
        return null;
    }
}


