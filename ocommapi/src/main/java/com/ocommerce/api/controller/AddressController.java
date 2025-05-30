package com.ocommerce.api.controller;

import com.ocommerce.api.exception.AddressNotFoundException;
import com.ocommerce.api.exception.UserNotFoundException;
import com.ocommerce.api.model.AddressDto;
import com.ocommerce.api.model.UserDetails;
import com.ocommerce.api.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

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
}