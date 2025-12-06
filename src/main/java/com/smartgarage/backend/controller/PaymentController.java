package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.*;
import com.smartgarage.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate/{bookingId}")
    public ResponseEntity<PaymentResponseDTO> initiatePayment(
            @PathVariable Long bookingId,
            @RequestBody PaymentInitiateRequestDTO request) {
        return ResponseEntity.ok(paymentService.initiatePayment(bookingId, request));
    }

    @PutMapping("/confirm/{bookingId}")
    public ResponseEntity<PaymentResponseDTO> confirmPayment(
            @PathVariable Long bookingId,
            @RequestBody PaymentConfirmRequestDTO request) {
        return ResponseEntity.ok(paymentService.confirmPayment(bookingId, request));
    }

    @GetMapping("/status/{bookingId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentStatus(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentByBooking(bookingId));
    }
}
