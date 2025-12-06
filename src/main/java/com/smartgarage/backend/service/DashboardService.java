package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.CustomerDashboardDTO;

public interface DashboardService {

    CustomerDashboardDTO getCustomerDashboard(Long customerId);
}
