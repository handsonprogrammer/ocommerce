package com.ocommerce.api.configuration;

import com.ocommerce.api.filter.JWTRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private JWTRequestFilter jwtRequestFilter;

    public SecurityConfig(JWTRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf((csrf) -> csrf
                .ignoringRequestMatchers("/api/**"));
        http.addFilterBefore(jwtRequestFilter, AuthenticationFilter.class);
        http.authorizeHttpRequests((authorize) ->
                        authorize.requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                                .anyRequest().authenticated());
        return http.build();
    }

}
