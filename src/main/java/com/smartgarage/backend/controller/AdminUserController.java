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

    // 🔍 View all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(
                userRepository.findAll()
        );
    }

    // 🔍 Filter by role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getByRole(
            @PathVariable String role
    ) {
        List<User> filtered =
                userRepository.findAll()
                        .stream()
                        .filter(u -> role.equalsIgnoreCase(u.getRole()))
                        .toList();

        return ResponseEntity.ok(filtered);
    }

    // 🚫 Disable user
    @PutMapping("/{id}/disable")
    public ResponseEntity<String> disableUser(
            @PathVariable Long id
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + id)
                );

        user.setActive(false);
        userRepository.save(user);

        return ResponseEntity.ok("User disabled");
    }

    // ✅ Enable user
    @PutMapping("/{id}/enable")
    public ResponseEntity<String> enableUser(
            @PathVariable Long id
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + id)
                );

        user.setActive(true);
        userRepository.save(user);

        return ResponseEntity.ok("User enabled");
    }
}
