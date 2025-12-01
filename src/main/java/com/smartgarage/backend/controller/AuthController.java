package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.*;
import com.smartgarage.backend.config.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    public AuthController(AuthenticationManager authManager, JwtUtils jwtUtils){ this.authManager = authManager; this.jwtUtils = jwtUtils; }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req){
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        String token = jwtUtils.generateToken(((org.springframework.security.core.userdetails.User)auth.getPrincipal()).getUsername());
        return ResponseEntity.ok(new JwtResponse(token, "Bearer", req.email()));
    }
}
