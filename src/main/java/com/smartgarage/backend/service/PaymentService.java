package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.PaymentInitiateRequestDTO;
import com.smartgarage.backend.dto.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {

    // ================= INITIATE STRIPE PAYMENT =================
    PaymentResponseDTO initiatePayment(
            Long bookingId,
            PaymentInitiateRequestDTO request
    );

    // ================= PAYMENT STATUS (FLUTTER POLLS) =================
    PaymentResponseDTO getPaymentByBooking(
            Long bookingId
    );

    // ================= PAYMENT HISTORY =================
    List<PaymentResponseDTO> getPaymentsByCustomer(
            Long customerId
    );
    PaymentResponseDTO confirmPayment(Long bookingId);

}
