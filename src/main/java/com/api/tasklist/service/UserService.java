package com.api.tasklist.service;

import com.api.tasklist.dto.LoginRequest;
import com.api.tasklist.entity.RefreshToken;
import com.api.tasklist.entity.User;
import com.api.tasklist.exception.NoSuchUserException;
import com.api.tasklist.exception.UserAlreadyExistsException;
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
        if (userRepository.findByLoginId(loginId).isPresent()) {
            throw new UserAlreadyExistsException("loginId already exists");
        }
        String hashed = passwordEncoder.encode(password);
        User user = new User(email, loginId, hashed);
        User saved = userRepository.save(user);
        logger.info("User registered successfully: loginId={}", loginId);
        return saved;
    }

    @Transactional
    public Map<String, String> login(LoginRequest req) {

        if (userRepository.findByLoginId(req.loginId()).isEmpty()) {
            throw new NoSuchUserException("User not found with loginId: " + req.loginId());
        }

        Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.loginId(), req.password())
                );
        if(authentication.isAuthenticated()){
            RefreshToken refreshToken = refreshTokenService.generateRefreshToken(req.loginId(), true);
            String accessToken = jwtService.generateToken(req.loginId());
            return Map.of("access_token", accessToken, "refresh_token", refreshToken.getToken());
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