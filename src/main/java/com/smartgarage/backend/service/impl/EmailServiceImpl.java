package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.model.BookingStatus;
import com.smartgarage.backend.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Override
    public void sendMailWithAttachment(
            String to,
            String subject,
            String text,
            byte[] attachmentBytes,
            String attachmentFilename
    ) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            helper.setFrom("tuternity@gmail.com");

            if (attachmentBytes != null && attachmentFilename != null) {
                helper.addAttachment(
                        attachmentFilename,
                        new ByteArrayResource(attachmentBytes)
                );
            }

            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            sendSimpleMail(
                    to,
                    subject,
                    text + "\n\n(Invoice attachment failed to send)"
            );
        }
    }

    // ==========================================================
    // ✅ BOOKING STATUS EMAIL (ASYNC)
    // ==========================================================
    @Async
    @Override
    public void sendBookingStatusMail(
            String to,
            Long bookingId,
            BookingStatus status
    ) {
        String subject = "Booking Update - #" + bookingId;
        String message;

        switch (status) {
            case PENDING -> message =
                    "Hi,\n\nYour booking #" + bookingId +
                            " has been created and is pending approval.\n\n" +
                            "Smart Garage Team";

            case ACCEPTED -> message =
                    "Good news!\n\nYour booking #" + bookingId +
                            " has been accepted by the garage.\n\n" +
                            "Smart Garage Team";

            case IN_PROGRESS -> message =
                    "Update:\n\nWork has started on your booking #" + bookingId +
                            ".\n\nSmart Garage Team";

            case COMPLETED -> message =
                    "Completed 🎉\n\nYour booking #" + bookingId +
                            " has been completed successfully.\n\n" +
                            "Thank you for choosing Smart Garage.";

            case CANCELLED -> message =
                    "Cancelled ❌\n\nYour booking #" + bookingId +
                            " has been cancelled.\n\n" +
                            "If you have questions, contact support.";

            default -> message =
                    "Your booking #" + bookingId +
                            " status changed to " + status;
        }

        sendSimpleMail(to, subject, message);
    }
}
