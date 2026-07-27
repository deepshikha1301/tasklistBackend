package com.api.tasklist.service;

import com.api.tasklist.entity.RefreshToken;
import com.api.tasklist.entity.User;
import com.api.tasklist.repository.RefreshTokenRepository;
import com.api.tasklist.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository){
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken generateRefreshToken(String username, boolean isFirstToken){
        User user = userRepository.findByLoginId(username).get();
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(java.time.Instant.now().plusSeconds(60 * 60 * 24 * 7)); // 7 days
        if(isFirstToken){
            refreshTokenRepository.save(refreshToken);
        }else{
            refreshTokenRepository.setNewToken(refreshToken.getToken(), username);
        }

        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyTokenExpiration(RefreshToken token){
        if (token.getExpiryDate().isBefore(java.time.Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token has expired, please login again");
        }else {
            token = generateRefreshToken(token.getUser().getLoginId(), false);
        }
        return token;
    }
}
