package com.api.tasklist.controller;

import com.api.tasklist.dto.LoginRequest;
import com.api.tasklist.dto.RegisterRequest;
import com.api.tasklist.entity.User;
import com.api.tasklist.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        logger.info("Received registration request: loginId={}, email={}", req.getLoginId(), req.getEmail());
        User user;
        try{
            user = userService.register(req.getLoginId(), req.getEmail(), req.getPassword());
        }catch (IllegalArgumentException e){
            logger.warn("Invalid registration request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (IllegalStateException e){
            logger.warn("Registration request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }catch (Exception e){
            logger.error("Unexpected error during registration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }

        logger.info("User registered successfully: loginId={}", req.getLoginId());
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req){
        logger.info("Received login request: loginId={}", req.getLoginId());
        try{
            User user = userService.login(req.getLoginId(), req.getPassword());
            logger.info("User logged in successfully: loginId={}", req.getLoginId());
            return ResponseEntity.ok(user);
        }catch (IllegalArgumentException e){
            logger.warn("Login request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }catch (Exception e){
            logger.error("Unexpected error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}

