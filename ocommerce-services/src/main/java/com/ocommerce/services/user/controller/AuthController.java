package com.ocommerce.services.user.controller;

import com.ocommerce.services.user.dto.AuthResponse;
import com.ocommerce.services.user.dto.LoginRequest;
import com.ocommerce.services.user.dto.SignupRequest;
import com.ocommerce.services.user.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication operations")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticate user with email and password, returns access and refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login request for email: {}", loginRequest.getEmail());

        AuthResponse authResponse = authenticationService.login(loginRequest);

        return ResponseEntity.ok(authResponse);
    }

    /**
     * User registration endpoint
     */
    @PostMapping("/signup")
    @Operation(summary = "Register new user", description = "Register new user account and return access and refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or user already exists")
    })
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        logger.info("Signup request for email: {}", signupRequest.getEmail());

        AuthResponse authResponse = authenticationService.signup(signupRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Refresh access token", description = "Generate new access token using valid refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<AuthResponse> refreshToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Token refresh request");

        AuthResponse authResponse = authenticationService.refreshTokenForUser(userDetails.getUsername(), refreshToken);

        return ResponseEntity.ok(authResponse);
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Logout user", description = "Revoke refresh token to logout user from current device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged out successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            authenticationService.logout(userDetails.getUsername(), refreshToken);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required"));
        }

        logger.info("Logout request processed");

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Logout from all devices endpoint
     */
    @PostMapping("/logout-all")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Logout from all devices", description = "Revoke all refresh tokens for user to logout from all devices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged out from all devices successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Map<String, String>> logoutFromAllDevices(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        authenticationService.logoutFromAllDevices(email);

        logger.info("Logout from all devices request processed for email: {}", email);

        return ResponseEntity.ok(Map.of("message", "Logged out from all devices successfully"));
    }

    /**
     * Health check endpoint for auth service
     */
    @GetMapping("/health")
    @Operation(summary = "Authentication service health check", description = "Check if authentication service is running")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Authentication Service",
                "timestamp", String.valueOf(System.currentTimeMillis())));
    }
}
