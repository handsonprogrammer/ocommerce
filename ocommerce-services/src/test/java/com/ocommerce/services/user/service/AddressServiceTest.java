package com.ocommerce.services.user.service;

import com.ocommerce.services.user.domain.Address;
import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.AddressRequest;
import com.ocommerce.services.user.dto.AddressResponse;
import com.ocommerce.services.user.exception.AddressNotFoundException;
import com.ocommerce.services.user.repository.AddressRepository;
import com.ocommerce.services.user.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Address testAddress;
    private AddressRequest testAddressRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");

        testAddress = new Address();
        testAddress.setId(UUID.randomUUID());
        testAddress.setType("home");
        testAddress.setStreetAddress("123 Main St");
        testAddress.setCity("New York");
        testAddress.setPostalCode("10001");
        testAddress.setCountry("United States");
        testAddress.setUser(testUser);
        testAddress.setCreatedAt(LocalDateTime.now());
        testAddress.setUpdatedAt(LocalDateTime.now());

        testAddressRequest = new AddressRequest();
        testAddressRequest.setType("home");
        testAddressRequest.setStreetAddress("123 Main St");
        testAddressRequest.setCity("New York");
        testAddressRequest.setPostalCode("10001");
        testAddressRequest.setCountry("United States");
    }

    @Test
    void createAddress_ShouldCreateNewAddress() {
        // Given
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // When
        AddressResponse result = addressService.createAddress(testUser, testAddressRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("home");
        assertThat(result.getStreetAddress()).isEqualTo("123 Main St");
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void createAddress_WithDefaultFlag_ShouldClearOtherDefaults() {
        // Given
        testAddressRequest.setDefault(true);
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // When
        addressService.createAddress(testUser, testAddressRequest);

        // Then
        verify(addressRepository).clearDefaultFlagForUser(testUser);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void updateAddress_ShouldSoftDeleteOldAndCreateNew() {
        // Given
        UUID addressId = testAddress.getId();
        when(addressRepository.findActiveAddressByIdAndUser(addressId, testUser))
                .thenReturn(Optional.of(testAddress));

        Address newAddress = new Address();
        newAddress.setId(UUID.randomUUID());
        newAddress.setType("work");
        newAddress.setStreetAddress("456 Work St");
        newAddress.setCity("Boston");
        newAddress.setPostalCode("02101");
        newAddress.setCountry("United States");
        newAddress.setUser(testUser);
        newAddress.setCreatedAt(LocalDateTime.now());
        newAddress.setUpdatedAt(LocalDateTime.now());

        when(addressRepository.save(any(Address.class))).thenReturn(newAddress);

        testAddressRequest.setType("work");
        testAddressRequest.setStreetAddress("456 Work St");
        testAddressRequest.setCity("Boston");
        testAddressRequest.setPostalCode("02101");

        // When
        AddressResponse result = addressService.updateAddress(testUser, addressId, testAddressRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("work");
        assertThat(result.getStreetAddress()).isEqualTo("456 Work St");
        verify(addressRepository, times(2)).save(any(Address.class)); // Once for soft delete, once for new
        assertThat(testAddress.isDeleted()).isTrue();
    }

    @Test
    void updateAddress_WithNonExistentAddress_ShouldThrowException() {
        // Given
        UUID addressId = UUID.randomUUID();
        when(addressRepository.findActiveAddressByIdAndUser(addressId, testUser))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> addressService.updateAddress(testUser, addressId, testAddressRequest))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessageContaining("Address not found or does not belong to user");
    }

    @Test
    void deleteAddress_ShouldSoftDeleteAddress() {
        // Given
        UUID addressId = testAddress.getId();
        when(addressRepository.findActiveAddressByIdAndUser(addressId, testUser))
                .thenReturn(Optional.of(testAddress));

        // When
        addressService.deleteAddress(testUser, addressId);

        // Then
        verify(addressRepository).save(testAddress);
        assertThat(testAddress.isDeleted()).isTrue();
    }

    @Test
    void deleteAddress_WithNonExistentAddress_ShouldThrowException() {
        // Given
        UUID addressId = UUID.randomUUID();
        when(addressRepository.findActiveAddressByIdAndUser(addressId, testUser))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> addressService.deleteAddress(testUser, addressId))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessageContaining("Address not found or does not belong to user");
    }

    @Test
    void getUserAddresses_ShouldReturnAllActiveAddresses() {
        // Given
        Address address2 = new Address();
        address2.setId(UUID.randomUUID());
        address2.setType("work");
        address2.setStreetAddress("456 Work St");
        address2.setCity("Boston");
        address2.setPostalCode("02101");
        address2.setCountry("United States");
        address2.setUser(testUser);
        address2.setCreatedAt(LocalDateTime.now());
        address2.setUpdatedAt(LocalDateTime.now());

        when(addressRepository.findActiveAddressesByUser(testUser))
                .thenReturn(Arrays.asList(testAddress, address2));

        // When
        List<AddressResponse> result = addressService.getUserAddresses(testUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo("home");
        assertThat(result.get(1).getType()).isEqualTo("work");
    }

    @Test
    void getAddressById_ShouldReturnAddress() {
        // Given
        UUID addressId = testAddress.getId();
        when(addressRepository.findActiveAddressByIdAndUser(addressId, testUser))
                .thenReturn(Optional.of(testAddress));

        // When
        AddressResponse result = addressService.getAddressById(testUser, addressId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(addressId);
        assertThat(result.getType()).isEqualTo("home");
    }

    @Test
    void getDefaultAddress_ShouldReturnDefaultAddress() {
        // Given
        testAddress.setDefault(true);
        when(addressRepository.findDefaultAddressByUser(testUser))
                .thenReturn(Optional.of(testAddress));

        // When
        AddressResponse result = addressService.getDefaultAddress(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isDefault()).isTrue();
    }

    @Test
    void getDefaultAddress_WithNoDefault_ShouldReturnNull() {
        // Given
        when(addressRepository.findDefaultAddressByUser(testUser))
                .thenReturn(Optional.empty());

        // When
        AddressResponse result = addressService.getDefaultAddress(testUser);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void setDefaultAddress_ShouldSetAddressAsDefault() {
        // Given
        UUID addressId = testAddress.getId();
        when(addressRepository.findActiveAddressByIdAndUser(addressId, testUser))
                .thenReturn(Optional.of(testAddress));

        // When
        addressService.setDefaultAddress(testUser, addressId);

        // Then
        verify(addressRepository).clearDefaultFlagForUser(testUser);
        verify(addressRepository).save(testAddress);
        assertThat(testAddress.isDefault()).isTrue();
    }
}

