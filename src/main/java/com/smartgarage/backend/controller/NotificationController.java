package com.smartgarage.backend.controller;

import com.smartgarage.backend.model.Notification;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    private final UserRepository userRepository;

    private Optional<User> getUser(Principal p) {
        if (p == null) return Optional.empty();
        return userRepository.findByEmail(p.getName());
    }

    // ===== GET MY NOTIFICATIONS =====
    @GetMapping("/me")
    public ResponseEntity<?> myNotifications(
            Principal principal
    ) {
        Optional<User> u = getUser(principal);

        if (u.isEmpty())
            return ResponseEntity.status(401).body("Unauthenticated");

        List<Notification> list =
                service.getMyNotifications(u.get().getId());

        return ResponseEntity.ok(list);
    }

    // ===== MARK AS READ =====
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(
            @PathVariable Long id
    ) {
        service.markAsRead(id);
        return ResponseEntity.ok("Read");
    }
}
