package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@Table(name = "tags",indexes = {
        @Index(name = "idx_movie_id", columnList = "movie_id"), // 'movie_id' 컬럼에 대한 인덱스
        @Index(name = "idx_user_id", columnList = "user_id") // 'user_id' 컬럼에 대한 인덱스
})
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    @ManyToOne
    @JoinColumn(name = "user_id") // 데이터베이스의 실제 컬럼 이름과 일치시켜야 합니다.
    private TestUser userId;

    @ManyToOne
    @JoinColumn(name = "movie_id") // 데이터베이스의 실제 컬럼 이름과 일치시켜야 합니다.
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Movie movieId;

    private String tag;

    private Integer timestamp;

}
