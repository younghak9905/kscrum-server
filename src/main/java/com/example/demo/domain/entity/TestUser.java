package com.example.demo.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "test_user")
public class TestUser {
    @Id
    private Long userId;

    public TestUser(Long id) {
        this.userId = id;
    }

    public TestUser() {

    }
}
