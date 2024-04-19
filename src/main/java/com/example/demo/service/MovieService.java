package com.example.demo.service;

import com.example.demo.domain.dto.MovieChoiceRequestDto;
import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.entity.Genre;
import com.example.demo.domain.entity.Links;
import com.example.demo.domain.entity.Movie;
import com.example.demo.domain.entity.MovieGenre;
import com.example.demo.repository.GenreRepository;
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

    private final GenreRepository genreRepository;

    private final MovieGenreRepository movieGenreRepository;

    private final LinksRepository linksRepository;

    private final WebClient webClient;

    private final TmdbClient tmdbClient;


    public Movie getMovieByMovieId(Long movieId) {
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

        for (int page = 50; page < totalPages; page++) {
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
            Long tmdbId = linksRepository.findTmdbIdByMovieId(movie).get(0).getTmdbId();
            System.out.println("tmdbId = " + tmdbId);
            MoviePosterDto moviePosterDto = tmdbClient.searchMoviePoster(Long.toString(tmdbId));
            moviePosterDtos.add(moviePosterDto);
        }
        return moviePosterDtos;
    }


    public Page<Movie> getMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    public List<MoviePosterDto> getMovies(int pageNumber, int pageSize) {
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviesPage = movieRepository.findAllSortedByPriorityAndUpdateDate(pageable);

        List<MoviePosterDto> moviePosterDtos = moviesPage.getContent().stream()
                // getTmdbId(movie)가 null이 아닌 경우에만 처리
                .filter(movie -> getTmdbId(movie) != null)
                .map(movie -> tmdbClient.searchMoviePoster(getTmdbId(movie)))
                .collect(Collectors.toList());

        // PageImpl을 사용하여 Page<MoviePosterDto> 객체 생성
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


