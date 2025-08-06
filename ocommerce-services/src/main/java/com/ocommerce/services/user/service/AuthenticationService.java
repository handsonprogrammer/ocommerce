package com.ocommerce.services.user.service;

import com.ocommerce.services.security.JwtUtil;
import com.ocommerce.services.user.domain.RefreshToken;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.AuthResponse;
import com.ocommerce.services.user.dto.LoginRequest;
import com.ocommerce.services.user.dto.SignupRequest;
import com.ocommerce.services.user.dto.UserResponse;
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
 * Authentication service for handling login, signup, and token operations
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
     * Authenticate user and generate tokens
     * 
     * @param loginRequest login credentials
     * @return authentication response with tokens
     * @throws BadCredentialsException if credentials are invalid
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
     * Register new user and generate tokens
     * 
     * @param signupRequest registration data
     * @return authentication response with tokens
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
     * Refresh access token using refresh token
     * 
     * @param refreshTokenString refresh token
     * @return new authentication response
     * @throws InvalidRefreshTokenException if refresh token is invalid
     */
    public AuthResponse refreshToken(String refreshTokenString) {
        logger.info("Token refresh attempt");

        try {
            // Verify refresh token
            RefreshToken refreshToken = refreshTokenService.verifyToken(refreshTokenString);
            User user = refreshToken.getUser();

            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());

            // Optionally rotate refresh token (create new one and revoke old one)
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
            refreshTokenService.revokeToken(refreshTokenString);

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
     * Logout user by revoking refresh token
     * 
     * @param refreshTokenString refresh token to revoke
     */
    public void logout(String refreshTokenString) {
        logger.info("Logout attempt");

        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString)
                    .orElseThrow(() -> new RefreshTokenService.InvalidRefreshTokenException("Refresh token not found"));

            refreshTokenService.revokeToken(refreshTokenString);
            logger.info("User logged out successfully: {}", refreshToken.getUser().getEmail());

        } catch (RefreshTokenService.InvalidRefreshTokenException e) {
            logger.warn("Invalid refresh token used for logout");
            // Don't throw exception on logout - fail silently
        }
    }

    /**
     * Logout user from all devices by revoking all refresh tokens
     * 
     * @param userEmail user email
     */
    public void logoutFromAllDevices(String userEmail) {
        logger.info("Logout from all devices for user: {}", userEmail);

        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenService.revokeAllTokensForUser(user);
        logger.info("User logged out from all devices: {}", userEmail);
    }

    /**
     * Validate access token
     * 
     * @param token access token
     * @return true if valid
     */
    @Transactional(readOnly = true)
    public boolean validateAccessToken(String token) {
        return jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token);
    }

    /**
     * Extract user email from access token
     * 
     * @param token access token
     * @return user email
     */
    @Transactional(readOnly = true)
    public String extractUserEmail(String token) {
        if (!validateAccessToken(token)) {
            throw new IllegalArgumentException("Invalid access token");
        }
        return jwtUtil.extractUsername(token);
    }
}
