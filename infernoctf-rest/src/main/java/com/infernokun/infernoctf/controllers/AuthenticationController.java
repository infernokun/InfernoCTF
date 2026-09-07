package com.infernokun.infernoctf.controllers;

import com.infernokun.infernoctf.config.RefreshTokenCookieFactory;
import com.infernokun.infernoctf.models.ApiResponse;
import com.infernokun.infernoctf.models.dto.AuthSession;
import com.infernokun.infernoctf.models.dto.LoginResponseDTO;
import com.infernokun.infernoctf.models.dto.RefreshRequestDTO;
import com.infernokun.infernoctf.models.dto.RegistrationDTO;
import com.infernokun.infernoctf.services.AuthenticationService;
import com.infernokun.infernoctf.services.WebSocketTicketService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final WebSocketTicketService webSocketTicketService;
    private final RefreshTokenCookieFactory refreshCookies;

    public AuthenticationController(AuthenticationService authenticationService,
                                    WebSocketTicketService webSocketTicketService,
                                    RefreshTokenCookieFactory refreshCookies) {
        this.authenticationService = authenticationService;
        this.webSocketTicketService = webSocketTicketService;
        this.refreshCookies = refreshCookies;
    }

    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<ApiResponse<Boolean>> registerUser(@RequestBody RegistrationDTO registrationDTO) {
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(HttpStatus.OK.value())
                .message("User registered successfully.")
                .data(authenticationService.registerUser(registrationDTO))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginUser(@RequestBody RegistrationDTO user,
                                                                   HttpServletRequest request) {
        AuthSession session = authenticationService.loginUser(
                user.getUsername(), user.getPassword(), clientAddress(request));
        return sessionResponse(session, "Login successful.");
    }

    /**
     * Unauthenticated by design: the access token has expired by the time a client calls this,
     * so the refresh grant is the credential. Browsers send it in the cookie; the body is a
     * fallback for non-browser clients.
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refresh(
            @CookieValue(name = "${app.jwt.refresh-cookie.name:refreshToken}", required = false)
            String cookieToken,
            @RequestBody(required = false) RefreshRequestDTO body) {

        String presented = cookieToken != null && !cookieToken.isBlank()
                ? cookieToken
                : (body == null ? null : body.getRefreshToken());

        return sessionResponse(authenticationService.refresh(presented), "Token refreshed.");
    }

    /**
     * Single-use, ~30s ticket for the WebSocket handshake. The browser WebSocket API cannot send
     * an Authorization header, and URLs get logged, so the access token must not go in one.
     */
    @GetMapping("/ws-ticket")
    public ResponseEntity<ApiResponse<Map<String, String>>> webSocketTicket(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.OK.value())
                .message("WebSocket ticket issued.")
                .data(Map.of("ticket", webSocketTicketService.issue(authentication.getName())))
                .build());
    }

    /** Revokes the caller's session. The user comes from the token, never from the request. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(Authentication authentication) {
        authenticationService.logout(authentication.getName());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookies.expire().toString())
                .build();
    }

    private ResponseEntity<ApiResponse<LoginResponseDTO>> sessionResponse(AuthSession session, String message) {
        ResponseCookie cookie = refreshCookies.create(session.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.<LoginResponseDTO>builder()
                        .code(HttpStatus.OK.value())
                        .message(message)
                        .data(new LoginResponseDTO(session.accessToken(), session.user()))
                        .build());
    }

    /** Real client, not the proxy: server.forward-headers-strategy is native. */
    private static String clientAddress(HttpServletRequest request) {
        String address = request.getRemoteAddr();
        return address == null ? "unknown" : address;
    }
}
