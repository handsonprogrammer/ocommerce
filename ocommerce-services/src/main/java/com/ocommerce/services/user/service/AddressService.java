package com.ocommerce.services.user.service;

import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.AddressRequest;
import com.ocommerce.services.user.dto.AddressResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for address management operations
 */
public interface AddressService {

    /**
     * Create a new address for the user
     *
     * @param user           the user
     * @param addressRequest address details
     * @return created address response
     */
    AddressResponse createAddress(User user, AddressRequest addressRequest);

    /**
     * Update an existing address by soft deleting the old one and creating a new one
     *
     * @param user           the user
     * @param addressId      address ID to update
     * @param addressRequest updated address details
     * @return updated address response
     * @throws IllegalArgumentException if address not found or doesn't belong to user
     */
    AddressResponse updateAddress(User user, UUID addressId, AddressRequest addressRequest);

    /**
     * Soft delete an address
     *
     * @param user      the user
     * @param addressId address ID to delete
     * @throws IllegalArgumentException if address not found or doesn't belong to user
     */
    void deleteAddress(User user, UUID addressId);

    /**
     * Get all active addresses for a user
     *
     * @param user the user
     * @return list of active addresses
     */
    List<AddressResponse> getUserAddresses(User user);

    /**
     * Get a specific address by ID for a user
     *
     * @param user      the user
     * @param addressId address ID
     * @return address response
     * @throws IllegalArgumentException if address not found or doesn't belong to user
     */
    AddressResponse getAddressById(User user, UUID addressId);

    /**
     * Get user's default address
     *
     * @param user the user
     * @return default address response or null if no default address
     */
    AddressResponse getDefaultAddress(User user);

    /**
     * Set an address as the default address for the user
     *
     * @param user      the user
     * @param addressId address ID to set as default
     * @throws IllegalArgumentException if address not found or doesn't belong to user
     */
    void setDefaultAddress(User user, UUID addressId);
}

