package com.example.demo.service;
import com.example.demo.domain.dto.MovieDto;
import com.example.demo.domain.dto.MoviePosterDto;
import com.example.demo.domain.dto.MovieResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + movieId)
                .queryParam("api_key", apiKey)
                .toUriString();
        return restTemplate.getForObject(url, MovieDto.class);
    }


    public MoviePosterDto searchMoviePoster(String query) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("language", "ko-KR")
                .toUriString();
        MovieResponseDto movie = restTemplate.getForObject(url, MovieResponseDto.class);
        MoviePosterDto movies = MoviePosterDto.builder()
                .movieId(movie.getResults().get(0).getId())
                .title(movie.getResults().get(0).getOriginalTitle())
                .posterPath(movie.getResults().get(0).getPosterPath())
                .url("https://image.tmdb.org/t/p/w500/"+movie.getResults().get(0).getPosterPath())
                .build();
        return movies;
    }
}
