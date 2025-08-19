package com.ocommerce.services.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocommerce.services.user.dto.LoginRequest;
import com.ocommerce.services.user.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for Authentication API endpoints
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Transactional
class AuthControllerE2ETest{


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signup_WithValidData_ShouldCreateUserAndReturnCreated() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");

        // When/Then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void signup_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("invalid-email");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");

        // When/Then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void signup_WithExistingEmail_ShouldReturnConflict() throws Exception {
        // Given - First signup
        SignupRequest firstRequest = new SignupRequest();
        firstRequest.setFirstName("John");
        firstRequest.setLastName("Doe");
        firstRequest.setEmail("existing@example.com");
        firstRequest.setPassword("Password123!");
        firstRequest.setPhoneNumber("+1234567890");

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)));

        // Given - Second signup with same email
        SignupRequest secondRequest = new SignupRequest();
        secondRequest.setFirstName("Jane");
        secondRequest.setLastName("Smith");
        secondRequest.setEmail("existing@example.com");
        secondRequest.setPassword("Password456!");
        secondRequest.setPhoneNumber("+0987654321");

        // When/Then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("User already exists"));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() throws Exception {
        // Given - Create user first
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("login-test@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login-test@example.com");
        loginRequest.setPassword("Password123!");

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("wrongpassword");

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Authentication failed"));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAuthResponse() throws Exception {
        // Given - Create user and login
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("refresh-test@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");

        String signupResponse = mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract refresh token from signup response
        String refreshToken = objectMapper.readTree(signupResponse).get("refreshToken").asText();

        // When/Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid-refresh-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Invalid refresh token"));
    }

    @Test
    void logout_WithNoToken_ShouldReturnBadRequest() throws Exception {
        // Given - Create user and login
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("logout-test@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");

        String signupResponse = mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract access token from signup response
        String accessToken = objectMapper.readTree(signupResponse).get("accessToken").asText();

        // When/Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_WithValidToken_ShouldReturnOk() throws Exception {
        // Given - Create user and login
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("logout-test@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");

        String signupResponse = mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract access token from signup response
        String accessToken = objectMapper.readTree(signupResponse).get("accessToken").asText();
        String refreshToken = objectMapper.readTree(signupResponse).get("refreshToken").asText();

        // When/Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \""+refreshToken+"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void logout_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"\"}"))
                .andExpect(status().isUnauthorized());
    }
}
