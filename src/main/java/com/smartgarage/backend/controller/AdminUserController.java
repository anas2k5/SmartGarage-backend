package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final AuditService auditService;

    // ================= GET ALL USERS =================
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ================= DISABLE USER =================
    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setActive(false);
                    userRepository.save(user);

                    // 🔥 AUDIT LOG
                    auditService.log(
                            user.getId(),
                            userDetails.getUsername(),
                            "ADMIN",
                            "USER_DISABLED",
                            "USER",
                            user.getId(),
                            "ACTIVE",
                            "DISABLED"
                    );

                    return ResponseEntity.ok("User disabled successfully");
                })
                .orElseGet(() ->
                        ResponseEntity.badRequest().body("User not found")
                );
    }

    // ================= ENABLE USER =================
    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setActive(true);
                    userRepository.save(user);

                    // 🔥 AUDIT LOG
                    auditService.log(
                            user.getId(),
                            userDetails.getUsername(),
                            "ADMIN",
                            "USER_ENABLED",
                            "USER",
                            user.getId(),
                            "DISABLED",
                            "ACTIVE"
                    );

                    return ResponseEntity.ok("User enabled successfully");
                })
                .orElseGet(() ->
                        ResponseEntity.badRequest().body("User not found")
                );
    }
}
