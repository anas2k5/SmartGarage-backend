package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.*;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.InvoicePdfService;
import com.smartgarage.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final InvoicePdfService invoicePdfService;

    // ================= STRIPE FLOW =================

    @PostMapping("/initiate/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDTO> initiatePayment(
            @PathVariable Long bookingId,
            @RequestBody PaymentInitiateRequestDTO request) {
        return ResponseEntity.ok(paymentService.initiatePayment(bookingId, request));
    }

    @PutMapping("/confirm/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDTO> confirmPayment(
            @PathVariable Long bookingId,
            @RequestBody PaymentConfirmRequestDTO request) {
        return ResponseEntity.ok(paymentService.confirmPayment(bookingId, request));
    }

    @GetMapping("/status/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','OWNER','ADMIN')")
    public ResponseEntity<PaymentResponseDTO> getPaymentStatus(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentByBooking(bookingId));
    }

    // ================= PAYMENT HISTORY =================

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getMyPayments(Principal principal) {

        Optional<User> maybeUser =
                userRepository.findByEmail(principal.getName());

        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        User me = maybeUser.get();
        return ResponseEntity.ok(
                paymentService.getPaymentsByCustomer(me.getId())
        );
    }

    // ================= DOWNLOAD INVOICE =================

    @GetMapping("/invoice/{bookingId}/download")
    @PreAuthorize("hasAnyRole('CUSTOMER','OWNER','ADMIN')")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long bookingId) {

        byte[] pdf =
                invoicePdfService.generateInvoicePdf(bookingId);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=invoice-" + bookingId + ".pdf")
                .body(pdf);
    }
}
