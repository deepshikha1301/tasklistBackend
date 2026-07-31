package com.api.tasklist.controller;

import com.api.tasklist.dto.LoginRequest;
import com.api.tasklist.dto.RegisterRequest;
import com.api.tasklist.entity.RefreshToken;
import com.api.tasklist.entity.User;
import com.api.tasklist.service.JwtService;
import com.api.tasklist.service.RefreshTokenService;
import com.api.tasklist.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    UserService userService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    RefreshTokenService refreshTokenService;


    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        User user = mockUser();
        RegisterRequest req = mockRegisterUser();
        Mockito.when(userService.register(anyString(), anyString(), anyString())).thenReturn(user);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loginId").value("abc_xyz"));

    }

    @Test
    void shouldLoginUserSuccessfully() throws Exception {
        LoginRequest req = mockLoginRequest();
        Mockito.when(userService.login(any())).thenReturn(Map.of("access_token", "accessToken", "refresh_token", "refreshToken"));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("accessToken"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void shouldReturnAccessTokenSuccessfully() throws Exception {
        RefreshToken rToken = mockRefreshToken();
        Cookie refreshCookie = new Cookie("refresh_token", "refreshToken");
        Mockito.when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(rToken));
        Mockito.when(refreshTokenService.verifyTokenExpiration(any())).thenReturn(rToken);
        Mockito.when(jwtService.generateToken(anyString())).thenReturn("accessToken");

        mockMvc.perform(post("/api/users/refreshToken")
                .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("accessToken"))
                .andExpect(header().exists("Set-Cookie"));
    }


    private User mockUser(){
        User user = new User();
        user.setId(1L);
        user.setEmail("abcxyz@gmail.com");
        user.setPassword("password");
        user.setLoginId("abc_xyz");
        return user;
    }

    private RegisterRequest mockRegisterUser(){
        return new RegisterRequest("abcxyz@gmail.com", "abc_xyz", "password");
    }

    private LoginRequest mockLoginRequest(){
        return new LoginRequest("abc_xyz", "password");
    }

    private RefreshToken mockRefreshToken(){
        RefreshToken token = new RefreshToken();
        token.setId(1);
        token.setToken("refreshToken");
        token.setUser(mockUser());
        token.setExpiryDate(Instant.now());
        return token;
    }
}