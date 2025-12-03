package com.smartgarage.backend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT helper for generating and validating tokens.
 * Expects jwt.secret to be a Base64-encoded 32-byte key (recommended).
 */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final Key key;
    private final long jwtExpirationMs;

    public JwtUtils(@Value("${jwt.secret}") String jwtSecret,
                    @Value("${jwt.expirationMs:3600000}") long jwtExpirationMs) {

        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret property is not set");
        }

        String trimmed = jwtSecret.trim();
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(trimmed);
            log.info("Loaded jwt.secret as Base64 (decoded length = {})", keyBytes.length);
        } catch (Exception ex) {
            log.warn("jwt.secret is not valid Base64; falling back to UTF-8 bytes (not recommended).");
            keyBytes = trimmed.getBytes(StandardCharsets.UTF_8);
            log.info("jwt.secret UTF-8 length = {}", keyBytes.length);
        }

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("jwt.secret decoded length = " + keyBytes.length +
                    " bytes. Must be at least 32 bytes (256 bits). Generate with: openssl rand -base64 32");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public Key getSigningKey() { return key; }

    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
        }
        return false;
    }
}
