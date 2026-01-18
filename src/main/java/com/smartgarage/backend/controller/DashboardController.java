package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.CustomerDashboardDTO;
import com.smartgarage.backend.dto.OwnerDashboardDTO;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    // --------------------
    // Helper
    // --------------------
    private Optional<User> getAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(principal.getName());
    }

    // ✅ CUSTOMER DASHBOARD (SECURE)
    @GetMapping("/customer/me")
    public ResponseEntity<?> getCustomerDashboard(Principal principal) {

        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        User customer = maybeUser.get();
        return ResponseEntity.ok(
                dashboardService.getCustomerDashboard(customer.getId())
        );
    }

    // ✅ OWNER DASHBOARD (SECURE)
    @GetMapping("/owner/me")
    public ResponseEntity<?> getOwnerDashboard(Principal principal) {

        Optional<User> maybeUser = getAuthenticatedUser(principal);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        User owner = maybeUser.get();
        return ResponseEntity.ok(
                dashboardService.getOwnerDashboard(owner.getId())
        );
    }
}
