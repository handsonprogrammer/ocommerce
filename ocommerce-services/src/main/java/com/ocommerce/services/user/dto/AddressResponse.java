package com.ocommerce.services.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for address response
 */
@Schema(description = "Address information response")
public class AddressResponse {

    @Schema(description = "Address ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "Address type", example = "home")
    @JsonProperty("type")
    private String type;

    @Schema(description = "Street address", example = "123 Main St")
    @JsonProperty("streetAddress")
    private String streetAddress;

    @Schema(description = "Address line 2", example = "Apt 4B")
    @JsonProperty("addressLine2")
    private String addressLine2;

    @Schema(description = "City", example = "New York")
    @JsonProperty("city")
    private String city;

    @Schema(description = "State", example = "NY")
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

    // Constructors
    public AddressResponse() {
    }

    public AddressResponse(UUID id, String type, String streetAddress, String city,
            String postalCode, String country) {
        this.id = id;
        this.type = type;
        this.streetAddress = streetAddress;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "AddressResponse{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", streetAddress='" + streetAddress + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
