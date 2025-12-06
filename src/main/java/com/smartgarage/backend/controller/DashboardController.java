package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.CustomerDashboardDTO;
import com.smartgarage.backend.dto.OwnerDashboardDTO;
import com.smartgarage.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // GET /api/dashboard/customer/{customerId}
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomerDashboardDTO> getCustomerDashboard(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(dashboardService.getCustomerDashboard(customerId));
    }

    // GET /api/dashboard/owner/{ownerId}
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<OwnerDashboardDTO> getOwnerDashboard(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(dashboardService.getOwnerDashboard(ownerId));
    }
}
