package com.infernokun.infernoctf.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived, single-use tickets that authenticate a WebSocket handshake.
 *
 * <p>The browser WebSocket API cannot set an {@code Authorization} header, so the credential has
 * to go in the URL, and URLs reach proxy and access logs. A ticket is exchanged for the access
 * token over a normal authenticated request and destroyed on redemption, so a logged one is
 * already useless.
 *
 * <p>In memory, like the sessions themselves; single-instance only.
 */
@Service
public class WebSocketTicketService {
    private static final Duration TICKET_TTL = Duration.ofSeconds(30);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();

    private record Ticket(String userId, Instant expiresAt) {
    }

    public String issue(String userId) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        tickets.put(ticket, new Ticket(userId, Instant.now().plus(TICKET_TTL)));
        return ticket;
    }

    /** Returns the user id and invalidates the ticket. Empty if unknown, expired or used. */
    public Optional<String> redeem(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return Optional.empty();
        }
        Ticket found = tickets.remove(ticket);
        if (found == null || found.expiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(found.userId());
    }

    /** Unredeemed tickets would otherwise accumulate for the life of the process. */
    @Scheduled(fixedDelay = 60_000)
    void purgeExpired() {
        Instant now = Instant.now();
        tickets.values().removeIf(ticket -> ticket.expiresAt().isBefore(now));
    }
}
