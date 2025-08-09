package com.ocommerce.services.user.controller;

import com.ocommerce.services.config.WithCustomUser;
import com.ocommerce.services.security.JwtUtil;
import com.ocommerce.services.user.dto.UserResponse;
import com.ocommerce.services.user.dto.UserUpdateRequest;
import com.ocommerce.services.user.exception.UserNotFoundException;
import com.ocommerce.services.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.ocommerce.services.user.UserConstants.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID,
            firstName = FIRST_NAME,
            lastName = LAST_NAME,
            phoneNumber = PHONE_NUMBER,
            accountEnabled = ACCOUNT_ENABLED,
            emailVerified = EMAIL_VERIFIED)
    public void testGetCurrentUser() throws Exception {
        UserResponse userResponse = new UserResponse();
        userResponse.setEmail("testuser@ibi.com");
        userResponse.setFirstName("Test");
        userResponse.setLastName("User");

        Mockito.when(userService.getUserProfile(anyString())).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/me"))
                .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(EMAIL))
            .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(LAST_NAME));
    }

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testUpdateCurrentUser() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("User");

        UserResponse updatedResponse = new UserResponse();
        updatedResponse.setEmail(EMAIL);
        updatedResponse.setFirstName("Updated");
        updatedResponse.setLastName("User");
        updatedResponse.setPhoneNumber(PHONE_NUMBER);
        updatedResponse.setAccountEnabled(ACCOUNT_ENABLED);
        updatedResponse.setEmailVerified(EMAIL_VERIFIED);


        Mockito.when(userService.updateUserProfile(anyString(), Mockito.any())).thenReturn(updatedResponse);

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/users/me")
                        .contentType("application/json")
                        .content("{\"firstName\":\"Updated\",\"lastName\":\"User\"}")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.phoneNumber").value(PHONE_NUMBER))
                .andExpect(jsonPath("$.accountEnabled").value(ACCOUNT_ENABLED))
                .andExpect(jsonPath("$.emailVerified").value(EMAIL_VERIFIED));


    }

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testGetUserStats() throws Exception {
        Mockito.when(userService.getActiveUsersCount()).thenReturn(42L);

        mockMvc.perform(get("/api/v1/users/stats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeUsersCount").value(42));
    }

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testGetCurrentUser_NotFound() throws Exception {
        Mockito.when(userService.getUserProfile(anyString())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/me"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testUpdateCurrentUser_InvalidRequest() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/users/me")
                                .contentType("application/json")
                                .content("{\"firstName\":\"\",\"lastName\":\"\"}")
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    //@Test
    @WithCustomUser(email = EMAIL, userId = USER_ID)
    public void testGetUserStats_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/stats")
                        .header("Authorization", "InvalidToken"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}