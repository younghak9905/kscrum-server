package com.example.demo.domain.vo;

import com.example.demo.domain.dto.MoviePosterDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoviePosterVo {

    private MoviePosterDto moviePosterDto;

    private boolean isMarked;

    private boolean isLiked;



    public MoviePosterVo(MoviePosterDto moviePosterDto, boolean isMarked, boolean isLiked) {
        this.moviePosterDto = moviePosterDto;
        this.isMarked = isMarked;
        this.isLiked = isLiked;
    }
}
