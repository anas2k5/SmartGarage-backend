package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.model.Notification;
import com.smartgarage.backend.repository.NotificationRepository;
import com.smartgarage.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository repo;

    @Override
    public void create(
            Long userId,
            String title,
            String message,
            String type
    ) {
        repo.save(
                Notification.builder()
                        .userId(userId)
                        .title(title)
                        .message(message)
                        .type(type)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Override
    public List<Notification> getMyNotifications(
            Long userId
    ) {
        return repo
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public void markAsRead(Long id) {
        Notification n =
                repo.findById(id)
                        .orElseThrow();

        n.setReadStatus(true);
        repo.save(n);
    }
}
