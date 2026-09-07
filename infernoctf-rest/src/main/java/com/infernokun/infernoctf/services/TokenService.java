package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.models.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class TokenService {
    /** Must match the issuer the decoder validates in SecurityConfig. */
    public static final String ISSUER = "infernoctf";

    private final JwtEncoder jwtEncoder;
    private final Duration accessTokenTtl;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${app.jwt.access-token-ttl:PT15M}") Duration accessTokenTtl) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenTtl = accessTokenTtl;
    }

    /**
     * The subject is the user id, so that is what {@code Authentication#getName()} returns on
     * every authenticated request. Refresh grants are issued separately by
     * {@link RefreshTokenService}.
     */
    public String generateJwt(User user) {
        Instant now = Instant.now();

        String roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenTtl))
                .subject(user.getId())
                .claim("roles", roles)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
