package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.TokenException;
import com.infernokun.infernoctf.models.entities.RefreshToken;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.repositories.RefreshTokenRepository;
import com.infernokun.infernoctf.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public void createRefreshToken(String username, String token, Instant expiration) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Find existing token
        Optional<RefreshToken> existingTokenOpt = this.refreshTokenRepository.findByUserId(user.getId());

        if (existingTokenOpt.isPresent()) {
            RefreshToken existingToken = existingTokenOpt.get();

            existingToken.setToken(token);
            existingToken.setCreationDate(Instant.now());
            existingToken.setExpirationDate(expiration);
            refreshTokenRepository.save(existingToken);
        } else {
            // If no existing token, create a new one
            RefreshToken newToken = RefreshToken.builder()
                    .user(user)
                    .token(token)
                    .creationDate(Instant.now())
                    .expirationDate(expiration)
                    .build();

            refreshTokenRepository.save(newToken);
        }
    }

    public Optional<RefreshToken> findByUserId(String id) {
        // Bypass cache for User lookups to avoid serialization issues
        return this.refreshTokenRepository.findByUserId(id);
    }

    public RefreshToken findByToken(String token) {
        // Bypass cache to avoid serialization issues
        return this.refreshTokenRepository.findByToken(token).orElseThrow(
                () -> new TokenException("Failed to find token."));
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpirationDate().compareTo(Instant.now()) < 0) {
            // Token expired - remove from DB and cache
            this.refreshTokenRepository.delete(token);

            throw new RuntimeException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;
    }

    @Transactional
    public Optional<RefreshToken> deleteToken(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return refreshTokenRepository.deleteByUserId(id);
        } else {
            return Optional.empty();
        }
    }
}