package com.smartgarage.backend.service.impl;

import com.sendgrid.SendGrid;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.Method;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.smartgarage.backend.model.BookingStatus;
import com.smartgarage.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${SENDGRID_API_KEY}")
    private String sendGridApiKey;

    private final String FROM_EMAIL = "smartgarage05@gmail.com";

    // ================= SIMPLE MAIL =================
    @Override
    public void sendSimpleMail(String to, String subject, String text) {
        sendEmail(to, subject, text, null, null);
    }

    // ================= MAIL WITH ATTACHMENT =================
    @Override
    public void sendMailWithAttachment(
            String to,
            String subject,
            String text,
            byte[] attachmentBytes,
            String attachmentFilename
    ) {
        sendEmail(to, subject, text, attachmentBytes, attachmentFilename);
    }

    // ================= CORE SEND METHOD =================
    private void sendEmail(
            String to,
            String subject,
            String text,
            byte[] attachmentBytes,
            String attachmentFilename
    ) {

        Email from = new Email(FROM_EMAIL);
        Email recipient = new Email(to);
        Content content = new Content("text/plain", text);
        Mail mail = new Mail(from, subject, recipient, content);

        // Attachment
        if (attachmentBytes != null && attachmentFilename != null) {
            Attachments attachment = new Attachments();
            attachment.setContent(Base64.getEncoder().encodeToString(attachmentBytes));
            attachment.setType("application/pdf");
            attachment.setFilename(attachmentFilename);
            attachment.setDisposition("attachment");
            mail.addAttachments(attachment);
        }

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("SENDGRID STATUS: " + response.getStatusCode());

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("SendGrid email failed");
        }
    }

    // ================= BOOKING STATUS EMAIL =================
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
                            " has been created and is pending approval.\n\nSmart Garage Team";

            case ACCEPTED -> message =
                    "Good news!\n\nYour booking #" + bookingId +
                            " has been accepted by the garage.\n\nSmart Garage Team";

            case IN_PROGRESS -> message =
                    "Update:\n\nWork has started on your booking #" + bookingId +
                            ".\n\nSmart Garage Team";

            case COMPLETED -> message =
                    "Completed 🎉\n\nYour booking #" + bookingId +
                            " has been completed successfully.\n\nThank you for choosing Smart Garage.";

            case CANCELLED -> message =
                    "Cancelled ❌\n\nYour booking #" + bookingId +
                            " has been cancelled.\n\nIf you have questions, contact support.";

            default -> message =
                    "Your booking #" + bookingId +
                            " status changed to " + status;
        }

        sendSimpleMail(to, subject, message);
    }
}