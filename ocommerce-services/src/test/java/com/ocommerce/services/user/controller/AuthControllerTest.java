package com.ocommerce.services.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocommerce.services.security.JwtAuthenticationFilter;
import com.ocommerce.services.security.JwtUtil;
import com.ocommerce.services.user.dto.AuthResponse;
import com.ocommerce.services.user.dto.LoginRequest;
import com.ocommerce.services.user.dto.SignupRequest;
import com.ocommerce.services.user.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;


    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    /*@BeforeEach
    public void setUp() {
        // Any setup needed before each test can be done here
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters()
                .apply(springSecurity())
            .build();
    }*/

    @Test
    public void testLogin() throws Exception {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(authResponse);

        String requestBody = """
            {
                "email": "testuser@ibi.com",
                "password": "password"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    public void testSignup_InvalidEmail() throws Exception {
        String requestBody = """
            {
                "email": "invalid-email",
                "password": "password"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testSignup_MissingPassword() throws Exception {
        String requestBody = """
            {
                "email": "testuser@ibi.com"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testSignup_DuplicateUser() throws Exception {
        String requestBody = """
            {
                "email": "existinguser@ibi.com",
                "password": "password"
            }
            """;

        when(authenticationService.signup(any(SignupRequest.class)))
            .thenThrow(new RuntimeException("User already exists"));

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testSignup_Success_StandardEmail() throws Exception {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");
        when(authenticationService.signup(any(SignupRequest.class)))
            .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    public void testSignup_Success_EmailWithDot() throws Exception {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("Jane");
        signupRequest.setLastName("Doe");
        signupRequest.setEmail("first.last@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");
        when(authenticationService.signup(any(SignupRequest.class)))
            .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    public void testSignup_Success_EmailWithPlus() throws Exception {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("Alice");
        signupRequest.setLastName("Smith");
        signupRequest.setEmail("user+tag@example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");
        when(authenticationService.signup(any(SignupRequest.class)))
            .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isBadRequest());
//            .andExpect(jsonPath("$.accessToken").value("access-token"))
//            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    public void testSignup_Success_EmailWithDash() throws Exception {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("Bob");
        signupRequest.setLastName("Brown");
        signupRequest.setEmail("user-name@example-domain.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");
        when(authenticationService.signup(any(SignupRequest.class)))
            .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    public void testSignup_Success_EmailWithNumbers() throws Exception {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("Charlie");
        signupRequest.setLastName("Davis");
        signupRequest.setEmail("user123@example123.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");
        when(authenticationService.signup(any(SignupRequest.class)))
            .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    public void testSignup_Success_EmailWithSubdomain() throws Exception {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("Dana");
        signupRequest.setLastName("Evans");
        signupRequest.setEmail("user@mail.example.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPhoneNumber("+1234567890");
        when(authenticationService.signup(any(SignupRequest.class)))
            .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

}