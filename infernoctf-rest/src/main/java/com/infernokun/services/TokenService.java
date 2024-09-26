package com.infernokun.services;

import com.infernokun.models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenService refreshTokenService;
    private final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);
    private final UserService userService;

    public TokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, RefreshTokenService refreshTokenService, UserService userService) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    public String generateJwt(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(60);

        String scope = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(userDetails.getUsername())
                .claim("roles", scope)
                .build();

        String token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        this.refreshTokenService.createRefreshToken(userDetails.getUsername(), token, expiration);
        return token;
    }

    @Bean
    private void applicationJWT() {
        Optional<List<User>> usersOpt = this.userService.findAllUsers();

        User admin = usersOpt
                .flatMap(users -> users.stream()
                        .filter(user -> "admin".equals(user.getUsername()))
                        .findFirst())
                .orElse(new User("admin", "defaultPassword"));

        String scope = admin.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(365, ChronoUnit.DAYS))
                .subject("admin")
                .claim("roles", scope)
                .build();

        LOGGER.info("TOKEN: {}", this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue());
    }
}
