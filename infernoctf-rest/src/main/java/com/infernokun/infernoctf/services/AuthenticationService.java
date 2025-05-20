package com.infernokun.services;

import com.infernokun.exceptions.AuthFailedException;
import com.infernokun.exceptions.WrongPasswordException;
import com.infernokun.models.entities.RefreshToken;
import com.infernokun.models.dto.LoginResponseDTO;
import com.infernokun.models.entities.User;
import com.infernokun.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    private final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    public void registerUser(User user) {
        String encodedPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
    }

    public LoginResponseDTO loginUser(String username, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = tokenService.generateJwt(userDetails);
            return new LoginResponseDTO(token);
        } catch (BadCredentialsException e) {
            throw new WrongPasswordException("Invalid username or password");
        } catch (AuthenticationException e) {
            throw new AuthFailedException("Authentication failed");
        }
    }

    public LoginResponseDTO revalidateToken(String oldToken) {
        LOGGER.info("old token: {}", oldToken);
        Optional<RefreshToken> oldRefreshToken = this.refreshTokenService.findByToken(oldToken);
        LOGGER.info("OLD REFRESH TOKEN - Attempting!");
        if (oldRefreshToken.isPresent()) {
            LOGGER.info("OLD REFRESH TOKEN - Found!");
            if (Objects.equals(oldRefreshToken.get().getToken(), oldToken)) {
                LOGGER.info("OLD REFRESH TOKEN - Matches database for user {}!", oldRefreshToken.get().getUser().getId());
                Optional<User> user = this.userRepository.findById(oldRefreshToken.get().getUser().getId());
                if (user.isPresent()) {
                    LOGGER.info("OLD REFRESH TOKEN - Token replaced!");
                    String token = this.tokenService.generateJwt(user.get());
                    return new LoginResponseDTO(token);
                }
            }
        }
        return null;
    }
}
