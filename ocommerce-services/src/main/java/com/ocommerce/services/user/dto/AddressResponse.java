package com.ocommerce.services.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Address response DTO
 */
@Schema(description = "Address information")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    @Schema(description = "Address ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "Address type", example = "home")
    @JsonProperty("type")
    private String type;

    @Schema(description = "Street address", example = "123 Main Street")
    @JsonProperty("streetAddress")
    private String streetAddress;

    @Schema(description = "Address line 2", example = "Apt 4B")
    @JsonProperty("addressLine2")
    private String addressLine2;

    @Schema(description = "City", example = "New York")
    @JsonProperty("city")
    private String city;

    @Schema(description = "State/Province", example = "NY")
    @JsonProperty("state")
    private String state;

    @Schema(description = "Postal code", example = "10001")
    @JsonProperty("postalCode")
    private String postalCode;

    @Schema(description = "Country", example = "United States")
    @JsonProperty("country")
    private String country;

    @Schema(description = "Whether this is the default address", example = "true")
    @JsonProperty("isDefault")
    private boolean isDefault;

    @Schema(description = "Full formatted address")
    @JsonProperty("fullAddress")
    private String fullAddress;

    @Schema(description = "Address creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Address last update timestamp")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
