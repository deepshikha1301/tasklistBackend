package com.api.tasklist.service;

import com.api.tasklist.entity.User;
import com.api.tasklist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String loginId, String email, String password) {
        if (loginId == null || loginId.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("loginId and password are required");
        }

        if (userRepository.findByLoginId(loginId).isPresent()) {
            throw new IllegalStateException("loginId already exists");
        }

        String hashed = passwordEncoder.encode(password);
        User user = new User(email, loginId, hashed);
        User saved = userRepository.save(user);
        logger.info("User registered successfully: loginId={}", loginId);
        return saved;
    }

    @Transactional(readOnly = true)
    public User login(String loginId, String password) {
        if (loginId == null || loginId.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("loginId and password are required");
        }

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Login failed: incorrect password for loginId={}", loginId);
            throw new IllegalArgumentException("Invalid loginId or password");
        }

        logger.info("User logged in successfully: loginId={}", loginId);
        return user;
    }
}