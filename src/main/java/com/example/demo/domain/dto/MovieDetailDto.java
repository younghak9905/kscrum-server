package com.example.demo.domain.dto;

import com.example.demo.domain.entity.Genre;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


@Getter
@Setter
public class MovieDetailDto {
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("original_title")
    private String originalTitle;
    private String title;
    @JsonProperty("release_date")
    private String releaseDate;
    private double voteAverage;
    private int runtime;
    private ArrayList<Genre> genres;
    private String genreString;
    private String tagline;
    private String overview;

    @Builder
    public MovieDetailDto(String posterPath, String originalTitle, String title,
                          String releaseDate, double voteAverage, int runtime,
                          ArrayList genres, String tagline, String overview) {
        this.posterPath = posterPath;
        this.originalTitle = originalTitle;
        this.title = title;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.runtime = runtime;
        this.genreString = genreToString(genres);
        this.tagline = tagline;
        this.overview = overview;
    }
        
    // arraylist로 들어온 장르를 '액션 / 코미디 / 멜로' 와 같이 바꾸는 메소드
    private String genreToString(ArrayList genres) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i<genres.size(); i++) {
            Object genre = genres.get(i);
            LinkedHashMap<String,String> map =  (LinkedHashMap<String,String>) genre;
            result.append(map.get("name"));
            if(i!=genres.size()-1) {
                result.append(" / ");
            } else{
            }
        }

        return result.toString();
    }


}
