package com.ocommerce.services.user.repository;

import com.ocommerce.services.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity using Spring Data JPA
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address (case insensitive)
     * 
     * @param email the email address
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Check if user exists by email address (case insensitive)
     * 
     * @param email the email address
     * @return true if user exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    /**
     * Find enabled users by email
     * 
     * @param email the email address
     * @return Optional containing the user if found and enabled
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.accountEnabled = true")
    Optional<User> findByEmailIgnoreCaseAndAccountEnabled(@Param("email") String email);

    /**
     * Find user by email with addresses (fetch join to avoid N+1 problem)
     * 
     * @param email the email address
     * @return Optional containing the user with addresses if found
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.addresses a WHERE LOWER(u.email) = LOWER(:email) AND (a.isDeleted = false OR a IS NULL)")
    Optional<User> findByEmailWithActiveAddresses(@Param("email") String email);

    /**
     * Count active users (enabled and not locked)
     * 
     * @return count of active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountEnabled = true AND u.accountLocked = false")
    long countActiveUsers();
}
