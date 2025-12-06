package com.smartgarage.backend.controller;

import com.smartgarage.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-email")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    // GET /api/test-email
    @GetMapping
    public ResponseEntity<String> sendTestEmail() {
        String to = "tuternity@gmail.com";   // your email
        String subject = "SmartGarage Test Email";
        String text = "Hi, this is a test email from SmartGarage backend.";

        emailService.sendSimpleMail(to, subject, text);

        return ResponseEntity.ok("Test email triggered");
    }
}
