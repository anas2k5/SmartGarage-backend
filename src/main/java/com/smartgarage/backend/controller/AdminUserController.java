package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;

    // 🔍 Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // 🔍 Filter by role (DB level, not memory)
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getByRole(
            @PathVariable String role
    ) {
        return ResponseEntity.ok(
                userRepository.findAll()
                        .stream()
                        .filter(u -> role.equalsIgnoreCase(u.getRole()))
                        .toList()
        );
    }

    // 🚫 Disable user
    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {

        return userRepository.findById(id)
                .map(user -> {
                    user.setActive(false);
                    userRepository.save(user);
                    return ResponseEntity.ok("User disabled successfully");
                })
                .orElseGet(() ->
                        ResponseEntity.badRequest().body("User not found")
                );
    }

    // ✅ Enable user
    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {

        return userRepository.findById(id)
                .map(user -> {
                    user.setActive(true);
                    userRepository.save(user);
                    return ResponseEntity.ok("User enabled successfully");
                })
                .orElseGet(() ->
                        ResponseEntity.badRequest().body("User not found")
                );
    }
}
