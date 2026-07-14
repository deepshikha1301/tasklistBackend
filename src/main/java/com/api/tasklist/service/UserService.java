package com.api.tasklist.service;

import com.api.tasklist.dto.LoginRequest;
import com.api.tasklist.entity.RefreshToken;
import com.api.tasklist.entity.User;
import com.api.tasklist.model.UserPrincipal;
import com.api.tasklist.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       @Lazy AuthenticationManager authenticationManager, JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
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
    public Map<String, String> login(LoginRequest req) {

        Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getLoginId(), req.getPassword())
                );
        if(authentication.isAuthenticated()){
            RefreshToken refreshToken = refreshTokenService.generateRefreshToken(req.getLoginId());
            String accessToken = jwtService.generateToken(req.getLoginId());
            return Map.of("access_token", accessToken, "refresh_token", refreshToken.getTokenId());
        }
        return Map.of("access_token", "Failed");
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with loginId: " + username));
        logger.info("User logged in successfully---loadUserByUsername: loginId={}", username);
        return new UserPrincipal(user);
    }
}