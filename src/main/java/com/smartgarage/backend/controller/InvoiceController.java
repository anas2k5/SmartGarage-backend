package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.Invoice;
import com.smartgarage.backend.repository.InvoiceRepository;
import com.smartgarage.backend.service.InvoicePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final InvoicePdfService invoicePdfService;

    // ================= GET INVOICE (JSON) =================
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','OWNER','ADMIN')")
    public ResponseEntity<?> getInvoice(
            @PathVariable Long bookingId
    ) {

        Invoice invoice = invoiceRepository
                .findByBookingId(bookingId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invoice not found for booking " + bookingId
                        )
                );

        return ResponseEntity.ok(invoice);
    }

    // ================= DOWNLOAD PDF =================
    @GetMapping("/{bookingId}/pdf")
    @PreAuthorize("hasAnyRole('CUSTOMER','OWNER','ADMIN')")
    public ResponseEntity<byte[]> downloadInvoicePdf(
            @PathVariable Long bookingId
    ) {

        byte[] pdfBytes =
                invoicePdfService.generateInvoicePdf(bookingId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("invoice-" + bookingId + ".pdf")
                        .build()
        );

        return new ResponseEntity<>(
                pdfBytes,
                headers,
                HttpStatus.OK
        );
    }
}
