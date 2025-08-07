package com.ocommerce.services.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT utility class for token generation, validation, and claim extraction
 * with configurable secret and expiration times.
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    /**
     * Generate JWT access token from user authentication
     * 
     * @param authentication Spring Security authentication object
     * @return JWT access token
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(userPrincipal.getUsername());
    }

    /**
     * Generate JWT access token from username
     * 
     * @param username the username (email)
     * @return JWT access token
     */
    public String generateAccessToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims, username, accessTokenExpirationMs);
    }

    /**
     * Generate JWT refresh token from username
     * 
     * @param username the username (email)
     * @return JWT refresh token
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, username, refreshTokenExpirationMs);
    }

    /**
     * Create JWT token with claims, subject, and expiration
     * 
     * @param claims       token claims
     * @param subject      token subject (username)
     * @param expirationMs expiration time in milliseconds
     * @return JWT token
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from JWT token
     * 
     * @param token JWT token
     * @return username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from JWT token
     * 
     * @param token JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from JWT token
     * 
     * @param token          JWT token
     * @param claimsResolver function to resolve claims
     * @return extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token
     * 
     * @param token JWT token
     * @return claims
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            logger.error("Failed to extract claims from JWT token", e);
            throw e;
        }
    }

    /**
     * Check if JWT token is expired
     * 
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true; // Consider invalid tokens as expired
        }
    }

    /**
     * Validate JWT token against user details
     * 
     * @param token       JWT token
     * @param userDetails user details
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            logger.error("JWT token validation failed", e);
            return false;
        }
    }

    /**
     * Validate JWT token structure and signature
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("JWT token validation failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Get signing key for JWT tokens
     * 
     * @return signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get access token expiration time in seconds
     * 
     * @return expiration time in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    /**
     * Get refresh token expiration time in seconds
     * 
     * @return expiration time in seconds
     */
    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationMs / 1000;
    }

    /**
     * Get refresh token expiration LocalDateTime
     * 
     * @return expiration LocalDateTime
     */
    public LocalDateTime getRefreshTokenExpiration() {
        return LocalDateTime.now().plusSeconds(getRefreshTokenExpirationSeconds());
    }

    /**
     * Extract token type from JWT token
     * 
     * @param token JWT token
     * @return token type
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> (String) claims.get("type"));
    }

    /**
     * Check if token is access token
     * 
     * @param token JWT token
     * @return true if access token, false otherwise
     */
    public boolean isAccessToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "access".equals(tokenType);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Check if token is refresh token
     * 
     * @param token JWT token
     * @return true if refresh token, false otherwise
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "refresh".equals(tokenType);
        } catch (JwtException e) {
            return false;
        }
    }
}
