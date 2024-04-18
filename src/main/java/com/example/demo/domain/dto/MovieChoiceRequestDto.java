package com.example.demo.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieChoiceRequestDto {

    List<Long> movieIds;
}
