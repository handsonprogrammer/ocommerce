package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Address;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends ListCrudRepository<Address, Long> {

    /**
     * Finds the first address for a given user that is active.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the first active address for the user, or
     *         empty if none found.
     */
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.status = com.ocommerce.api.constants.AddressStatus.ACTIVE")
    Optional<Address> findFirstByUserId(Long userId);

    /**
     * Finds the default address for a given user that is active.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the default active address for the user, or
     *         empty if none found.
     */
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefaultAddress = true AND a.status = com.ocommerce.api.constants.AddressStatus.ACTIVE")
    Optional<Address> findDefaultAddressByUserId(Long userId);

    /**
     * Finds an address by user ID and address ID that is active.
     *
     * @param userId    The ID of the user.
     * @param addressId The ID of the address.
     * @return An Optional containing the address if found, or empty if not found.
     */
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.id = :addressId AND a.status = com.ocommerce.api.constants.AddressStatus.ACTIVE")
    Optional<Address> findAddressByUserIdAndAddressId(Long userId, Long addressId);

    /**
     * Finds all active addresses for a given user.
     *
     * @param userId The ID of the user.
     * @return A list of active addresses for the user.
     */
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.status = com.ocommerce.api.constants.AddressStatus.ACTIVE")
    List<Address> findAllActiveByUserId(Long userId);

    /**
     * Unsets the default address for all other addresses of a user except the
     * specified address.
     *
     * @param userId    The ID of the user.
     * @param addressId The ID of the address to keep as default.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefaultAddress = false WHERE a.user.id = :userId AND a.id <> :addressId")
    void unsetDefaultForOtherAddresses(Long userId, Long addressId);

    /**
     * Marks an address as terminated by its addressId and userId.
     *
     * @param userId    The ID of the user.
     * @param addressId The ID of the address to delete.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.status = com.ocommerce.api.constants.AddressStatus.TERMINATED WHERE a.user.id = :userId AND a.id = :addressId")
    void deleteAddressByUserIdAndAddressId(Long userId, Long addressId);

}