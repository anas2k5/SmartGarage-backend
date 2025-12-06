package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.CustomerDashboardDTO;
import com.smartgarage.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // GET /api/dashboard/customer/3
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomerDashboardDTO> getCustomerDashboard(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(dashboardService.getCustomerDashboard(customerId));
    }
}
