package com.smartgarage.backend.dto;

/**
 * JWT response returned after successful login.
 * Fields:
 *  - token: the JWT token string
 *  - type: typically "Bearer"
 *  - email: user's email (username)
 *  - userId: numeric DB id
 *  - role: user role string (e.g., CUSTOMER, OWNER)
 */
public record JwtResponse(String token, String type, String email, Long userId, String role) {}
