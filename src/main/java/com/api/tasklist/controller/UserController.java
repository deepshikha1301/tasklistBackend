package com.api.tasklist.controller;

import com.api.tasklist.dto.LoginRequest;
import com.api.tasklist.dto.RegisterRequest;
import com.api.tasklist.entity.User;
import com.api.tasklist.service.JwtService;
import com.api.tasklist.service.RefreshTokenService;
import com.api.tasklist.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Autowired
    public UserController(UserService userService, RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody RegisterRequest req) {
        return userService.register(req.loginId(), req.email(), req.password());

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req){
        logger.info("Received login request: loginId={}", req.loginId());
        Map<String, String> result = userService.login(req);
        ResponseCookie cookie = setRefeshTokenCookie(result.get("refresh_token"));
        if("Failed".equals(result.get("access_token"))){
            logger.warn("Login request failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }else{
            logger.info("User logged in successfully: loginId={}", req.loginId());
            return ResponseEntity.ok()
                            .header("Set-Cookie", cookie.toString())
                            .body(Map.of("access_token", result.get("access_token")));
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@CookieValue(name="refresh_token") String refreshToken){
        logger.info("refresh token request------" + refreshToken);
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyTokenExpiration)
                .map(
                        token -> {
                            String newAccessToken = jwtService.generateToken(token.getUser().getLoginId());
                            ResponseCookie cookie = setRefeshTokenCookie(token.getToken());
                            logger.info("Access token refreshed successfully for user: loginId={}", token.getUser().getLoginId());
                            return ResponseEntity.ok()
                                    .header("Set-Cookie", cookie.toString())
                                    .body(Map.of("access_token", newAccessToken));
                        }).orElseThrow(()-> {
                            logger.warn("Refresh token not found or expired: refreshTokenId={}", refreshToken);
                            return new RuntimeException("Refresh token not found or expired, please login again");
                        });
    }

    private ResponseCookie setRefeshTokenCookie(String refreshToken ){
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false) // only for dev environment
                .path("/api/users/refreshToken")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict")
                .build();
    }
}

