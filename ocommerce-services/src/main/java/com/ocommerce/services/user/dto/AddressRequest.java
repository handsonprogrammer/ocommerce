package com.ocommerce.services.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for address creation and update requests
 */
@Schema(description = "Address creation/update request")
public class AddressRequest {

    @Schema(description = "Address type", example = "home", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("type")
    @NotBlank(message = "Address type is required")
    @Size(max = 50, message = "Address type cannot exceed 50 characters")
    private String type;

    @Schema(description = "Street address", example = "123 Main St", requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(description = "State", example = "NY")
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

    @Schema(description = "Whether this should be the default address", example = "false")
    @JsonProperty("isDefault")
    private boolean isDefault = false;

    // Constructors
    public AddressRequest() {
    }

    public AddressRequest(String type, String streetAddress, String city,
                         String postalCode, String country) {
        this.type = type;
        this.streetAddress = streetAddress;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return "AddressRequest{" +
                "type='" + type + '\'' +
                ", streetAddress='" + streetAddress + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}

