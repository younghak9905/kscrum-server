package com.example.demo.repository;

import com.example.demo.domain.entity.Links;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinksRepository extends JpaRepository<Links, Long> {
}
