package com.ocommerce.services.user.service;

import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.dto.SignupRequest;
import com.ocommerce.services.user.dto.UserResponse;
import com.ocommerce.services.user.dto.UserUpdateRequest;
import com.ocommerce.services.user.exception.UserAlreadyExistsException;
import com.ocommerce.services.user.exception.UserNotFoundException;
import com.ocommerce.services.user.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service class for User domain operations
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user
     * 
     * @param signupRequest user registration data
     * @return created user response
     * @throws UserAlreadyExistsException if user with email already exists
     */
    public UserResponse registerUser(SignupRequest signupRequest) {
        logger.info("Attempting to register user with email: {}", signupRequest.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmailIgnoreCase(signupRequest.getEmail())) {
            logger.warn("User registration failed - email already exists: {}", signupRequest.getEmail());
            throw new UserAlreadyExistsException("User with email " + signupRequest.getEmail() + " already exists");
        }

        // Create new user
        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setAccountEnabled(true);
        user.setEmailVerified(false); // Email verification can be implemented later

        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());

        return convertToUserResponse(savedUser);
    }

    /**
     * Find user by email
     * 
     * @param email user email
     * @return user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Find enabled user by email
     * 
     * @param email user email
     * @return user if found and enabled
     */
    @Transactional(readOnly = true)
    public Optional<User> findEnabledUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCaseAndAccountEnabled(email);
    }

    /**
     * Find user by ID
     * 
     * @param id user ID
     * @return user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * Get user profile with addresses
     * 
     * @param email user email
     * @return user response with addresses
     */
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmailWithActiveAddresses(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return convertToUserResponseWithAddresses(user);
    }

    /**
     * Update user profile
     * 
     * @param email         current user email
     * @param updateRequest update data
     * @return updated user response
     */
    public UserResponse updateUserProfile(String email, UserUpdateRequest updateRequest) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));


        if (StringUtils.isNotBlank(updateRequest.getFirstName())) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (StringUtils.isNotBlank(updateRequest.getLastName())) {
            user.setLastName(updateRequest.getLastName());
        }
        if (StringUtils.isNotBlank(updateRequest.getPhoneNumber())) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }

        User savedUser = userRepository.save(user);
        logger.info("User profile updated for ID: {}", savedUser.getId());

        return convertToUserResponse(savedUser);
    }

    /**
     * Lock user account
     * 
     * @param email user email
     */
    public void lockUserAccount(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        user.setAccountLocked(true);
        userRepository.save(user);
        logger.info("User account locked for email: {}", email);
    }

    /**
     * Unlock user account
     * 
     * @param email user email
     */
    public void unlockUserAccount(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        user.setAccountLocked(false);
        userRepository.save(user);
        logger.info("User account unlocked for email: {}", email);
    }

    /**
     * Check if user exists by email
     * 
     * @param email user email
     * @return true if user exists
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Get active users count
     * 
     * @return count of active users
     */
    @Transactional(readOnly = true)
    public long getActiveUsersCount() {
        return userRepository.countActiveUsers();
    }

    // Private helper methods

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setEmailVerified(user.isEmailVerified());
        response.setAccountEnabled(user.isAccountEnabled());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private UserResponse convertToUserResponseWithAddresses(User user) {
        UserResponse response = convertToUserResponse(user);
        // Address conversion will be handled by AddressService
        return response;
    }
}
