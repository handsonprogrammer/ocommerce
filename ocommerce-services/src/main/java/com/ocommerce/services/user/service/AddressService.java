package com.ocommerce.services.user.service;

import com.ocommerce.services.user.domain.Address;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.AddressRequest;
import com.ocommerce.services.user.dto.AddressResponse;
import com.ocommerce.services.user.exception.AddressNotFoundException;
import com.ocommerce.services.user.repository.AddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for address management with soft delete support
 */
@Slf4j
@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    /**
     * Create a new address for the user
     *
     * @param user           the user
     * @param addressRequest address details
     * @return created address response
     */
    public AddressResponse createAddress(User user, AddressRequest addressRequest) {
        log.info("Creating new address for user: {}", user.getEmail());

        // If this is set as default, clear other default flags
        if (addressRequest.isDefault()) {
            addressRepository.clearDefaultFlagForUser(user);
        }

        Address address = new Address();
        mapRequestToEntity(addressRequest, address);
        address.setUser(user);

        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully with ID: {}", savedAddress.getId());

        return mapEntityToResponse(savedAddress);
    }

    /**
     * Update an existing address by soft deleting the old one and creating a new one
     *
     * @param user           the user
     * @param addressId      address ID to update
     * @param addressRequest updated address details
     * @return updated address response
     * @throws IllegalArgumentException if address not found or doesn't belong to user
     */
    public AddressResponse updateAddress(User user, UUID addressId, AddressRequest addressRequest) {
        log.info("Updating address {} for user: {}", addressId, user.getEmail());

        // Find existing address and verify ownership
        Address existingAddress = findActiveAddressByIdAndUser(addressId, user);

        // Soft delete the existing address
        existingAddress.markAsDeleted();
        addressRepository.save(existingAddress);
        log.info("Soft deleted existing address: {}", addressId);

        // Create new address with updated data
        Address newAddress = new Address();
        mapRequestToEntity(addressRequest, newAddress);
        newAddress.setUser(user);

        // If this is set as default, clear other default flags
        if (addressRequest.isDefault()) {
            addressRepository.clearDefaultFlagForUser(user);
        }

        Address savedAddress = addressRepository.save(newAddress);
        log.info("Created new address {} to replace {}", savedAddress.getId(), addressId);

        return mapEntityToResponse(savedAddress);
    }

    /**
     * Soft delete an address
     *
     * @param user      the user
     * @param addressId address ID to delete
     * @throws IllegalArgumentException if address not found or doesn't belong to user
     */
    public void deleteAddress(User user, UUID addressId) {
        log.info("Deleting address {} for user: {}", addressId, user.getEmail());

        Address address = findActiveAddressByIdAndUser(addressId, user);
        address.markAsDeleted();
        addressRepository.save(address);

        log.info("Address {} soft deleted successfully", addressId);
    }

    /**
     * Get all active addresses for a user
     *
     * @param user the user
     * @return list of active addresses
     */
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(User user) {
        log.info("Fetching all addresses for user: {}", user.getEmail());

        List<Address> addresses = addressRepository.findActiveAddressesByUser(user);
        return addresses.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific address by ID for a user
     *
     * @param user      the user
     * @param addressId address ID
     * @return address response
     * @throws IllegalArgumentException if address not found or doesn't belong to user
     */
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(User user, UUID addressId) {
        log.info("Fetching address {} for user: {}", addressId, user.getEmail());

        Address address = findActiveAddressByIdAndUser(addressId, user);
        return mapEntityToResponse(address);
    }

    /**
     * Get user's default address
     *
     * @param user the user
     * @return default address response or null if no default address
     */
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress(User user) {
        log.info("Fetching default address for user: {}", user.getEmail());

        return addressRepository.findDefaultAddressByUser(user)
                .map(this::mapEntityToResponse)
                .orElse(null);
    }

    /**
     * Set an address as the default address for the user
     *
     * @param user      the user
     * @param addressId address ID to set as default
     * @throws IllegalArgumentException if address not found or doesn't belong to user
     */
    public void setDefaultAddress(User user, UUID addressId) {
        log.info("Setting address {} as default for user: {}", addressId, user.getEmail());

        Address address = findActiveAddressByIdAndUser(addressId, user);

        // Clear all default flags for user
        addressRepository.clearDefaultFlagForUser(user);

        // Set this address as default
        address.setDefault(true);
        addressRepository.save(address);

        log.info("Address {} set as default successfully", addressId);
    }

    private Address findActiveAddressByIdAndUser(UUID addressId, User user) {
        return addressRepository.findActiveAddressByIdAndUser(addressId, user)
                .orElseThrow(() -> new AddressNotFoundException(
                        "Address not found or does not belong to user: " + addressId));
    }

    private void mapRequestToEntity(AddressRequest request, Address address) {
        address.setType(request.getType());
        address.setStreetAddress(request.getStreetAddress());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setDefault(request.isDefault());
    }

    private AddressResponse mapEntityToResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setType(address.getType());
        response.setStreetAddress(address.getStreetAddress());
        response.setAddressLine2(address.getAddressLine2());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPostalCode(address.getPostalCode());
        response.setCountry(address.getCountry());
        response.setDefault(address.isDefault());
        response.setFullAddress(address.getFullAddress());
        response.setCreatedAt(address.getCreatedAt());
        response.setUpdatedAt(address.getUpdatedAt());
        return response;
    }
}
