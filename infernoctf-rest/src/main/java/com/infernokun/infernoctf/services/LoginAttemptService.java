package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.TooManyAttemptsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Throttles password guessing with two counters, because each alone has a hole:
 * <ul>
 *   <li><b>account + source</b>: brute force against one account. Keying on the account alone
 *       would let anyone lock out a known user at will.</li>
 *   <li><b>source</b>: credential stuffing, where each attempt uses a different account and so
 *       never trips the first counter.</li>
 * </ul>
 *
 * <p>In-memory, so the limit is per instance.
 */
@Service
@Slf4j
public class LoginAttemptService {

    private final int maxAttemptsPerAccount;
    private final int maxAttemptsPerSource;
    private final Duration window;

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${app.login.max-attempts-per-account:5}") int maxAttemptsPerAccount,
            @Value("${app.login.max-attempts-per-source:20}") int maxAttemptsPerSource,
            @Value("${app.login.window:PT15M}") Duration window) {
        this.maxAttemptsPerAccount = maxAttemptsPerAccount;
        this.maxAttemptsPerSource = maxAttemptsPerSource;
        this.window = window;
    }

    private static final class Attempts {
        private final AtomicInteger count = new AtomicInteger();
        private volatile Instant windowEnds;

        Attempts(Duration window) {
            this.windowEnds = Instant.now().plus(window);
        }
    }

    /** Throws if either counter is over its limit. Call before verifying the password. */
    public void checkNotBlocked(String username, String sourceAddress) {
        if (isBlocked(accountKey(username, sourceAddress), maxAttemptsPerAccount)
                || isBlocked(sourceKey(sourceAddress), maxAttemptsPerSource)) {
            log.warn("Blocked a login attempt for '{}' from {}: too many failures", username, sourceAddress);
            throw new TooManyAttemptsException(
                    "Too many failed login attempts. Try again later.");
        }
    }

    public void recordFailure(String username, String sourceAddress) {
        increment(accountKey(username, sourceAddress));
        increment(sourceKey(sourceAddress));
    }

    /** Clears the account counter, but not the per-source one. */
    public void recordSuccess(String username, String sourceAddress) {
        attempts.remove(accountKey(username, sourceAddress));
    }

    private boolean isBlocked(String key, int limit) {
        Attempts record = attempts.get(key);
        if (record == null) {
            return false;
        }
        if (record.windowEnds.isBefore(Instant.now())) {
            attempts.remove(key);
            return false;
        }
        return record.count.get() >= limit;
    }

    private void increment(String key) {
        attempts.compute(key, (ignored, existing) -> {
            if (existing == null || existing.windowEnds.isBefore(Instant.now())) {
                existing = new Attempts(window);
            }
            existing.count.incrementAndGet();
            return existing;
        });
    }

    private static String accountKey(String username, String sourceAddress) {
        String normalised = username == null ? "" : username.toLowerCase(Locale.ROOT);
        return "account:" + normalised + '|' + sourceAddress;
    }

    private static String sourceKey(String sourceAddress) {
        return "source:" + sourceAddress;
    }

    @Scheduled(fixedDelay = 300_000)
    void purgeExpired() {
        Instant now = Instant.now();
        attempts.values().removeIf(record -> record.windowEnds.isBefore(now));
    }
}
