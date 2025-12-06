package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
        // optional but safe (should match spring.mail.username)
        // message.setFrom("tuternity@gmail.com");
        mailSender.send(message);
    }

    @Override
    public void sendMailWithAttachment(String to,
                                       String subject,
                                       String text,
                                       byte[] attachmentBytes,
                                       String attachmentFilename) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // true -> multipart
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            // explicit plain-text (no HTML)
            helper.setText(text, false);
            // explicit from (matches your spring.mail.username)
            helper.setFrom("tuternity@gmail.com");

            if (attachmentBytes != null && attachmentFilename != null) {
                ByteArrayResource resource = new ByteArrayResource(attachmentBytes);
                helper.addAttachment(attachmentFilename, resource);
            }

            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            System.out.println("Failed to send email with attachment: " + ex.getMessage());
            // fallback: send simple mail without attachment
            try {
                sendSimpleMail(
                        to,
                        subject,
                        text + "\n\n(Note: Invoice PDF could not be attached due to email server/network issue.)"
                );
            } catch (Exception ignored) {
            }
        }
    }
}
