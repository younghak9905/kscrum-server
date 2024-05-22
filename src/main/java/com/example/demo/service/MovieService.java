package com.example.demo.service;

import com.example.demo.domain.dto.*;
import com.example.demo.domain.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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


    private final MovieLikeService movieLikeService;

    private final MovieMarkedService markedService;


    @Value("${ML.api.url}")
    String url;

    public Movie getMovieByMovieId(Long movieId) {
        return movieRepository.findByMovieId(movieId).orElse(null);
    }



    public List<MoviePosterDto> choiceRandomMovies() {
        //List<Movie> randomMovies = movieRepository.findRandomMovie();

        List<MoviePosterDto> moviePosterDtos = new ArrayList<>();
//        List<Genre> movieGenres = findAllGenres();
        List<String> movieGenres = new ArrayList<String>(Arrays.asList("Drama", "Thriller", "Action", "Animation", "Romance", "Comedy"));

        for (String genre : movieGenres) {
            System.out.println("Genre: " + genre);
                List<Movie> randomMovies = movieRepository.findRandomMoviesByGenre(genre);
                moviePosterDtos.addAll(movieToMoviePosterDto(randomMovies));
        }

        return moviePosterDtos;
    }

    public MovieGenreDto choiceRandomMovies(String genre) {
        //List<Movie> randomMovies = movieRepository.findRandomMovie();

        List<MoviePosterDto> moviePosterDtos = new ArrayList<>();
//        List<Genre> movieGenres = findAllGenres();

            System.out.println("Genre: " + genre);

            List<Movie> randomMovies = movieRepository.findRandomMoviesByRomance(genre);
            moviePosterDtos.addAll(movieToMoviePosterDto(randomMovies));
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

    public List<MoviePosterDto> getGenreMovies(int pageNumber, int pageSize, String genre) {
        long startTime = System.currentTimeMillis();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviesPage = movieRepository.findAllSortedByGenre(genre,pageable);
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
        boolean isMarked =markedService.isMarkedMovie(movie);
        boolean isLiked = movieLikeService.isLikedMovie(movie);
        System.out.println("Create MoviePosterDto time: " + (System.currentTimeMillis() - startTime) + " ms");
        return new MoviePosterDto(movie, posterUrl,isMarked,isLiked);
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



    public List<MoviePosterDto> search(int pageNumber, int pageSize, String type, String keyword, String filterType, String ordering) {
        PageRequest pageable;
        Page<Movie> moviesPage;
        if(type.equals("genre")){
            if(ordering.equals("desc"))
                pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, filterType));
            else
                pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, filterType));
            moviesPage = movieRepository.findAllSortedByGenre(keyword, pageable);
            return movieToMoviePosterDto(moviesPage.getContent());
        } else {
            if(ordering.equals("desc"))
                pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, filterType));
            else
                pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, filterType));
            moviesPage = movieRepository.findAllByTitleContaining(keyword, pageable);
        }
        return movieToMoviePosterDto(moviesPage.getContent());
    }

    public MovieDetailDto getMovieDetails(Long movieId) {
        Movie movie = movieRepository.findByMovieId(movieId).orElse(null);
        Long tmdbId = getTmdbId(movie);
        if (tmdbId != null) {
            MovieDto movieDto = tmdbClient.getMovieDetails(tmdbId);
            MovieDetailDto result = MovieDetailDto.builder()
                    .posterPath("https://image.tmdb.org/t/p/w500/"+ movieDto.getPosterPath())
                    .originalTitle(movieDto.getOriginalTitle())
                    .title(movieDto.getTitle())
                    .releaseDate(movieDto.getReleaseDate())
                    .voteAverage(movieDto.getVoteAverage())
                    .runtime(movieDto.getRuntime())
                    .genres(movieDto.getGenres())
                    .tagline(movieDto.getTagline())
                    .overview(movieDto.getOverview())
                    .build();
            return result;
        }
        return null;
    }



    public List<MoviePosterDto> getLikedMovies(){
        return movieToMoviePosterDto(new ArrayList<>( movieLikeService.getLikedMovies()));
    }

    public List<MoviePosterDto> getMarkedMovies() {
        return movieToMoviePosterDto(new ArrayList<>( markedService.getMarkedMovies()));
    }

    public MoviePosterDto getMovieDetailsInDB(Long movieId) {
        Movie movie = movieRepository.findByMovieId(movieId).orElse(null);
        if (movie != null) {
            return new MoviePosterDto(movie, getPosterUrl(movie));
        }
        return null;
    }
}

