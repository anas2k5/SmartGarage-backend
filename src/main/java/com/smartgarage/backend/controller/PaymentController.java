package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.PaymentInitiateRequestDTO;
import com.smartgarage.backend.dto.PaymentResponseDTO;
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

    // ================= INITIATE STRIPE =================
    @PostMapping("/initiate/{bookingId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<PaymentResponseDTO> initiatePayment(
            @PathVariable Long bookingId,
            @RequestBody PaymentInitiateRequestDTO request
    ) {
        return ResponseEntity.ok(
                paymentService.initiatePayment(
                        bookingId,
                        request
                )
        );
    }

    // ================= STATUS (POLLING) =================
    @GetMapping("/status/{bookingId}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER','OWNER','ADMIN')")
    public ResponseEntity<PaymentResponseDTO> getPaymentStatus(
            @PathVariable Long bookingId
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentByBooking(
                        bookingId
                )
        );
    }

    // ================= HISTORY =================
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> getMyPayments(
            Principal principal
    ) {

        Optional<User> maybeUser =
                userRepository.findByEmail(
                        principal.getName()
                );

        if (maybeUser.isEmpty()) {
            return ResponseEntity
                    .status(401)
                    .body("Unauthenticated");
        }

        return ResponseEntity.ok(
                paymentService
                        .getPaymentsByCustomer(
                                maybeUser.get().getId()
                        )
        );
    }

    // ================= INVOICE =================
    @GetMapping("/invoice/{bookingId}/download")
    @PreAuthorize("hasAnyAuthority('CUSTOMER','OWNER','ADMIN')")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long bookingId
    ) {

        byte[] pdf =
                invoicePdfService
                        .generateInvoicePdf(
                                bookingId
                        );

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=invoice-" +
                                bookingId +
                                ".pdf"
                )
                .body(pdf);
    }
}
