package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.TokenException;
import com.infernokun.infernoctf.models.entities.RefreshToken;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Issues and redeems refresh tokens: 256 bits of {@link SecureRandom}, stored only as a SHA-256
 * hash. Single-use, so a captured token stops working once the real client uses it.
 *
 * <p>One grant per user, so one active session per account: logging in elsewhere invalidates
 * the earlier session.
 */
@Service
public class RefreshTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration tokenTtl;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               @Value("${app.jwt.refresh-token-ttl:P14D}") Duration tokenTtl) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenTtl = tokenTtl;
    }

    /** Returns the raw token, which is not recoverable afterwards. */
    @Transactional
    public String issue(User user) {
        byte[] raw = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        Instant now = Instant.now();

        RefreshToken grant = refreshTokenRepository.findByUserId(user.getId())
                .orElseGet(() -> RefreshToken.builder().user(user).build());
        grant.setTokenHash(hash(token));
        grant.setCreationDate(now);
        grant.setExpirationDate(now.plus(tokenTtl));
        refreshTokenRepository.save(grant);

        return token;
    }

    /** Validates the presented token and consumes it, deleting expired grants on the way. */
    @Transactional
    public User rotate(String presentedToken) {
        if (presentedToken == null || presentedToken.isBlank()) {
            throw new TokenException("Refresh token is required.");
        }

        RefreshToken grant = refreshTokenRepository.findByTokenHash(hash(presentedToken))
                .orElseThrow(() -> new TokenException("Refresh token is not valid."));

        if (grant.getExpirationDate() == null || grant.getExpirationDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(grant);
            throw new TokenException("Refresh token has expired. Please log in again.");
        }

        User user = grant.getUser();
        refreshTokenRepository.delete(grant);
        return user;
    }

    @Transactional
    public void revokeByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
