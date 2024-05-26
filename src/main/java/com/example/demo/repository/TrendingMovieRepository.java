package com.example.demo.repository;

import com.example.demo.domain.entity.TrendingMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrendingMovieRepository extends JpaRepository<TrendingMovie, Long> {
    @Query("SELECT m FROM TrendingMovie m ORDER BY m.updateDate DESC")
    List<TrendingMovie> findAllOrderByUpdateDateDesc();
@Query("SELECT m FROM TrendingMovie m WHERE m.movieType LIKE %:movieType% ORDER BY m.updateDate DESC")
    List<TrendingMovie> findAllByOption(String movieType);
}
