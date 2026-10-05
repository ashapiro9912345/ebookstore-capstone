package com.bookstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * Issues and validates JWT Bearer tokens using jjwt 0.12.x.
 *
 * The secret is read from the environment variable JWT_SECRET (Base64-encoded).
 * Expiration defaults to 24 hours (configurable via JWT_EXPIRATION_MS).
 *
 * No card numbers or payment credentials are handled here.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a signed JWT with the customer's email as the subject.
     *
     * @param email the authenticated customer's email address
     * @return signed JWT string
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the subject (email) from a valid token.
     *
     * @param token the JWT string (without the "Bearer " prefix)
     * @return the email address encoded in the token
     * @throws JwtException if the token is malformed, expired, or has an invalid signature
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Validates the token signature and expiration.
     *
     * @param token the JWT string
     * @return true if the token is valid
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
