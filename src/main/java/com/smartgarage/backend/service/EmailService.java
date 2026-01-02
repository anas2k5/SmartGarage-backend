package com.smartgarage.backend.service;

import com.smartgarage.backend.model.BookingStatus;

public interface EmailService {

    void sendSimpleMail(String to, String subject, String text);

    void sendMailWithAttachment(
            String to,
            String subject,
            String text,
            byte[] attachmentBytes,
            String attachmentFilename
    );

    // ✅ NEW: Booking status email
    void sendBookingStatusMail(
            String to,
            Long bookingId,
            BookingStatus status
    );
}
