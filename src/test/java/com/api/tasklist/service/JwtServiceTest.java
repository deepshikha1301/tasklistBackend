package com.api.tasklist.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    JwtService jwtService;

    @Test
    void shouldReturnTokenSuccessfully(){
        String token = jwtService.generateToken("abc_xyz");
        Assertions.assertNotNull(token);
    }
}