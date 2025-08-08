package com.ocommerce.services.user.controller;

import com.ocommerce.services.config.WithCustomUser;
import com.ocommerce.services.security.JwtUtil;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.AddressResponse;
import com.ocommerce.services.user.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.ocommerce.services.user.UserConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration(exclude = {
        EnableJpaAuditing.class
})
public class AddressControllerTest {
    // This class will contain tests for AddressController
    // It will use Spring's WebMvcTest to load only the AddressController and its dependencies
    // Additional setup and test methods will be added here as needed
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    @MockBean
    private JwtUtil jwtUtil;

    //test get addresses for user
    public AddressControllerTest() {
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID,
            firstName = FIRST_NAME,
            lastName = LAST_NAME,
            phoneNumber = PHONE_NUMBER,
            accountEnabled = ACCOUNT_ENABLED,
            emailVerified = EMAIL_VERIFIED)
    public void testGetAddressesForUser() throws Exception {

        User testUser = new User();
        testUser.setId(UUID.fromString(USER_ID));
        testUser.setEmail(EMAIL);
        testUser.setPassword(PASSWORD);
        testUser.setFirstName(FIRST_NAME);
        testUser.setLastName(LAST_NAME);
        testUser.setPhoneNumber(PHONE_NUMBER);
        testUser.setAccountEnabled(ACCOUNT_ENABLED);
        testUser.setEmailVerified(EMAIL_VERIFIED);

        // Arrange
        AddressResponse address = new AddressResponse();
        address.setType("home");
        address.setStreetAddress("123 Main St");
        address.setCity("New York");
        address.setPostalCode("10001");
        address.setCountry("United States");
        address.setId(UUID.randomUUID());

        List<AddressResponse> addresses = List.of(
                address
        );
        when(addressService.getUserAddresses(testUser)).thenReturn(addresses);

       // Act & Assert
        mockMvc.perform(get("/api/v1/address")
                        .header("Authorization", "Bearer test-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("home"))
                .andExpect(jsonPath("$[0].streetAddress").value("123 Main St"));
    }

    // src/test/java/com/ocommerce/services/user/controller/AddressControllerTest.java

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testGetAddressById() throws Exception {
        UUID addressId = UUID.randomUUID();
        AddressResponse address = new AddressResponse();
        address.setId(addressId);
        address.setType("work");
        address.setStreetAddress("456 Park Ave");
        address.setCity("New York");
        address.setPostalCode("10022");
        address.setCountry("United States");

        when(addressService.getAddressById(any(User.class), Mockito.eq(addressId))).thenReturn(address);

        mockMvc.perform(get("/api/v1/address/" + addressId)
                .header("Authorization", "Bearer test-jwt-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("work"))
            .andExpect(jsonPath("$.streetAddress").value("456 Park Ave"));
    }

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testCreateAddress() throws Exception {
        AddressResponse address = new AddressResponse();
        address.setId(UUID.randomUUID());
        address.setType("home");
        address.setStreetAddress("789 Broadway");
        address.setCity("New York");
        address.setPostalCode("10003");
        address.setCountry("United States");

        when(addressService.createAddress(any(User.class), any())).thenReturn(address);

        String requestBody = """
            {
                "type": "home",
                "streetAddress": "789 Broadway",
                "city": "New York",
                "postalCode": "10003",
                "country": "United States"
            }
            """;

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/address")
                    .header("Authorization", "Bearer test-jwt-token")
                    .contentType("application/json")
                    .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.type").value("home"))
            .andExpect(jsonPath("$.streetAddress").value("789 Broadway"));
    }

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testUpdateAddress() throws Exception {
        UUID addressId = UUID.randomUUID();
        AddressResponse address = new AddressResponse();
        address.setId(addressId);
        address.setType("office");
        address.setStreetAddress("101 Wall St");
        address.setCity("New York");
        address.setPostalCode("10005");
        address.setCountry("United States");

        when(addressService.updateAddress(any(User.class), Mockito.eq(addressId), any())).thenReturn(address);

        String requestBody = """
            {
                "type": "office",
                "streetAddress": "101 Wall St",
                "city": "New York",
                "postalCode": "10005",
                "country": "United States"
            }
            """;

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/address/" + addressId)
                    .header("Authorization", "Bearer test-jwt-token")
                    .contentType("application/json")
                    .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("office"))
            .andExpect(jsonPath("$.streetAddress").value("101 Wall St"));
    }

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testDeleteAddress() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/address/" + addressId)
                    .header("Authorization", "Bearer test-jwt-token"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testGetDefaultAddress() throws Exception {
        AddressResponse address = new AddressResponse();
        address.setId(UUID.randomUUID());
        address.setType("home");
        address.setStreetAddress("123 Main St");
        address.setCity("New York");
        address.setPostalCode("10001");
        address.setCountry("United States");

        when(addressService.getDefaultAddress(any(User.class))).thenReturn(address);

        mockMvc.perform(get("/api/v1/address/default")
                .header("Authorization", "Bearer test-jwt-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("home"))
            .andExpect(jsonPath("$.streetAddress").value("123 Main St"));
    }

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testSetDefaultAddress() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/address/" + addressId + "/default")
                    .header("Authorization", "Bearer test-jwt-token"))
            .andExpect(status().isNoContent());
    }
    //test get address by id
    //test create address
    //test update address
    //test delete address

}
