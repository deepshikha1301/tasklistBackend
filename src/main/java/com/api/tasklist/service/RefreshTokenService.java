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

    public RefreshToken generateRefreshToken(String username){
        User user = userRepository.findByLoginId(username).get();
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenId(token);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(java.time.Instant.now().plusSeconds(60 * 60 * 24 * 7)); // 7 days
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public Optional<RefreshToken> findByTokenId(String tokenId){
        return refreshTokenRepository.findByTokenId(tokenId);
    }

    public RefreshToken verifyTokenExpiration(RefreshToken token){
        if (token.getExpiryDate().isBefore(java.time.Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token has expired, please login again");
        }
        return token;
    }
}
