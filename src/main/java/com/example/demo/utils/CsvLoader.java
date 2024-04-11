package com.example.demo.utils;

import com.example.demo.domain.entity.Links;
import com.example.demo.domain.entity.Movie;
import com.example.demo.repository.LinksRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.service.MovieService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class CsvLoader implements CommandLineRunner {

    private final MovieRepository movieRepository;

    private final LinksRepository linksRepository;

    private final MovieService movieService;




    @Override
    @Transactional
    public void run(String... args) throws Exception {
        loadMovies();
        loadLinks();
    }

    private void loadMovies() throws Exception {
        Resource resource = new ClassPathResource("data/movies.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            List<Movie> movies = new ArrayList<>();
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                if (nextLine != csvReader.peek()) {
                    Movie movie = new Movie();
                    movie.setMovieId(Long.parseLong(nextLine[0].trim()));
                    movie.setTitle(nextLine[1].trim());
                    movie.setGenres(nextLine[2].trim());
                    movies.add(movie);
                }
            }
            movieRepository.saveAll(movies); // 배치 저장
        } catch (CsvException | IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLinks() throws Exception {
        Resource resource = new ClassPathResource("data/links.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            List<Links> linksList = new ArrayList<>();
            String[] nextLine;

            // 첫 번째 라인(헤더)을 읽어서 건너뜁니다.
            csvReader.readNext();

            // 다음 줄부터 데이터를 읽기 시작합니다.
            while ((nextLine = csvReader.readNext()) != null) {
                try {
                    Long movieId = Long.parseLong(nextLine[0].trim());
                    Optional<Movie> movie = movieRepository.findById(movieId);
                    if (movie.isPresent()) {
                        Links link = new Links();
                        link.setMovieId(movie.get());
                        link.setImdbId(Long.parseLong(nextLine[1].trim()));
                        link.setTmdbId(Long.parseLong(nextLine[2].trim()));
                        linksList.add(link);
                    } else {
                        System.out.println("Movie not found for ID: " + movieId);
                    }
                } catch (NumberFormatException e) {
                    // Log and skip this record or handle the error appropriately.
                    System.err.println("Skipping invalid record: " + Arrays.toString(nextLine));
                }
            }
            if (!linksList.isEmpty()) {
                linksRepository.saveAll(linksList); // 배치 저장
            }
        } catch (CsvException | IOException e) {
            e.printStackTrace();
        }
    }
}