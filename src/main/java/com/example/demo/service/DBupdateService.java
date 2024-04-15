package com.example.demo.service;

import com.example.demo.domain.entity.TestUser;
import com.example.demo.repository.TestUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DBupdateService {

    private final TestUserRepository testUserRepository;

    public void insertTestUser() {
        //1~610까지의 데이터를 넣어줍니다.
        for (int i = 1; i <= 610; i++) {
            testUserRepository.save(new TestUser((long) i));
        }
    }



}
