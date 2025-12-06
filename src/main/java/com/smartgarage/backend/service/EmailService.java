package com.smartgarage.backend.service;

public interface EmailService {

    // existing simple text email
    void sendSimpleMail(String to, String subject, String text);

    // NEW – email with a single attachment (e.g. PDF invoice)
    void sendMailWithAttachment(String to,
                                String subject,
                                String text,
                                byte[] attachmentBytes,
                                String attachmentFilename);
}
