package com.infernokun.infernoctf.config;

import com.infernokun.infernoctf.services.TokenService;
import com.infernokun.infernoctf.utils.RSAKeyProperties;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RSAKeyProperties keys;
    private final List<String> allowedOrigins;

    public SecurityConfig(RSAKeyProperties keys,
                          @Value("${app.cors.allowed-origins:http://localhost:4301}") String allowedOrigins) {
        this.keys = keys;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless bearer-token API: no session cookie to ride, so CSRF adds nothing.
                .csrf(AbstractHttpConfigurer::disable)
                // Must be here rather than an MVC mapping: the security chain runs first, so
                // preflight to a secured endpoint is decided before MVC is consulted.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/token").permitAll()
                        .requestMatchers("/actuator/health/**", "/error").permitAll()
                        // Handshake auth happens in WebSocketAuthInterceptor, via a ticket.
                        .requestMatchers("/socket-handler/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/user").hasAnyRole("ADMIN", "DEVELOPER")
                        .requestMatchers(HttpMethod.POST, "/api/user/**").hasAnyRole("ADMIN", "DEVELOPER")
                        .requestMatchers(HttpMethod.PUT, "/api/user/**").hasAnyRole("ADMIN", "DEVELOPER")
                        .requestMatchers(HttpMethod.DELETE, "/api/user/**").hasAnyRole("ADMIN", "DEVELOPER")
                        .requestMatchers(HttpMethod.POST, "/api/ctf-entity/**", "/api/room/**")
                        .hasAnyRole("ADMIN", "DEVELOPER", "CREATOR", "FACILITATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/ctf-entity/**", "/api/room/**")
                        .hasAnyRole("ADMIN", "DEVELOPER", "CREATOR", "FACILITATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/ctf-entity/**", "/api/room/**")
                        .hasAnyRole("ADMIN", "DEVELOPER", "CREATOR", "FACILITATOR")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                // Bare 401, no WWW-Authenticate challenge: the Angular interceptor keys its
                // refresh-and-retry on it.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        // Needed for the refresh cookie cross-origin. Requires an explicit origin list;
        // browsers reject credentials combined with a wildcard origin.
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(this.passwordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(keys.getPublicKey()).build();
        // Without this only signature and expiry are checked, not the issuer.
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.ISSUER));
        return decoder;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(keys.getPublicKey()).privateKey(keys.getPrivateKey()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // The claim holds bare role names ("ADMIN"); the prefix is added here, so don't also
        // add it in User#getAuthorities.
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
