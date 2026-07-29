package com.api.tasklist.service;

import com.api.tasklist.dto.LoginRequest;
import com.api.tasklist.entity.RefreshToken;
import com.api.tasklist.entity.User;
import com.api.tasklist.exception.NoSuchUserException;
import com.api.tasklist.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtService jwtService;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    Authentication authentication;

    @InjectMocks
    UserService userService;

    @Test
    void shouldRegisterUserSuccessfully(){
        User user = createUser();
        Mockito.when(userRepository.save(any())).thenReturn(user);
        User output = userService.register("abc_xyz", "abcxyz@gmail.com", "password");
        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.getLoginId(), output.getLoginId());
    }

    @Test
    void shouldLoginUserSuccessfully(){
        User user = createUser();
        RefreshToken refreshToken = createRefreshToken(user);
        LoginRequest loginRequest = createLoginRequest();

        Mockito.when(userRepository.findByLoginId(anyString())).thenReturn(Optional.of(user));
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(authentication);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(jwtService.generateToken(anyString())).thenReturn("test_access_token");
        Mockito.when(refreshTokenService.generateRefreshToken(anyString(), anyBoolean())).thenReturn(refreshToken);

        Map<String, String> response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test_access_token", response.get("access_token"));
        assertEquals("refresh_token_value", response.get("refresh_token"));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound(){
        LoginRequest loginRequest = createLoginRequest();
        Mockito.when(userRepository.findByLoginId(anyString())).thenReturn(Optional.empty());

        assertThrows(NoSuchUserException.class, () -> {
            userService.login(loginRequest);
        });
    }

    @Test
    void shouldReturnFailedTokenWhenAuthenticationFails(){
        User user = createUser();
        LoginRequest loginRequest = createLoginRequest();

        Mockito.when(userRepository.findByLoginId(anyString())).thenReturn(Optional.of(user));
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(authentication);
        Mockito.when(authentication.isAuthenticated()).thenReturn(false);

        Map<String, String> response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("Failed", response.get("access_token"));
    }

    private User createUser(){
        User user = new User();
        user.setId(1L);
        user.setEmail("abcxyz@gmail.com");
        user.setPassword("password");
        user.setLoginId("abc_xyz");
        return user;
    }

    private LoginRequest createLoginRequest(){
        return new LoginRequest("abc_xyz", "password");
    }

    private RefreshToken createRefreshToken(User user){
        RefreshToken token = new RefreshToken();
        token.setId(1);
        token.setToken("refresh_token_value");
        token.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60));
        token.setUser(user);
        return token;
    }
}