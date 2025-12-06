package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.InvoiceDTO;
import com.smartgarage.backend.service.InvoicePdfService;
import com.smartgarage.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final PaymentService paymentService;
    private final InvoicePdfService invoicePdfService;

    // GET /api/invoices/{bookingId} -> JSON
    @GetMapping("/{bookingId}")
    public ResponseEntity<InvoiceDTO> getInvoiceByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getInvoiceByBooking(bookingId));
    }

    // GET /api/invoices/{bookingId}/pdf -> PDF download
    @GetMapping("/{bookingId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long bookingId) {
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(bookingId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("invoice-" + bookingId + ".pdf")
                        .build()
        );

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
