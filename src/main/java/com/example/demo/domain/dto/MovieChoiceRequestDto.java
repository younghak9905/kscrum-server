package com.example.demo.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieChoiceRequestDto {

    List<Long> movieIds;

    public MovieChoiceRequestDto(List<Long> movieIds) {
        this.movieIds = movieIds;
    }

    public MovieChoiceRequestDto() {
    }
}
