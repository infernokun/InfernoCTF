package com.infernokun.infernoctf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Builds the httpOnly cookie the refresh grant travels in, so page scripts cannot read it.
 *
 * <p>The path defaults to {@code /} because the browser-visible path is not knowable
 * server-side: nginx rewrites {@code /api/} to {@code /infernoctf-rest/api/}, so a path built
 * from the servlet context path would be one the browser never sends back.
 *
 * <p>SameSite defaults to Lax, which suits a same-origin SPA. Another origin needs
 * {@code None}, and therefore {@code Secure}.
 */
@Component
public class RefreshTokenCookieFactory {

    private final String name;
    private final String path;
    private final String sameSite;
    private final boolean secure;
    private final Duration maxAge;

    public RefreshTokenCookieFactory(
            @Value("${app.jwt.refresh-cookie.name:refreshToken}") String name,
            @Value("${app.jwt.refresh-cookie.path:/}") String path,
            @Value("${app.jwt.refresh-cookie.same-site:Lax}") String sameSite,
            @Value("${app.jwt.refresh-cookie.secure:true}") boolean secure,
            @Value("${app.jwt.refresh-token-ttl:P14D}") Duration maxAge) {
        this.name = name;
        this.path = path;
        this.sameSite = sameSite;
        this.secure = secure;
        this.maxAge = maxAge;
    }

    public String getName() {
        return name;
    }

    public ResponseCookie create(String refreshToken) {
        return base(refreshToken).maxAge(maxAge).build();
    }

    /** An expired cookie with the same attributes, which is how a browser drops one. */
    public ResponseCookie expire() {
        return base("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path);
    }
}
