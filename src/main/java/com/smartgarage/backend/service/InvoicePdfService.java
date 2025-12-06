package com.smartgarage.backend.service;

public interface InvoicePdfService {

    byte[] generateInvoicePdf(Long bookingId);
}
