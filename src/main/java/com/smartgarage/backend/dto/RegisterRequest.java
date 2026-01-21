package com.smartgarage.backend.dto;

public record RegisterRequest(
        String fullName,
        String email,
        String password,
        String role
) {}
