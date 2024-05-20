package com.example.demo.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieRecommendDto {
    @JsonProperty("Title") // JSON 필드 "Title"를 매핑
    private String title;

    @JsonProperty("MovieID")
    private Long movieId;

    @JsonProperty("Estimated Rating") // JSON 필드 "Estimated Rating"를 매핑
    private double estimatedRating;
}
