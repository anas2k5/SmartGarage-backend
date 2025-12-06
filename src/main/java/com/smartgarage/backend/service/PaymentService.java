package com.smartgarage.backend.service;

import com.smartgarage.backend.dto.InvoiceDTO;
import com.smartgarage.backend.dto.PaymentConfirmRequestDTO;
import com.smartgarage.backend.dto.PaymentInitiateRequestDTO;
import com.smartgarage.backend.dto.PaymentResponseDTO;

public interface PaymentService {

    PaymentResponseDTO initiatePayment(Long bookingId, PaymentInitiateRequestDTO request);

    PaymentResponseDTO confirmPayment(Long bookingId, PaymentConfirmRequestDTO request);

    PaymentResponseDTO getPaymentByBooking(Long bookingId);

    InvoiceDTO getInvoiceByBooking(Long bookingId);
}
