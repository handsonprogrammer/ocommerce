package com.ocommerce.api.controller;

import com.ocommerce.api.exception.AddressNotFoundException;
import com.ocommerce.api.exception.UserNotFoundException;
import com.ocommerce.api.model.AddressDto;
import com.ocommerce.api.model.UserDetails;
import com.ocommerce.api.service.AddressService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to handle the creation, updating & viewing of addresses.
 */
@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * Endpoint to create a new address for the user.
     *
     * @param addressDto The address details to be created.
     * @param user       The user provided by spring security context.
     * @return The created address details.
     */
    @PostMapping("/create")
    public ResponseEntity<AddressDto> createAddress(@RequestBody AddressDto addressDto,
            @AuthenticationPrincipal UserDetails user) {
        AddressDto address;
        try {
            address = addressService.createAddress(addressDto, user);
            return ResponseEntity.ok(address);
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().build();

        }
    }

    /**
     * Endpoint to update an existing address for the user.
     *
     * @param addressDto The address details to be updated.
     * @param user       The user provided by spring security context.
     * @return The updated address details.
     */
    @PutMapping("/update")
    public ResponseEntity<AddressDto> updateAddress(@RequestBody AddressDto addressDto,
            @AuthenticationPrincipal UserDetails user) throws AddressNotFoundException {
        AddressDto address;
        try {
            address = addressService.updateAddress(addressDto, user);
            return ResponseEntity.ok(address);
        } catch (AddressNotFoundException | UserNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint to get the default address for the user.
     *
     * @param user The user provided by spring security context.
     * @return The default address details.
     */
    @GetMapping("/default")
    public ResponseEntity<AddressDto> getDefaultAddress(@AuthenticationPrincipal UserDetails user) {
        try {
            AddressDto address = addressService.getDefaultAddressForUser(user.getUserId());
            return ResponseEntity.ok(address);
        } catch (AddressNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint to get an address by user ID and address ID.
     *
     * @param user      The user provided by spring security context.
     * @param addressId The ID of the address to retrieve.
     * @return The address details if found, or a 404 Not Found response.
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDto> getAddressByUserIdAndAddressId(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long addressId) {
        try {
            AddressDto address = addressService.getAddressByUserIdAndAddressId(user.getUserId(), addressId);
            return ResponseEntity.ok(address);
        } catch (AddressNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint to get all active addresses for the user.
     *
     * @param user The user provided by spring security context.
     * @return A list of active addresses for the user.
     */
    @GetMapping("/all")
    public ResponseEntity<List<AddressDto>> getAllActiveAddressesByUserId(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(addressService.getAllActiveAddressesByUserId(user.getUserId()));
    }

    /**
     * Endpoint to delete an address by address ID.
     *
     * @param user      The user provided by spring security context.
     * @param addressId The ID of the address to delete.
     * @return A 204 No Content response if successful, or a 404 Not Found response
     *         if the address does not exist.
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal UserDetails user,
            @PathVariable Long addressId) {
        try {
            addressService.deleteAddress(user.getUserId(), addressId);
            return ResponseEntity.noContent().build();
        } catch (AddressNotFoundException | UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint to set an address as the default address for the user.
     *
     * @param user      The user provided by spring security context.
     * @param addressId The ID of the address to set as default.
     * @return A 204 No Content response if successful, or a 404 Not Found response
     *         if the address does not exist.
     */
    @PostMapping("/set-default/{addressId}")
    public ResponseEntity<Void> setDefaultAddress(@AuthenticationPrincipal UserDetails user,
            @PathVariable Long addressId) {
        try {
            addressService.setDefaultAddress(user.getUserId(), addressId);
            return ResponseEntity.noContent().build();
        } catch (AddressNotFoundException | UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}