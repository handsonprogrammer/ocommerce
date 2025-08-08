package com.ocommerce.services.user.controller;

import com.ocommerce.services.user.dto.UserResponse;
import com.ocommerce.services.user.dto.UserUpdateRequest;
import com.ocommerce.services.user.service.UserService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management endpoints
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User profile operations")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get current authenticated user profile
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieve the profile information of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Get current user profile request for: {}", userDetails.getUsername());

        UserResponse userResponse = userService.getUserProfile(userDetails.getUsername());

        return ResponseEntity.ok(userResponse);
    }

    /**
     * Update current user profile
     */
    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Update the profile information of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest updateRequest) {

        logger.info("Update user profile request for: {}", userDetails.getUsername());

        UserResponse userResponse = userService.updateUserProfile(userDetails.getUsername(), updateRequest);

        return ResponseEntity.ok(userResponse);
    }

    /**
     * Get user statistics (for admin or analytics)
     */
    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Get basic statistics about users (requires authentication)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<UserStats> getUserStats() {
        logger.info("Get user statistics request");

        long activeUsersCount = userService.getActiveUsersCount();

        UserStats stats = new UserStats(activeUsersCount);

        return ResponseEntity.ok(stats);
    }

    /**
     * Inner class for user statistics response
     */
    public static class UserStats {
        private final long activeUsersCount;
        private final long timestamp;

        public UserStats(long activeUsersCount) {
            this.activeUsersCount = activeUsersCount;
            this.timestamp = System.currentTimeMillis();
        }

        public long getActiveUsersCount() {
            return activeUsersCount;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
