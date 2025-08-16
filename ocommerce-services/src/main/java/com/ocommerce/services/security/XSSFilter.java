package com.ocommerce.services.security;

import com.ocommerce.services.security.wrapper.XSSRequestWrapper;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XSSFilter implements Filter {

    @Value("${app.security.xss.enabled:false}")
    private String enabled;

    // Implement filter methods to sanitize incoming requests
    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
            jakarta.servlet.FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException {

        // If XSS protection is disabled, proceed without wrapping
        if (!Boolean.parseBoolean(enabled)) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap the request to sanitize inputs
        XSSRequestWrapper wrappedRequest = new XSSRequestWrapper((HttpServletRequest) request);
        // Continue the filter chain with the wrapped request
        chain.doFilter(wrappedRequest, response);
    }
}
