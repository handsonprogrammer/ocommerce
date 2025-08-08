package com.ocommerce.services.user.controller;

import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.AddressRequest;
import com.ocommerce.services.user.dto.AddressResponse;
import com.ocommerce.services.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for address management operations
 */
@RestController
@RequestMapping("/api/v1/addresses")
@Tag(name = "Address Management", description = "APIs for managing user addresses")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);

    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @Operation(summary = "Create a new address", description = "Create a new address for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Address created successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddressRequest addressRequest) {

        logger.info("Creating address for user: {}", user.getEmail());
        AddressResponse response = addressService.createAddress(user, addressRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an address", description = "Update an existing address (soft delete old, create new)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address updated successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Address ID to update") @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest addressRequest) {

        logger.info("Updating address {} for user: {}", addressId, user.getEmail());
        AddressResponse response = addressService.updateAddress(user, addressId, addressRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an address", description = "Soft delete an address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Address deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Address ID to delete") @PathVariable UUID addressId) {

        logger.info("Deleting address {} for user: {}", addressId, user.getEmail());
        addressService.deleteAddress(user, addressId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all user addresses", description = "Retrieve all active addresses for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Addresses retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(@AuthenticationPrincipal User user) {
        logger.info("Fetching addresses for user: {}", user.getEmail());
        List<AddressResponse> addresses = addressService.getUserAddresses(user);
        return ResponseEntity.ok(addresses);
    }

    @Operation(summary = "Get address by ID", description = "Retrieve a specific address by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddressById(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Address ID") @PathVariable UUID addressId) {

        logger.info("Fetching address {} for user: {}", addressId, user.getEmail());
        AddressResponse response = addressService.getAddressById(user, addressId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get default address", description = "Retrieve the default address for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Default address retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "No default address found")
    })
    @GetMapping("/default")
    public ResponseEntity<AddressResponse> getDefaultAddress(@AuthenticationPrincipal User user) {
        logger.info("Fetching default address for user: {}", user.getEmail());
        AddressResponse response = addressService.getDefaultAddress(user);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Set default address", description = "Set an address as the default address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Default address set successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Address ID to set as default") @PathVariable UUID addressId) {

        logger.info("Setting address {} as default for user: {}", addressId, user.getEmail());
        addressService.setDefaultAddress(user, addressId);
        return ResponseEntity.noContent().build();
    }
}

