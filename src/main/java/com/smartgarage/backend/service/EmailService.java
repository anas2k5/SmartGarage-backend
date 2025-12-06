package com.smartgarage.backend.service;

public interface EmailService {

    void sendSimpleMail(String to, String subject, String text);
}
