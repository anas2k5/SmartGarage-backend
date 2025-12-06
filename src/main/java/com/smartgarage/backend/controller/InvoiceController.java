package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.InvoiceDTO;
import com.smartgarage.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final PaymentService paymentService;

    // GET /api/invoices/{bookingId}
    @GetMapping("/{bookingId}")
    public ResponseEntity<InvoiceDTO> getInvoiceByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getInvoiceByBooking(bookingId));
    }
}
