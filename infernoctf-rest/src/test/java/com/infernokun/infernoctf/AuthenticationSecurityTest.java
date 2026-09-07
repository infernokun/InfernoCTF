package com.infernokun.infernoctf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Covers the authentication and authorization rules themselves. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationSecurityTest {

    private static final String ADMIN_USERNAME = "infernoctf_admin";
    private static final String ADMIN_PASSWORD = "test-admin-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Public vs. protected

    @Test
    void protectedEndpointRejectsAnonymousCallers() throws Exception {
        mockMvc.perform(get("/api/user/by").param("username", ADMIN_USERNAME))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listingAllUsersRejectsAnonymousCallers() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsAGarbageToken() throws Exception {
        mockMvc.perform(get("/api/user").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginIsPublic() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void loginWithABadPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(ADMIN_USERNAME, "not-the-password")))
                .andExpect(status().isUnauthorized());
    }

    // Role enforcement

    @Test
    void listingAllUsersIsAdminOnly() throws Exception {
        JsonNode member = registerAndLogin();

        mockMvc.perform(get("/api/user").header("Authorization", bearer(member)))
                .andExpect(status().isForbidden());

        JsonNode admin = login(ADMIN_USERNAME, ADMIN_PASSWORD);
        mockMvc.perform(get("/api/user").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void anAuthenticatedMemberCanReadTheirOwnUser() throws Exception {
        JsonNode member = registerAndLogin();
        String id = member.path("user").path("id").asText();

        mockMvc.perform(get("/api/user/by").param("id", id).header("Authorization", bearer(member)))
                .andExpect(status().isOk());
    }

    // Token issue and refresh

    @Test
    void theRefreshGrantIsAnHttpOnlyCookieAndNeverAppearsInTheBody() throws Exception {
        MvcResult result = loginResult(ADMIN_USERNAME, ADMIN_PASSWORD);

        assertThat(readData(result).path("jwt").asText()).isNotBlank();
        // If the grant were in the JSON body, any XSS could read it.
        assertThat(result.getResponse().getContentAsString()).doesNotContain("refreshToken");

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("refreshToken=").contains("HttpOnly");

        Cookie cookie = result.getResponse().getCookie("refreshToken");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNotBlank();
        // The grant must not be the access token.
        assertThat(cookie.getValue()).isNotEqualTo(readData(result).path("jwt").asText());
    }

    @Test
    void refreshGrantsAreSingleUse() throws Exception {
        Cookie grant = refreshCookie(loginResult(ADMIN_USERNAME, ADMIN_PASSWORD));

        MvcResult refreshed = mockMvc.perform(post("/api/auth/token").cookie(grant))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(readData(refreshed).path("jwt").asText()).isNotBlank();
        assertThat(refreshCookie(refreshed).getValue()).isNotEqualTo(grant.getValue());

        // Replaying the consumed grant must fail.
        mockMvc.perform(post("/api/auth/token").cookie(grant))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anUnknownRefreshGrantIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/token").cookie(new Cookie("refreshToken", "made-up-token")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshWithNoGrantAtAllIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutRevokesTheCallersRefreshGrantAndClearsTheCookie() throws Exception {
        MvcResult login = loginResult(ADMIN_USERNAME, ADMIN_PASSWORD);
        Cookie grant = refreshCookie(login);

        MvcResult logout = mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + readData(login).path("jwt").asText()))
                .andExpect(status().isNoContent())
                .andReturn();

        assertThat(logout.getResponse().getCookie("refreshToken").getMaxAge()).isZero();

        mockMvc.perform(post("/api/auth/token").cookie(grant))
                .andExpect(status().isBadRequest());
    }

    // WebSocket handshake tickets

    @Test
    void webSocketTicketsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/ws-ticket"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anAuthenticatedUserGetsAWebSocketTicket() throws Exception {
        JsonNode session = registerAndLogin();

        JsonNode ticket = readData(mockMvc.perform(get("/api/auth/ws-ticket")
                        .header("Authorization", bearer(session)))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(ticket.path("ticket").asText()).isNotBlank();
    }

    // Login throttling

    @Test
    void repeatedBadPasswordsAreThrottled() throws Exception {
        String username = "throttled-" + UUID.randomUUID();
        register(username, "member-password");

        // The account limit is 5 per window.
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(credentials(username, "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }

        // Refused before the password is checked, including the correct one.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, "wrong-password")))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, "member-password")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void logoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    // Helpers

    private JsonNode registerAndLogin() throws Exception {
        String username = "member-" + UUID.randomUUID();
        String password = "member-password";
        register(username, password);
        return login(username, password);
    }

    private void register(String username, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, password)))
                .andExpect(status().isOk());
    }

    private JsonNode login(String username, String password) throws Exception {
        return readData(loginResult(username, password));
    }

    private MvcResult loginResult(String username, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, password)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private static Cookie refreshCookie(MvcResult result) {
        Cookie cookie = result.getResponse().getCookie("refreshToken");
        assertThat(cookie).as("refresh cookie").isNotNull();
        return cookie;
    }

    private JsonNode readData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private String credentials(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(
                java.util.Map.of("username", username, "password", password));
    }

    private static String bearer(JsonNode session) {
        return "Bearer " + session.path("jwt").asText();
    }
}
