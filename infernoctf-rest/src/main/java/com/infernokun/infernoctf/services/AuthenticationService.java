package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.AuthFailedException;
import com.infernokun.infernoctf.exceptions.WrongPasswordException;
import com.infernokun.infernoctf.models.dto.RegistrationDTO;
import com.infernokun.infernoctf.models.entities.RefreshToken;
import com.infernokun.infernoctf.models.dto.LoginResponseDTO;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.models.enums.Role;
import com.infernokun.infernoctf.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService, RefreshTokenService refreshTokenService, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    public boolean registerUser(RegistrationDTO user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            throw new AuthFailedException("Username and password required!");
        }

        if (userService.existsByUsername(user.getUsername())) {
            throw new AuthFailedException("Username already exists!");
        }

        String encodedPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        User newUser = new User(user.getUsername(), user.getPassword());
        newUser.setRole(Role.MEMBER);

        userRepository.save(newUser);

        log.info("User registered: {}", newUser.getUsername());
        return true;
    }

    public LoginResponseDTO loginUser(String username, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            User loggedInUser = userService.findUserByUsername(username);
            loggedInUser.setLastLogin(LocalDateTime.now());
            userService.updateUser(loggedInUser);

            User user = (User) auth.getPrincipal();
            String token = tokenService.generateJwt(user);

            return new LoginResponseDTO(token, user);
        } catch (BadCredentialsException e) {
            throw new WrongPasswordException("Invalid username or password");
        } catch (AuthenticationException e) {
            throw new AuthFailedException("Authentication failed");
        }
    }

    public LoginResponseDTO revalidateToken(String oldToken) {
        log.info("old token: {}", oldToken);
        RefreshToken oldRefreshToken = this.refreshTokenService.findByToken(oldToken);
        log.info("OLD REFRESH TOKEN - Found!");
        if (Objects.equals(oldRefreshToken.getToken(), oldToken)) {
            log.info("OLD REFRESH TOKEN - Matches database for user {}!", oldRefreshToken.getUser().getId());
            Optional<User> user = this.userRepository.findById(oldRefreshToken.getUser().getId());
            if (user.isPresent()) {
                log.info("OLD REFRESH TOKEN - Token replaced!");
                String token = this.tokenService.generateJwt(user.get());
                return new LoginResponseDTO(token, user.get());
            }
        }
        return null;
    }
}