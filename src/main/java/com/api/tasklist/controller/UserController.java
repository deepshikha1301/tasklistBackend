package com.api.tasklist.controller;

import com.api.tasklist.dto.RegisterRequest;
import com.api.tasklist.entity.User;
import com.api.tasklist.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        logger.info("Received registration request: loginId={}, email={}", req.getLoginId(), req.getEmail());
        if (req.getLoginId() == null || req.getLoginId().trim().isEmpty()
                || req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            logger.warn("Invalid registration request: loginId or password is missing");
            return ResponseEntity.badRequest().body("loginId and password are required");
        }

        if (userRepository.findByLoginId(req.getLoginId()).isPresent()) {
            logger.warn("Registration request failed: loginId already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("loginId already exists");
        }

        String hashed = passwordEncoder.encode(req.getPassword());
        User user = new User(req.getEmail(), req.getLoginId(), hashed);
        User saved = userRepository.save(user);

        logger.info("User registered successfully: loginId={}", req.getLoginId());
        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(saved);
    }
}

