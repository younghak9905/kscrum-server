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

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;


@Component
@RequiredArgsConstructor
public class CsvLoader implements CommandLineRunner {

    private final MovieRepository movieRepository;

    private final LinksRepository linksRepository;

    private final MovieService movieService;




    @Override
    public void run(String... args) throws Exception {
        Resource resource = new ClassPathResource("data/movies.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> records = csvReader.readAll();
            records.stream()
                    .skip(1) // 첫 번째 라인은 헤더이므로 건너뜁니다.
                    .forEach(array -> {
                        Movie movie = new Movie();
                        movie.setMovieId(Long.parseLong(array[0].trim()));
                        movie.setTitle(array[1].trim());
                        movie.setGenres(array[2].trim());
                        movieRepository.save(movie);
                    });
        } catch (CsvException e) {
            e.printStackTrace();
        }

        Resource resource2 = new ClassPathResource("data/links.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> records = csvReader.readAll();
            records.stream()
                    .skip(1) // 첫 번째 라인은 헤더이므로 건너뜁니다.
                    .forEach(array -> {
                       Links link = new Links();
                       Movie movieId= movieService.getMovieByMovieId(Long.parseLong(array[1].trim()));
                        link.setMovieId(movieId);
                        link.setImdbId(Long.parseLong(array[2].trim()));
                        link.setTmdbId(Long.parseLong(array[3].trim()));
                        linksRepository.save(link);
                    });
        } catch (CsvException e) {
            e.printStackTrace();
        }
    }
}