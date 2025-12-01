package com.smartgarage.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "custpass";   // change if needed
        String hashed = encoder.encode(rawPassword);
        System.out.println("BCrypt Hash: " + hashed);
    }
}
