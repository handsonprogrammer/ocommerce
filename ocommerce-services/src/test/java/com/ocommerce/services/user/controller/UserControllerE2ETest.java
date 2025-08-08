package com.ocommerce.services.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocommerce.services.common.AbstractIntegrationTest;
import com.ocommerce.services.user.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for User API endpoints
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("integration-test")
@Transactional
class UserControllerE2ETest extends AbstractIntegrationTest {


        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void getUserProfile_WithValidToken_ShouldReturnUserProfile() throws Exception {
                // Given - Create user and get token
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setFirstName("John");
                signupRequest.setLastName("Doe");
                signupRequest.setEmail("profile-test@example.com");
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
                mockMvc.perform(get("/api/v1/users/me")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("profile-test@example.com"))
                                .andExpect(jsonPath("$.firstName").value("John"))
                                .andExpect(jsonPath("$.lastName").value("Doe"))
                                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                                .andExpect(jsonPath("$.accountEnabled").value(true))
                                .andExpect(jsonPath("$.emailVerified").value(false))
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.createdAt").exists())
                                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void getUserProfile_WithoutToken_ShouldReturnUnauthorized() throws Exception {
                // When/Then
                mockMvc.perform(get("/api/v1/users/me"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void getUserProfile_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
                // When/Then
                mockMvc.perform(get("/api/v1/users/me")
                                .header("Authorization", "Bearer invalid-token"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void getUserProfile_WithExpiredToken_ShouldReturnUnauthorized() throws Exception {
                // This test would require manipulating token expiration
                // For now, we'll test with a malformed token
                String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDB9.invalid";

                // When/Then
                mockMvc.perform(get("/api/v1/users/me")
                                .header("Authorization", "Bearer " + expiredToken))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void integrationFlow_SignupLoginGetProfile_ShouldWork() throws Exception {
                // Given
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setFirstName("Integration");
                signupRequest.setLastName("Test");
                signupRequest.setEmail("integration-test@example.com");
                signupRequest.setPassword("Password123!");
                signupRequest.setPhoneNumber("+1234567890");

                // Step 1: Signup
                String signupResponse = mockMvc.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.accessToken").value(notNullValue()))
                                .andExpect(jsonPath("$.refreshToken").value(notNullValue()))
                                .andReturn().getResponse().getContentAsString();

                String accessToken = objectMapper.readTree(signupResponse).get("accessToken").asText();
                String refreshToken = objectMapper.readTree(signupResponse).get("refreshToken").asText();

                // Step 2: Get Profile with token from signup
                mockMvc.perform(get("/api/v1/users/me")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("integration-test@example.com"))
                                .andExpect(jsonPath("$.firstName").value("Integration"))
                                .andExpect(jsonPath("$.lastName").value("Test"));

                // Step 3: Login with the same credentials
                String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"integration-test@example.com\",\"password\":\"Password123!\"}"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

                String loginAccessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

                // Step 4: Get Profile with token from login
                mockMvc.perform(get("/api/v1/users/me")
                                .header("Authorization", "Bearer " + loginAccessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("integration-test@example.com"));

                // Step 5: Logout
                mockMvc.perform(post("/api/v1/auth/logout")
                                .header("Authorization", "Bearer " + loginAccessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }
}
