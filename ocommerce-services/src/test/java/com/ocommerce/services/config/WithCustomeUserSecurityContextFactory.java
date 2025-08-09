package com.ocommerce.services.config;

import com.ocommerce.services.user.domain.User;
import io.jsonwebtoken.lang.Assert;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.UUID;

public final class WithCustomeUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomUser> {


    @Override
    public SecurityContext createSecurityContext(WithCustomUser withUser) {
        User user = new User();
        user.setEmail(withUser.email());
        user.setPassword(withUser.password());
        user.setPhoneNumber(withUser.phoneNumber());
        user.setFirstName(withUser.firstName());
        user.setLastName(withUser.lastName());
        user.setAccountEnabled(withUser.accountEnabled());
        user.setEmailVerified(withUser.emailVerified());
        user.setId(UUID.fromString(withUser.userId()));
        Assert.notNull(user.getId(), "User ID must not be null");
        Assert.notNull(user.getEmail(), "Email must not be null");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        context.setAuthentication(authentication);
        return context;
    }
}
