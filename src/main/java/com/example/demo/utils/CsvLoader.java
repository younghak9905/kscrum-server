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
    public void run(String... args) throws Exception {
        loadMovies();
        //loadLinks();
    }

    private void loadMovies() throws Exception {
        Resource resource = new ClassPathResource("data/movies.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            List<Movie> batch = new ArrayList<>();
            String[] nextLine;
            // 첫 번째 행(헤더)을 읽어서 건너뜁니다.
            csvReader.readNext();

            while ((nextLine = csvReader.readNext()) != null) {
                Movie movie = new Movie();
                movie.setMovieId(Long.parseLong(nextLine[0].trim()));
                movie.setTitle(nextLine[1].trim());
                movie.setGenres(nextLine[2].trim());
                batch.add(movie);
                System.out.println(movie.getTitle());
                // 100개 단위로 저장
                if (batch.size() == 100) {
                    System.out.println("!!!batch size: " + batch.size()+"\n\n\n\n\n\n");
                    movieRepository.saveAll(batch);
                    batch.clear(); // 저장 후 리스트 초기화
                }
            }

            // 남은 데이터가 있다면 저장
            if (!batch.isEmpty()) {
                movieRepository.saveAll(batch);
            }
        } catch (CsvException | IOException e) {
            e.printStackTrace();
        }
    }


    private void loadLinks() throws Exception {
        Resource resource = new ClassPathResource("data/links.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            List<Links> batch = new ArrayList<>();
            csvReader.readNext(); // 첫 번째 행(헤더)을 읽어서 건너뜁니다.

            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                Long movieId = Long.parseLong(nextLine[0].trim());
                Optional<Movie> movie = movieRepository.findById(movieId);
                if (movie.isPresent()) {
                    Links link = new Links();
                    link.setMovieId(movie.get());
                    link.setImdbId(Long.parseLong(nextLine[1].trim()));
                    link.setTmdbId(Long.parseLong(nextLine[2].trim()));
                    batch.add(link);

                    // 100개 단위로 저장
                    if (batch.size() == 100) {
                        linksRepository.saveAll(batch);
                        batch.clear(); // 저장 후 리스트 초기화
                    }
                }
            }

            // 남은 데이터가 있다면 저장
            if (!batch.isEmpty()) {
                linksRepository.saveAll(batch);
            }
        } catch (CsvException | IOException e) {
            e.printStackTrace();
        }
    }

}