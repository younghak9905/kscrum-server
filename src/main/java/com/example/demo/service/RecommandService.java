package com.example.demo.service;

import com.example.demo.domain.dto.MovieChoiceRequestDto;
import com.example.demo.domain.dto.MovieRecommendDto;
import com.example.demo.domain.entity.Movie;
import com.example.demo.repository.LinksRepository;
import com.example.demo.repository.MovieGenreRepository;
import com.example.demo.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommandService {


    private final MovieRepository movieRepository;


    private final MovieGenreRepository movieGenreRepository;

    private final WebClient webClient;

    @Value("${ML.api.url}")
    String url;



    public void choiceMovie(MovieChoiceRequestDto dto) {
        List<Long> movieIds = dto.getMovieIds();
        List<Movie> movies = movieRepository.findByMovieIdIn(movieIds);
        List<String> movieTitles = new ArrayList<>();
        for (Movie movie : movies) {
            String titleWithoutYear = movie.getMovieId().toString();
            movieTitles.add(titleWithoutYear);
        }

        getRecommendationsAsync(movieTitles).thenAccept(recommendations -> {
            System.out.println("Recommendations: " + recommendations);
            List<Long> movieTitlesToSave = recommendations.stream()
                    .map(MovieRecommendDto::getMovieId)
                    .collect(Collectors.toList());
            // 비동기적으로 받은 추천 영화 처리
            updateMoviePriorities(movieTitlesToSave);
            System.out.println("Movie priorities updated");
        });
    }



    @Async
    public CompletableFuture<List<MovieRecommendDto>> getRecommendationsAsync(List<String> movieTitles) {

        String movieTitlesParam = String.join("", movieTitles);
        // 쿼리 파라미터로 영화 제목 목록을 추가
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("movie_titles", movieTitlesParam); // 리스트를 파이프로 구분된 문자열로 변환
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
    public void updateMoviePriorities(List<Long> recommendations) {
        System.out.println(recommendations);
        List<Movie> movies = new ArrayList<>();
        for (Long movieId : recommendations) {
            Movie movie = movieRepository.findByMovieId(movieId).stream()
                    .filter(m -> !recommendations.contains(m.getMovieId())) // 추천 영화 목록에 없는 영화만 선택
                    .findFirst()
                    .orElse(null);
            if (movie != null) {
                movies.add(movie);
            }
        }

        movies.forEach(movie -> {
            int movieYear = Integer.parseInt(movie.getYear());
            if (movieYear >= 2000) {
                movie.setPriority(movie.getPriority() == null ? 1 : movie.getPriority() + 1);
                movie.setUpdateDate(LocalDateTime.now());
                System.out.println("Movie: " + movie.getTitle() + ", Priority: " + movie.getPriority());
            }
        });
        movieRepository.saveAll(movies);

    }



}
