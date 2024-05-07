package com.example.demo.service;

import com.example.demo.domain.dto.MovieChoiceRequestDto;
import com.example.demo.domain.dto.MovieGenreDto;
import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.dto.MovieRecommendDto;
import com.example.demo.domain.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
            String titleWithoutYear = movie.getTitle().replaceAll("\\s*\\(\\d{4}\\)$", "");
            movieTitles.add(titleWithoutYear);
        }
        System.out.println("Movie titles: " + movieTitles);

        getRecommendationsAsync(movieTitles).thenAccept(recommendations -> {
            System.out.println("Recommendations: " + recommendations);
            List<String> movieTitlesToSave = recommendations.stream()
                    .map(MovieRecommendDto::getTitle)
                    .collect(Collectors.toList());
            // 비동기적으로 받은 추천 영화 처리
            updateMoviePriorities(movieTitlesToSave);
            System.out.println("Movie priorities updated");
        });
    }


    @Async
    public CompletableFuture<List<MovieRecommendDto>> getRecommendationsAsync(List<String> movieTitles) {
        String url = "http://4.246.130.162:2222/recommendations";

        // 쿼리 파라미터로 영화 제목 목록을 추가
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("movie_titles", String.join(",", movieTitles)); // 리스트를 콤마로 구분된 문자열로 변환
        System.out.println("URL: " + uriBuilder.toUriString());
        return webClient.get() // GET 메서드 사용
                .uri(uriBuilder.build().encode().toUri()) // URI에 쿼리 파라미터 포함시키고, URL 인코딩 수행
                .retrieve() // 응답 본문을 검색
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("API call failed with status: " + response.statusCode()))
                )
                .bodyToMono(new ParameterizedTypeReference<List<MovieRecommendDto>>() {}) // 응답 본문을 List<String>으로 변환
                .doOnError(error -> System.out.println("API call error: " + error.getMessage())) // 에러 발생 시 로깅
                .toFuture(); // CompletableFuture로 변환
    }


    @Async
    public void updateMoviePriorities(List<String> recommendations) {
        System.out.println(recommendations);
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
//        List<Genre> movieGenres = findAllGenres();
        List<String> movieGenres = new ArrayList<String>(Arrays.asList("Drama", "Thriller", "Action", "Animation", "Romance", "Comedy"));

        for (String genre : movieGenres) {
            System.out.println("Genre: " + genre);
            if(genre.equals("Romance")){ // 로맨스는 영화가 별로 없어서 년도 기준 뺐습니다.
                List<Movie> randomMovies = movieRepository.findRandomMoviesByRomance(genre);
                moviePosterDtos.addAll(movieToMoviePosterDto(randomMovies));
            } else {
                List<Movie> randomMovies = movieRepository.findRandomMoviesByGenre(genre);
                moviePosterDtos.addAll(movieToMoviePosterDto(randomMovies));
            }
        }

        return moviePosterDtos;
    }

    public MovieGenreDto choiceRandomMovies(String genre) {
        //List<Movie> randomMovies = movieRepository.findRandomMovie();

        List<MoviePosterDto> moviePosterDtos = new ArrayList<>();
//        List<Genre> movieGenres = findAllGenres();

            System.out.println("Genre: " + genre);
            if(genre.equals("Romance")){ // 로맨스는 영화가 별로 없어서 년도 기준 뺐습니다.
                List<Movie> randomMovies = movieRepository.findRandomMoviesByRomance(genre);
                moviePosterDtos.addAll(movieToMoviePosterDto(randomMovies));
            } else {
                List<Movie> randomMovies = movieRepository.findRandomMoviesByGenre(genre);
                moviePosterDtos.addAll(movieToMoviePosterDto(randomMovies));
            }
        MovieGenreDto movieGenreDto = new MovieGenreDto(genre, moviePosterDtos);

        return movieGenreDto;
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

    public List<MoviePosterDto> getGerneMovies(int pageNumber, int pageSize, String genre) {
        long startTime = System.currentTimeMillis();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviesPage = movieRepository.findAllSortedByGerne(genre,pageable);
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

