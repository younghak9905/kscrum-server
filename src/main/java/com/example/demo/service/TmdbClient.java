package com.example.demo.service;
import com.example.demo.domain.dto.*;
import com.example.demo.domain.entity.Links;
import com.example.demo.domain.entity.Movie;
import com.example.demo.domain.entity.PosterUrl;
import com.example.demo.repository.LinksRepository;
import com.example.demo.repository.PosterUrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webjars.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class TmdbClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    private final PosterUrlRepository posterUrlRepository;
    private final LinksRepository linksRepository;


    public TmdbClient(RestTemplate restTemplate,
                      @Value("${tmdb.api.key}") String apiKey,
                      @Value("${tmdb.api.baseurl}") String baseUrl,
                      PosterUrlRepository posterUrlRepository,
                      LinksRepository linksRepository) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.posterUrlRepository = posterUrlRepository;
        this.linksRepository = linksRepository;

    }

    public MovieResponseDto searchMovies(String query) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("language", "ko-KR")
                .toUriString();
        return restTemplate.getForObject(url, MovieResponseDto.class);
    }

    public MovieDto getMovieDetails(Long movieId) {
//        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + movieId)
//                .queryParam("api_key", apiKey)
//                .toUriString();
//        return restTemplate.getForObject(url, MovieDto.class);
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + movieId)
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "ko-KR")
                    .toUriString();
            return restTemplate.getForObject(url, MovieDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            // 영화 ID에 해당하는 영화가 존재하지 않는 경우
            // 혹은 TMDB API에서 해당 리소스를 찾을 수 없는 경우
            // 예외를 처리하고 적절한 에러 메시지를 반환
            //throw new NotFoundException("Requested movie does not exist.");
            return null;
        }
    }


    public MovieDto getMovieDetailsEng(Long movieId) {
//        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + movieId)
//                .queryParam("api_key", apiKey)
//                .toUriString();
//        return restTemplate.getForObject(url, MovieDto.class);
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + movieId)
                    .queryParam("api_key", apiKey)
                    .toUriString();
            return restTemplate.getForObject(url, MovieDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            // 영화 ID에 해당하는 영화가 존재하지 않는 경우
            // 혹은 TMDB API에서 해당 리소스를 찾을 수 없는 경우
            // 예외를 처리하고 적절한 에러 메시지를 반환
            //throw new NotFoundException("Requested movie does not exist.");
            return null;
        }
    }


    public MoviePosterDto searchMoviePoster(String query) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("language", "ko-KR")
                .toUriString();
        MovieResponseDto movie =restTemplate.getForObject(url, MovieResponseDto .class);
        MoviePosterDto movies = MoviePosterDto.builder()
                .movieId(movie.getResults().get(0).getId())
                .title(movie.getResults().get(0).getOriginalTitle())
                .posterPath(movie.getResults().get(0).getPosterPath())
                .url("https://image.tmdb.org/t/p/w500/"+movie.getResults().get(0).getPosterPath())
                .build();
        return movies;
    }

    public List<MoviePosterDto> searchMoviePosterList(String query) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("language", "ko-KR")
                .toUriString();
        MovieResponseDto movie =restTemplate.getForObject(url, MovieResponseDto .class);

        List<MoviePosterDto> movies = movie.getResults().stream()
                .filter(m-> linksRepository.findByTmdbId(m.getId()) != null)
                .map(m -> {
                    // linksRepository를 통해 tmdbId로 링크를 조회
                    Optional<Links> linkOptional = Optional.ofNullable(linksRepository.findByTmdbId(m.getId()));

                    // MoviePosterDto를 빌드
                    MoviePosterDto.MoviePosterDtoBuilder builder = MoviePosterDto.builder()
                            .movieId(m.getId()) // 기본값으로 tmdbId를 사용
                            .title(m.getOriginalTitle())
                            .posterPath(m.getPosterPath())
                            .url("https://image.tmdb.org/t/p/w500/" + m.getPosterPath());

                    // 링크가 존재하면 movieId를 링크의 movieId로 설정
                    linkOptional.ifPresent(link -> builder.movieId(link.getMovieId().getMovieId()));

                    // MoviePosterDto 객체 반환
                    return builder.build();
                })
                .toList();

        return movies;
    }



    public String getPosterUrl(Long tmdbId)
    {
        MovieDto movie = getMovieDetails(tmdbId);
        if (movie == null)
            return null;
        return movie.getPosterPath();
    }

    public MovieListDto searchPlayingMovie(int page){
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/now_playing")
                .queryParam("api_key", apiKey)
                .queryParam("language", "ko-KR")
                .queryParam("page", page)
                .queryParam("region","KR")
                .toUriString();
        MovieListDto result = restTemplate.getForObject(url, MovieListDto.class);
        return result;
    }

    public MovieListDto searchTrendingMovie(String timeWindow) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/trending/movie/{time_window}")
                .queryParam("api_key", apiKey)
                .queryParam("language", "ko-KR")
                .buildAndExpand(timeWindow)
                .toUriString();
        MovieListDto result = restTemplate.getForObject(url, MovieListDto.class);
        return result;
    }




}
