package com.example.demo.service;
import com.example.demo.domain.dto.MovieDto;
import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.dto.MovieResponseDto;
import com.example.demo.domain.entity.Movie;
import com.example.demo.repository.LinksRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webjars.NotFoundException;

@Service
public class TmdbClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;


    public TmdbClient(RestTemplate restTemplate,
                      @Value("${tmdb.api.key}") String apiKey,
                      @Value("${tmdb.api.baseurl}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
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


    public MoviePosterDto searchMoviePoster(Long movieId) {

        MovieDto movie = getMovieDetails(movieId);
        if (movie == null)
            return null;
        MoviePosterDto movies = MoviePosterDto.builder()
                .movieId(movie.getId())
                .title(movie.getOriginalTitle())
                .posterPath(movie.getPosterPath())
                .url("https://image.tmdb.org/t/p/w500/"+movie.getPosterPath())
                .build();
        return movies;
    }




}
