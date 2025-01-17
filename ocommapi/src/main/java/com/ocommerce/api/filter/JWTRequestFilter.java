package com.ocommerce.api.filter;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.ocommerce.api.jpa.entities.UserReg;
import com.ocommerce.api.jpa.repositories.UserRegRepository;
import com.ocommerce.api.model.UserDetails;
import com.ocommerce.api.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Filter for decoding a JWT in the Authorization header and loading the user
 * object into the authentication context.
 */
@Component
public class JWTRequestFilter extends OncePerRequestFilter {

    /** The JWT Service. */
    private JWTService jwtService;
    /** The Local User DAO. */
    private UserRegRepository userRegRepository;

    /**
     * Constructor for spring injection.
     * @param jwtService
     * @param userRegRepository
     */
    public JWTRequestFilter(JWTService jwtService, UserRegRepository userRegRepository) {
        this.jwtService = jwtService;
        this.userRegRepository = userRegRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenHeader = request.getHeader("Authorization");
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            try {
                String username = jwtService.getUsername(token);
                Optional<UserReg> opUser = userRegRepository.findByUsernameIgnoreCase(username);
                if (opUser.isPresent()) {
                    UserReg userReg = opUser.get();
                    UserDetails userDetails = new UserDetails();
                    userDetails.setUserId(userReg.getId());
                    userDetails.setEmail(userReg.getEmail());
                    userDetails.setFirstName(userReg.getFirstName());
                    userDetails.setLastName(userReg.getLastName());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, new ArrayList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JWTDecodeException ex) {
            }
        }
        filterChain.doFilter(request, response);
    }

}
