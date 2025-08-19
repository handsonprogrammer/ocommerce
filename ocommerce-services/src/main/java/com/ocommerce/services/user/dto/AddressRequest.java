package com.ocommerce.services.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address request DTO for creating and updating addresses
 */
@Schema(description = "Address request for create/update operations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @Schema(description = "Address type", example = "home", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("type")
    @NotBlank(message = "Address type is required")
    @Size(max = 50, message = "Address type cannot exceed 50 characters")
    private String type;

    @Schema(description = "Street address", example = "123 Main Street", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("streetAddress")
    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address cannot exceed 255 characters")
    private String streetAddress;

    @Schema(description = "Address line 2", example = "Apt 4B")
    @JsonProperty("addressLine2")
    @Size(max = 255, message = "Address line 2 cannot exceed 255 characters")
    private String addressLine2;

    @Schema(description = "City", example = "New York", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("city")
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Schema(description = "State/Province", example = "NY")
    @JsonProperty("state")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Schema(description = "Postal code", example = "10001", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("postalCode")
    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @Schema(description = "Country", example = "United States", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("country")
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Schema(description = "Whether this is the default address", example = "true")
    @JsonProperty("isDefault")
    private boolean isDefault = false;
}
