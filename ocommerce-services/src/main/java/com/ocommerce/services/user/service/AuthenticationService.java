package com.ocommerce.services.user.service;

import com.ocommerce.services.security.JwtUtil;
import com.ocommerce.services.user.domain.RefreshToken;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.AuthResponse;
import com.ocommerce.services.user.dto.LoginRequest;
import com.ocommerce.services.user.dto.SignupRequest;
import com.ocommerce.services.user.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service for handling user authentication, registration, and
 * JWT token lifecycle management
 * Provides comprehensive authentication operations including login, signup,
 * token refresh, and logout functionality
 * Implements secure token rotation and multi-device session management
 */
@Slf4j
@Service
@Transactional
public class AuthenticationService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserService userService,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Authenticate user with email and password, then generate access and refresh
     * tokens
     * Normalizes email to lowercase and trims whitespace before authentication
     * 
     * @param loginRequest the login credentials containing email and password
     * @return authentication response containing access token, refresh token, and
     *         expiration time
     * @throws BadCredentialsException if email or password is invalid
     */
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Authentication attempt for email: {}", loginRequest.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail().toLowerCase().trim(),
                            loginRequest.getPassword()));

            User user = (User) authentication.getPrincipal();

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(authentication);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            log.info("User authenticated successfully: {}", user.getEmail());

            return new AuthResponse(
                    accessToken,
                    refreshToken.getToken(),
                    jwtUtil.getAccessTokenExpirationSeconds());

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Register a new user account and automatically generate authentication tokens
     * Creates user account through UserService, then generates tokens for immediate
     * login
     * 
     * @param signupRequest the registration data containing user details
     * @return authentication response with access token, refresh token, and
     *         expiration time
     * @throws RuntimeException if user registration fails or user cannot be found
     *                          after creation
     */
    public AuthResponse signup(SignupRequest signupRequest) {
        log.info("User registration attempt for email: {}", signupRequest.getEmail());

        // Register user
        UserResponse userResponse = userService.registerUser(signupRequest);

        // Get the created user
        User user = userService.findByEmail(signupRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User registration failed"));

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User registered and authenticated successfully: {}", user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtUtil.getAccessTokenExpirationSeconds());
    }

    /**
         * Refresh access token using a valid refresh token.
         * Validates the refresh token, generates a new access token and rotates the refresh token.
         * Revokes the old refresh token for security.
         *
         * @param refreshTokenString the refresh token to be used for generating new tokens
         * @return authentication response with new access token, refresh token, and expiration time
         * @throws RefreshTokenService.InvalidRefreshTokenException if the refresh token is invalid or expired
         */
    public AuthResponse refreshToken(String refreshTokenString) {
        log.info("Token refresh attempt");

        // Validate the refresh token
        RefreshToken refreshToken = refreshTokenService.findValidToken(refreshTokenString)
                .orElseThrow(() -> new RefreshTokenService.InvalidRefreshTokenException("Invalid or expired refresh token"));

        // Get the user associated with the refresh token
        User user = refreshToken.getUser();

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());

        // Optionally rotate the refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        refreshTokenService.revokeTokenForUser(refreshToken.getToken(), user);

        log.info("Access token refreshed successfully for user: {}", user.getEmail());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                jwtUtil.getAccessTokenExpirationSeconds());
    }
    /**
     * Logout user from current session by revoking the specific refresh token
     * Finds and revokes the refresh token for the specified user
     * Fails silently if token is invalid or not found to prevent information
     * disclosure
     * 
     * @param email              the email address of the user to logout
     * @param refreshTokenString the refresh token to revoke for this logout session
     */
    public void logout(String email, String refreshTokenString) {
        log.info("Logout attempt");

        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            // Find the refresh token to log the user out
            RefreshToken refreshToken = refreshTokenService.findByUserAndToken(user, refreshTokenString)
                    .orElseThrow(() -> new RefreshTokenService.InvalidRefreshTokenException("Refresh token not found"));

            refreshTokenService.revokeTokenForUser(refreshToken.getToken(), user);
            log.info("User logged out successfully: {}", user.getEmail());

        } catch (RefreshTokenService.InvalidRefreshTokenException e) {
            log.warn("Invalid refresh token used for logout");
            // Don't throw exception on logout - fail silently
        }
    }

    /**
     * Logout user from all devices by revoking all associated refresh tokens
     * Invalidates all active sessions across all devices for security purposes
     * Useful for password changes, account compromise, or explicit "logout
     * everywhere" requests
     * 
     * @param userEmail the email address of the user to logout from all devices
     * @throws RuntimeException if the user with given email is not found
     */
    public void logoutFromAllDevices(String userEmail) {
        log.info("Logout from all devices for user: {}", userEmail);

        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenService.revokeAllTokensForUser(user);
        log.info("User logged out from all devices: {}", userEmail);
    }

    /**
     * Validate an access token for authenticity and type
     * Performs both general JWT validation and confirms the token is specifically
     * an access token
     * 
     * @param token the access token string to validate
     * @return true if the token is valid and is an access token, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean validateAccessToken(String token) {
        return jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token);
    }

    /**
     * Extract the user email from a valid access token
     * Validates the token before extraction to ensure security
     * 
     * @param token the access token from which to extract the user email
     * @return the email address of the user associated with the token
     * @throws IllegalArgumentException if the access token is invalid or malformed
     */
    @Transactional(readOnly = true)
    public String extractUserEmail(String token) {
        if (!validateAccessToken(token)) {
            throw new IllegalArgumentException("Invalid access token");
        }
        return jwtUtil.extractUsername(token);
    }
}
