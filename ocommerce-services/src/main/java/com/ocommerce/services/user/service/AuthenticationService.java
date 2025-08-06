package com.ocommerce.services.user.service;

import com.ocommerce.services.security.JwtUtil;
import com.ocommerce.services.user.domain.RefreshToken;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.AuthResponse;
import com.ocommerce.services.user.dto.LoginRequest;
import com.ocommerce.services.user.dto.SignupRequest;
import com.ocommerce.services.user.dto.UserResponse;

import org.mapstruct.control.MappingControl.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Service
@Transactional
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

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
        logger.info("Authentication attempt for email: {}", loginRequest.getEmail());

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

            logger.info("User authenticated successfully: {}", user.getEmail());

            return new AuthResponse(
                    accessToken,
                    refreshToken.getToken(),
                    jwtUtil.getAccessTokenExpirationSeconds());

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for email: {}", loginRequest.getEmail());
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
        logger.info("User registration attempt for email: {}", signupRequest.getEmail());

        // Register user
        UserResponse userResponse = userService.registerUser(signupRequest);

        // Get the created user
        User user = userService.findByEmail(signupRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User registration failed"));

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        logger.info("User registered and authenticated successfully: {}", user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtUtil.getAccessTokenExpirationSeconds());
    }

    /**
     * Refresh access token using a valid refresh token with automatic token
     * rotation
     * Validates the refresh token belongs to the specified user, generates new
     * tokens,
     * and revokes the old refresh token for enhanced security
     * 
     * @param email              the email address of the user requesting token
     *                           refresh
     * @param refreshTokenString the refresh token to be used for generating new
     *                           tokens
     * @return new authentication response with fresh access and refresh tokens
     * @throws UserService.UserNotFoundException                if the user with
     *                                                          given email is not
     *                                                          found
     * @throws RefreshTokenService.InvalidRefreshTokenException if refresh token is
     *                                                          invalid, expired, or
     *                                                          doesn't belong to
     *                                                          user
     */
    public AuthResponse refreshTokenForUser(String email, String refreshTokenString) {
        logger.info("Token refresh attempt");

        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new UserService.UserNotFoundException("User not found"));
            // Verify refresh token this will throw exceptio if invalid
            RefreshToken refreshToken = refreshTokenService.findByUserAndToken(user, refreshTokenString)
                    .orElseThrow(() -> new RefreshTokenService.InvalidRefreshTokenException("Refresh token not found"));

            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());

            // Optionally rotate refresh token (create new one and revoke old one)
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
            refreshTokenService.revokeTokenForUser(refreshToken.getToken(), user);

            logger.info("Token refreshed successfully for user: {}", user.getEmail());

            return new AuthResponse(
                    newAccessToken,
                    newRefreshToken.getToken(),
                    jwtUtil.getAccessTokenExpirationSeconds());

        } catch (RefreshTokenService.InvalidRefreshTokenException e) {
            logger.warn("Invalid refresh token used for token refresh");
            throw e;
        }
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
        logger.info("Logout attempt");

        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            // Find the refresh token to log the user out
            RefreshToken refreshToken = refreshTokenService.findByUserAndToken(user, refreshTokenString)
                    .orElseThrow(() -> new RefreshTokenService.InvalidRefreshTokenException("Refresh token not found"));

            refreshTokenService.revokeTokenForUser(refreshToken.getToken(), user);
            logger.info("User logged out successfully: {}", user.getEmail());

        } catch (RefreshTokenService.InvalidRefreshTokenException e) {
            logger.warn("Invalid refresh token used for logout");
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
        logger.info("Logout from all devices for user: {}", userEmail);

        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenService.revokeAllTokensForUser(user);
        logger.info("User logged out from all devices: {}", userEmail);
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
