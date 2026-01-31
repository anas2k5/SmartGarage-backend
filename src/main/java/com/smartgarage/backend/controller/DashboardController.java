package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.AdminDashboardDTO;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    // ================= HELPER =================
    private Optional<User> getAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(principal.getName());
    }

    // ================= CUSTOMER DASHBOARD =================
    @GetMapping("/customer/me")
    public ResponseEntity<?> getCustomerDashboard(Principal principal) {

        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) {
            return ResponseEntity
                    .status(401)
                    .body("Unauthenticated");
        }

        return ResponseEntity.ok(
                dashboardService.getCustomerDashboard(
                        maybeUser.get().getId()
                )
        );
    }

    // ================= OWNER DASHBOARD =================
    @GetMapping("/owner/me")
    public ResponseEntity<?> getOwnerDashboard(Principal principal) {

        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) {
            return ResponseEntity
                    .status(401)
                    .body("Unauthenticated");
        }

        return ResponseEntity.ok(
                dashboardService.getOwnerDashboard(
                        maybeUser.get().getId()
                )
        );
    }

    // ================= ADMIN DASHBOARD =================
    @GetMapping("/admin/me")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        return ResponseEntity.ok(
                dashboardService.getAdminDashboard()
        );
    }
}
