package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.RefundResponseDTO;

public interface RefundService {
    RefundResponseDTO processRefund(Long bookingId, String reason);
}
