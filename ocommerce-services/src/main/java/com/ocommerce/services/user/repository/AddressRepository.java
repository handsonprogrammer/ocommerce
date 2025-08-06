package com.ocommerce.services.user.repository;

import com.ocommerce.services.user.domain.Address;
import com.ocommerce.services.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Address entity with soft delete support
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    /**
     * Find all active addresses for a user (not soft deleted)
     * 
     * @param user the user
     * @return list of active addresses
     */
    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.isDeleted = false ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<Address> findActiveAddressesByUser(@Param("user") User user);

    /**
     * Find active address by ID and user (not soft deleted)
     * 
     * @param id   address ID
     * @param user the user
     * @return Optional containing the address if found and active
     */
    @Query("SELECT a FROM Address a WHERE a.id = :id AND a.user = :user AND a.isDeleted = false")
    Optional<Address> findActiveAddressByIdAndUser(@Param("id") UUID id, @Param("user") User user);

    /**
     * Find user's default address (not soft deleted)
     * 
     * @param user the user
     * @return Optional containing the default address if found
     */
    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.isDefault = true AND a.isDeleted = false")
    Optional<Address> findDefaultAddressByUser(@Param("user") User user);

    /**
     * Find addresses by type for a user (not soft deleted)
     * 
     * @param user the user
     * @param type address type
     * @return list of addresses with the specified type
     */
    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.type = :type AND a.isDeleted = false ORDER BY a.createdAt DESC")
    List<Address> findActiveAddressesByUserAndType(@Param("user") User user, @Param("type") String type);

    /**
     * Clear default flag for all user's addresses
     * 
     * @param user the user
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user AND a.isDeleted = false")
    void clearDefaultFlagForUser(@Param("user") User user);

    /**
     * Soft delete address by setting isDeleted flag
     * 
     * @param id   address ID
     * @param user the user (for security)
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDeleted = true WHERE a.id = :id AND a.user = :user")
    void softDeleteByIdAndUser(@Param("id") UUID id, @Param("user") User user);

    /**
     * Check if user has any active addresses
     * 
     * @param user the user
     * @return true if user has active addresses
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Address a WHERE a.user = :user AND a.isDeleted = false")
    boolean hasActiveAddresses(@Param("user") User user);
}
