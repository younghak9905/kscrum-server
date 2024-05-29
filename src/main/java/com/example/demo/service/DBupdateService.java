package com.example.demo.service;

import com.example.demo.domain.dto.MovieDto;
import com.example.demo.domain.entity.*;
import com.example.demo.repository.*;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DBupdateService {

    private final TestUserRepository testUserRepository;

    private final MovieRepository movieRepository;


    private final GenreRepository genreRepository;

    private final MovieGenreRepository movieGenreRepository;

    private final TmdbClient tmdbClient;

    private final SelectedMoviesRepository selectedMoviesRepository;


    private final MovieService movieService;
    public void insertTestUser() {
        //1~610까지의 데이터를 넣어줍니다.
        for (int i = 1; i <= 610; i++) {
            testUserRepository.save(new TestUser((long) i));
        }
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


    @Async
    public CompletableFuture<Void> updateMoviePostersAsync(int page, int size) {
        // 계산된 시작 인덱스: 페이지 번호가 2이면 200부터 시작 (2 * 100)
        int startIndex = page * 100;

        for (int i = 0; i < size; i++) {
            // 페이지 요청 생성: 각 반복에서 페이지 크기는 100으로 고정
            Pageable pageable = PageRequest.of((startIndex / 100) + i, 100);
            Page<Movie> moviePage = movieRepository.findAll(pageable);
           // Page<Movie> moviePage = movieRepository.findMovieByposterUrlIsNull(pageable);
            // 페이지별로 영화 포스터 URL 업데이트
            List<Movie> updatedMovies = moviePage.getContent().stream()
                    .map(this::updateMoviePoster)
                    .collect(Collectors.toList());

            // 데이터베이스에 한번에 저장
            movieRepository.saveAll(updatedMovies);

            // 진행 상황 로깅
            System.out.println("Processed page: " + ((startIndex / 100) + i + 1) + ", movies processed: " + updatedMovies.size());
        }

        return CompletableFuture.completedFuture(null);
    }

    private Movie updateMoviePoster(Movie movie) {
        Long tmdbId = movieService.getTmdbId(movie);
        if (tmdbId != null) {
            String posterUrl = tmdbClient.getPosterUrl(tmdbId);
            movie.setPosterUrl(posterUrl);
        }
        return movie;
    }
    public void updateMoviePoster(Long movieId) {
        Movie movie = movieRepository.findByMovieId(movieId).orElseThrow();
        Long tmdbId = movieService.getTmdbId(movie);
        if (tmdbId != null) {
            String posterUrl = tmdbClient.getPosterUrl(tmdbId);
            movie.setPosterUrl(posterUrl);
        }
        movieRepository.save(movie);
    }


    public void resetPriority() {
        List<Movie> movies = movieRepository.findPriorityIsNotNull();
        for (Movie movie : movies) {
            movie.setPriority(null);
            movie.setUpdateDate(null);
        }
        movieRepository.saveAll(movies);
    }

    public String getTitleByTitle(String title) {
        List<Movie> movies = movieRepository.findByTitleContaining(title);
       return movies.get(0).getTitle();

    }

    public String getTitleByMovieId(Long movieId) {
        return movieRepository.findByMovieId(movieId).get().getTitle();
    }

    public void updateTitle(String title) {
        List<Movie> movies = movieRepository.findByTitleContaining(title);
        for (Movie movie : movies) {
            movie.setTitle("The " + title.substring(0, title.length() - 12));
        }
        movieRepository.saveAll(movies);

    }

    @Async
    public CompletableFuture<Void> updateMovieTitle(int page, int size) {
        int startIndex = page * 100;
        for (int i = 0; i < size; i++) {
            // 페이지 요청 생성: 각 반복에서 페이지 크기는 100으로 고정
            Pageable pageable = PageRequest.of((startIndex / 100) + i, 100);
            Page<Movie> moviePage = movieRepository.findAll(pageable);

            // 각 영화에 대한 처리 로직을 수행합니다.
            updateKoreanTitle(moviePage); // 변경된 메서드 호출

            // 진행 상황 로깅
            System.out.println("Page: " + (page + 1) + ", Offset: " + pageable.getOffset() + ", Movies: " + moviePage.getContent().size());
        }

        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void updateKoreanTitle(Page<Movie> moviePage) {
        moviePage.forEach(movie -> {
            Long tmdbId = movieService.getTmdbId(movie);
            if (tmdbId != null) {
                String korTitle = tmdbClient.getMovieDetails(tmdbId).getTitle();
                movie.setKorTitle(korTitle);
                movieRepository.save(movie);
            }
        });

    }


    @Async
    public CompletableFuture<Void> match(int page, int size) {
        int startIndex = page * 100;
        for (int i = 0; i < size; i++) {
            // 페이지 요청 생성: 각 반복에서 페이지 크기는 100으로 고정
            Pageable pageable = PageRequest.of((startIndex / 100) + i, 100);
            Page<SelectedMovies> moviePage = selectedMoviesRepository.findAll(pageable);

            matchTmdbId(moviePage);

            // 각 영화에 대한 처리 로직을 수행합니다.


            // 진행 상황 로깅
            System.out.println("Page: " + (page + 1) + ", Offset: " + pageable.getOffset() + ", Movies: " + moviePage.getContent().size());
        }

        return CompletableFuture.completedFuture(null);

    }

    @Transactional
    public void matchTmdbId(Page<SelectedMovies> moviePage) {
        moviePage.forEach(movie -> {
            if(  movie.getMovie()==null) {

                Movie movieId = movieService.getMovieId(movie.getId());
                if (movieId != null) {
                    movie.setMovie(movieId);
                    String korTitle = tmdbClient.getMovieDetails(movie.getId()).getTitle();
                    if (korTitle != null) {
                        movie.setKorTitle(korTitle);
                    }
                    selectedMoviesRepository.save(movie);
                } else {

                    //movie 테이블에 가장 마지막 번호
                    MovieDto movieDto = tmdbClient.getMovieDetails(movie.getId());
                    if (movieDto != null) {
                        Long id = movieRepository.findFirstByOrderByMovieIdDesc().getMovieId() + movie.getId() + 99999;
                        Movie newMovie = new Movie(movieDto, id);
                        String genres = tmdbClient.getMovieDetailsEng(movie.getId()).getGenres().toString();
                        newMovie.setGenres(genres);
                        if (movieRepository.findByMovieId(id).isEmpty()) {
                            movieRepository.save(newMovie);
                            movie.setKorTitle(movieDto.getTitle());
                            selectedMoviesRepository.save(movie);
                        }
                    }


                }


            }

        });

    }
}
