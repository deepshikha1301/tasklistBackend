package com.api.tasklist.repository;

import com.api.tasklist.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldFindUserByLoginId(){
        User user = mockUser();
        userRepository.save(user);
        User foundUser = userRepository.findByLoginId("abc_xyz").get();
        assertEquals(user.getId(), foundUser.getId());
    }

    private User mockUser(){
        User user = new User();
        user.setEmail("abcxyz@gmail.com");
        user.setPassword("password");
        user.setLoginId("abc_xyz");
        return user;
    }
}