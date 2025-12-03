package com.smartgarage.backend.controller;

import com.smartgarage.backend.config.JwtUtils;
import com.smartgarage.backend.dto.JwtResponse;
import com.smartgarage.backend.dto.LoginRequest;
import com.smartgarage.backend.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authManager, JwtUtils jwtUtils) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        String token = jwtUtils.generateToken(principal.getUsername());
        JwtResponse res = new JwtResponse(token, "Bearer", principal.getUsername(), principal.getId(), principal.getRole());
        return ResponseEntity.ok(res);
    }
}
