package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Address;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AddressRepository extends ListCrudRepository<Address, Long> {
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.status = 'A'")
    Optional<Address> findFirstByUserId(Long userId);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefaultAddress = true AND a.status = 'A'")
    Optional<Address> findDefaultAddressByUserId(Long userId);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.id = :addressId AND a.status = 'A'")
    Optional<Address> findAddressByUserIdAndAddressId(Long userId, Long addressId);

    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefaultAddress = false WHERE a.user.id = :userId AND a.id <> :addressId")
    void unsetDefaultForOtherAddresses(Long userId, Long addressId);

}