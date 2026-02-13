package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.model.Notification;
import com.smartgarage.backend.repository.NotificationRepository;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.smartgarage.backend.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {
    private final UserRepository userRepository;

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
    @Override
    public long getUnreadCount(Long userId) {
        return repo.countByUserIdAndReadStatusFalse(userId);
    }

    @Override
    public void markAllAsRead(Long userId) {

        List<Notification> list =
                repo.findByUserIdAndReadStatusFalse(
                        userId);

        for (Notification n : list) {
            n.setReadStatus(true);
        }

        repo.saveAll(list);
    }

    // ================= ADMIN BROADCAST =================
    @Override
    public void notifyAdmins(
            String title,
            String message,
            String type
    ) {
        List<User> admins =
                userRepository.findByRole("ADMIN");

        for (User admin : admins) {
            repo.save(
                    Notification.builder()
                            .userId(admin.getId())
                            .title(title)
                            .message(message)
                            .type(type)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }
    }

}

