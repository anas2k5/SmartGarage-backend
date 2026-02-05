package com.smartgarage.backend.controller;

import com.smartgarage.backend.config.JwtUtils;
import com.smartgarage.backend.dto.JwtResponse;
import com.smartgarage.backend.dto.LoginRequest;
import com.smartgarage.backend.dto.RegisterRequest;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =============================
    // 🔐 LOGIN (Compatible with OLD JwtUtils)
    // =============================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.email(),
                            req.password()
                    )
            );

            CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();

            // Generate JWT using OLD JwtUtils (username only)
            String token = jwtUtils.generateToken(
                    principal.getUsername(),
                    principal.getRole()
            );

            JwtResponse res = new JwtResponse(
                    token,
                    "Bearer",
                    principal.getUsername(),
                    principal.getId(),
                    principal.getRole()
            );

            return ResponseEntity.ok(res);

        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(401)
                    .body("Invalid email or password");
        }
    }

    // =============================
    // 📝 REGISTER
    // =============================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {

        // Check duplicate email
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            java.util.Map.of(
                                    "error", "Email already registered"
                            )
                    );
        }

        // Create user
        User user = new User();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(req.role());

        User savedUser = userRepository.save(user);

        // ✅ RETURN JSON
        return ResponseEntity.ok(
                java.util.Map.of(
                        "message", "User registered successfully",
                        "userId", savedUser.getId()
                )
        );
    }

}
