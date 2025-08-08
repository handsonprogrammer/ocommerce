package com.ocommerce.services.config;

import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomeUserSecurityContextFactory.class,
        setupBefore = TestExecutionEvent.TEST_EXECUTION)
public @interface WithCustomUser {
    String value() default "testuser";

    String password() default "testpassword";

    String[] roles() default {"USER"};

    String username() default "testuser";

    String email() default "testuser@ibi.com";

    String firstName() default "Test";

    String lastName() default "User";

    String phoneNumber() default "1234567890";

    boolean accountEnabled() default true;

    boolean emailVerified() default true;

    String userId() default "123e4567-e89b-12d3-a456-426614174000";

}
