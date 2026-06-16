package com.api.tasklist.controller;

import com.api.tasklist.dto.RegisterRequest;
import com.api.tasklist.entity.User;
import com.api.tasklist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getLoginId() == null || req.getLoginId().trim().isEmpty()
                || req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("loginId and password are required");
        }

        if (userRepository.findByLoginId(req.getLoginId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("loginId already exists");
        }

        String hashed = passwordEncoder.encode(req.getPassword());
        User user = new User(req.getName(), req.getLoginId(), hashed);
        User saved = userRepository.save(user);

        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(saved);
    }
}

