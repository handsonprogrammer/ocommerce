package com.ocommerce.services.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocommerce.services.common.AbstractIntegrationTest;
import com.ocommerce.services.user.dto.AddressRequest;
import com.ocommerce.services.user.dto.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E Integration tests for AddressController
 * Tests all address management endpoints with real database and full Spring context
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("integration-test")
@Transactional
public class AddressControllerE2ETest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private AddressRequest testAddressRequest;

    @BeforeEach
    void setUp() throws Exception {
        // Create and authenticate a user first
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("address-test@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");

        String signupResponse = mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract access token
        accessToken = objectMapper.readTree(signupResponse).get("accessToken").asText();

        // Set up test address request
        testAddressRequest = new AddressRequest();
        testAddressRequest.setType("home");
        testAddressRequest.setStreetAddress("123 Main St");
        testAddressRequest.setAddressLine2("Apt 4B");
        testAddressRequest.setCity("New York");
        testAddressRequest.setState("NY");
        testAddressRequest.setPostalCode("10001");
        testAddressRequest.setCountry("United States");
    }

    @Test
    void createAddress_WithValidData_ShouldCreateAddressAndReturnCreated() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("home"))
                .andExpect(jsonPath("$.streetAddress").value("123 Main St"))
                .andExpect(jsonPath("$.addressLine2").value("Apt 4B"))
                .andExpect(jsonPath("$.city").value("New York"))
                .andExpect(jsonPath("$.state").value("NY"))
                .andExpect(jsonPath("$.postalCode").value("10001"))
                .andExpect(jsonPath("$.country").value("United States"))
                .andExpect(jsonPath("$.isDefault").value(false))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void createAddress_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - invalid request with missing required fields
        AddressRequest invalidRequest = new AddressRequest();
        // Missing type, streetAddress, city, etc.

        // When/Then
        mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void createAddress_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAddress_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserAddresses_WithValidToken_ShouldReturnAddressList() throws Exception {
        // Given - Create an address first
        String createResponse = mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // When/Then
        mockMvc.perform(get("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("home"))
                .andExpect(jsonPath("$[0].streetAddress").value("123 Main St"));
    }

    @Test
    void getUserAddresses_WithNoAddresses_ShouldReturnEmptyList() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUserAddresses_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/address"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAddressById_WithValidId_ShouldReturnAddress() throws Exception {
        // Given - Create an address first
        String createResponse = mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String addressId = objectMapper.readTree(createResponse).get("id").asText();

        // When/Then
        mockMvc.perform(get("/api/v1/address/{addressId}", addressId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressId))
                .andExpect(jsonPath("$.type").value("home"))
                .andExpect(jsonPath("$.streetAddress").value("123 Main St"));
    }

    @Test
    void getAddressById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/address/{addressId}", "550e8400-e29b-41d4-a716-446655440000")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Address not found"));
    }

    @Test
    void updateAddress_WithValidData_ShouldUpdateAndReturnAddress() throws Exception {
        // Given - Create an address first
        String createResponse = mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String addressId = objectMapper.readTree(createResponse).get("id").asText();

        // Update the address data
        testAddressRequest.setType("work");
        testAddressRequest.setStreetAddress("456 Business Ave");
        testAddressRequest.setCity("Boston");
        testAddressRequest.setState("MA");
        testAddressRequest.setPostalCode("02101");

        // When/Then
        mockMvc.perform(put("/api/v1/address/{addressId}", addressId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("work"))
                .andExpect(jsonPath("$.streetAddress").value("456 Business Ave"))
                .andExpect(jsonPath("$.city").value("Boston"))
                .andExpect(jsonPath("$.state").value("MA"))
                .andExpect(jsonPath("$.postalCode").value("02101"));
    }

    @Test
    void updateAddress_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // When/Then
        mockMvc.perform(put("/api/v1/address/{addressId}", "550e8400-e29b-41d4-a716-446655440000")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Address not found"));
    }

    @Test
    void updateAddress_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - Create an address first
        String createResponse = mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String addressId = objectMapper.readTree(createResponse).get("id").asText();

        // Invalid update data
        AddressRequest invalidRequest = new AddressRequest();
        // Missing required fields

        // When/Then
        mockMvc.perform(put("/api/v1/address/{addressId}", addressId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void deleteAddress_WithValidId_ShouldDeleteAndReturnNoContent() throws Exception {
        // Given - Create an address first
        String createResponse = mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String addressId = objectMapper.readTree(createResponse).get("id").asText();

        // When/Then
        mockMvc.perform(delete("/api/v1/address/{addressId}", addressId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Verify address is deleted
        mockMvc.perform(get("/api/v1/address/{addressId}", addressId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAddress_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/v1/address/{addressId}", "550e8400-e29b-41d4-a716-446655440000")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Address not found"));
    }

    @Test
    void deleteAddress_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/v1/address/{addressId}", "550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setDefaultAddress_WithValidId_ShouldSetDefaultAndReturnNoContent() throws Exception {
        // Given - Create an address first
        String createResponse = mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String addressId = objectMapper.readTree(createResponse).get("id").asText();

        // When/Then
        mockMvc.perform(put("/api/v1/address/{addressId}/default", addressId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Verify address is set as default
        mockMvc.perform(get("/api/v1/address/{addressId}", addressId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void getDefaultAddress_WithExistingDefault_ShouldReturnDefaultAddress() throws Exception {
        // Given - Create an address and set as default
        String createResponse = mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String addressId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(put("/api/v1/address/{addressId}/default", addressId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // When/Then
        mockMvc.perform(get("/api/v1/address/default")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressId))
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void getDefaultAddress_WithNoDefault_ShouldReturnNotFound() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/address/default")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void addressOperations_WithMultipleAddresses_ShouldWorkCorrectly() throws Exception {
        // Given - Create multiple addresses
        AddressRequest homeAddress = new AddressRequest();
        homeAddress.setType("home");
        homeAddress.setStreetAddress("123 Home St");
        homeAddress.setCity("New York");
        homeAddress.setState("NY");
        homeAddress.setPostalCode("10001");
        homeAddress.setCountry("United States");

        AddressRequest workAddress = new AddressRequest();
        workAddress.setType("work");
        workAddress.setStreetAddress("456 Work Ave");
        workAddress.setCity("Boston");
        workAddress.setState("MA");
        workAddress.setPostalCode("02101");
        workAddress.setCountry("United States");

        // Create home address
        String homeResponse = mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(homeAddress)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Create work address
        String workResponse = mockMvc.perform(post("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workAddress)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Verify user has two addresses
        mockMvc.perform(get("/api/v1/address")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        // Set work address as default
        String workAddressId = objectMapper.readTree(workResponse).get("id").asText();
        mockMvc.perform(put("/api/v1/address/{addressId}/default", workAddressId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Verify default address
        mockMvc.perform(get("/api/v1/address/default")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("work"));
    }
}
