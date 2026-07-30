package com.api.tasklist.service;

import com.api.tasklist.entity.RefreshToken;
import com.api.tasklist.entity.User;
import com.api.tasklist.repository.RefreshTokenRepository;
import com.api.tasklist.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    RefreshTokenService refreshTokenService;

    @Test
    void shouldReturnRefreshTokenSuccessfully(){
        User user = createUser();
        Mockito.when(userRepository.findByLoginId(anyString())).thenReturn(Optional.of(user));
        Mockito.when(refreshTokenRepository.save(any())).thenReturn(null);
        RefreshToken token = refreshTokenService.generateRefreshToken("abc_xyz", true);
        Assertions.assertNotNull(token);
    }

    private User createUser(){
        User user = new User();
        user.setId(1L);
        user.setEmail("abcxyz@gmail.com");
        user.setPassword("password");
        user.setLoginId("abc_xyz");
        return user;
    }
}