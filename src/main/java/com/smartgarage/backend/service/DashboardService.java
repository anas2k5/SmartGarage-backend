package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.CustomerDashboardDTO;
import com.smartgarage.backend.dto.OwnerDashboardDTO;

public interface DashboardService {

    CustomerDashboardDTO getCustomerDashboard(Long customerId);

    OwnerDashboardDTO getOwnerDashboard(Long ownerId);
}
