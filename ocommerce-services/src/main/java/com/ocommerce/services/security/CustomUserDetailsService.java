package com.ocommerce.services.security;

import com.ocommerce.services.user.domain.User;
import com.ocommerce.services.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserDetailsService implementation for Spring Security
 * Loads user data from the database for authentication
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load user by username (email in our case)
     * 
     * @param email the email address used as username
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if user not found or not enabled
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmailIgnoreCaseAndAccountEnabled(email)
                .orElseThrow(() -> {
                    log.warn("User not found or not enabled with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.debug("User loaded successfully: {}", email);
        return user; // User entity implements UserDetails
    }

    /**
     * Load user by email (for internal service use)
     * 
     * @param email the email address
     * @return User entity
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User loadUserEntityByEmail(String email) throws UsernameNotFoundException {
        log.debug("Loading user entity by email: {}", email);

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("User entity not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }
}
