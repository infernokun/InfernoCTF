package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.AuthFailedException;
import com.infernokun.infernoctf.exceptions.WrongPasswordException;
import com.infernokun.infernoctf.models.dto.AuthSession;
import com.infernokun.infernoctf.models.dto.RegistrationDTO;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.models.enums.Role;
import com.infernokun.infernoctf.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager, TokenService tokenService,
                                 RefreshTokenService refreshTokenService, UserService userService,
                                 LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
        this.loginAttemptService = loginAttemptService;
    }

    public boolean registerUser(RegistrationDTO registration) {
        if (registration == null || registration.getUsername() == null || registration.getPassword() == null) {
            throw new AuthFailedException("Username and password required!");
        }

        if (userService.existsByUsername(registration.getUsername())) {
            throw new AuthFailedException("Username already exists!");
        }

        User newUser = new User(registration.getUsername(),
                this.passwordEncoder.encode(registration.getPassword()));
        newUser.setRole(Role.MEMBER);

        userRepository.save(newUser);

        log.info("User registered: {}", newUser.getUsername());
        return true;
    }

    @Transactional
    public AuthSession loginUser(String username, String password, String sourceAddress) {
        // Before touching the password, so a blocked caller cannot keep probing.
        loginAttemptService.checkNotBlocked(username, sourceAddress);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            User loggedInUser = userService.findUserByUsername(username);
            loggedInUser.setLastLogin(LocalDateTime.now());
            userService.updateUser(loggedInUser);
            loginAttemptService.recordSuccess(username, sourceAddress);

            return new AuthSession(
                    tokenService.generateJwt(loggedInUser),
                    refreshTokenService.issue(loggedInUser),
                    loggedInUser);
        } catch (BadCredentialsException e) {
            loginAttemptService.recordFailure(username, sourceAddress);
            throw new WrongPasswordException("Invalid username or password");
        } catch (AuthenticationException e) {
            loginAttemptService.recordFailure(username, sourceAddress);
            throw new AuthFailedException("Authentication failed");
        }
    }

    /**
     * Exchanges a refresh grant for a new access token and a new grant. Validation, expiry and
     * single-use rotation live in {@link RefreshTokenService#rotate}, which throws on failure.
     */
    @Transactional
    public AuthSession refresh(String refreshToken) {
        User user = refreshTokenService.rotate(refreshToken);
        log.debug("Refreshed session for user {}", user.getId());
        return new AuthSession(
                tokenService.generateJwt(user),
                refreshTokenService.issue(user),
                user);
    }

    /** The user id comes from the token, never from the client. */
    @Transactional
    public void logout(String userId) {
        refreshTokenService.revokeByUserId(userId);
    }
}
