package com.infernokun.controllers;

import com.infernokun.exceptions.AuthFailedException;
import com.infernokun.exceptions.WrongPasswordException;
import com.infernokun.models.entities.RefreshToken;
import com.infernokun.models.dto.LoginResponseDTO;
import com.infernokun.models.dto.RegistrationDTO;
import com.infernokun.models.entities.User;
import com.infernokun.services.AuthenticationService;
import com.infernokun.services.RefreshTokenService;
import com.infernokun.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("auth")
@CrossOrigin
public class AuthenticationController {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationController(AuthenticationService authenticationService, UserService userService, RefreshTokenService refreshTokenService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping(value = "/register", produces = "text/plain;charset=UTF-8", consumes = "application/json")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password are required.");
        }

        boolean userExists = this.userService.existsByUsernameOrEmail(user);
        if (userExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists!");
        }

        this.authenticationService.registerUser(user);
        LOGGER.info("User registered: {}", user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body("User registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody RegistrationDTO user) {
        try {
            LoginResponseDTO loginResponseDTO = this.authenticationService.loginUser(
                    user.getUsername(), user.getPassword());

            return ResponseEntity.ok(loginResponseDTO);
        } catch (WrongPasswordException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (AuthFailedException ex) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/token")
    public ResponseEntity<LoginResponseDTO> revalidateToken(@RequestBody String token) {
        /* Simulate backend delay
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        return ResponseEntity.ok(this.authenticationService.revalidateToken(token));
    }

    @PostMapping("/token/check")
    public ResponseEntity<Boolean> checkToken(@RequestBody String token) {
        Optional<RefreshToken> refreshToken = this.refreshTokenService.findByToken(token);

        /* Simulate backend delay
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        if (refreshToken.isPresent()) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }

    @DeleteMapping("/token/logout/{username}")
    public ResponseEntity<?> logoutUser(@PathVariable String username) {
        Optional<RefreshToken> refreshTokenOptional = refreshTokenService.deleteToken(username);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (refreshTokenOptional.isPresent()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok("Something crazy....");
        }
    }
}
