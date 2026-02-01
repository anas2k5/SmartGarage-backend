package com.smartgarage.backend.service.impl;

import com.smartgarage.backend.model.AuditModule;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import com.smartgarage.backend.service.AdminUserService;
import com.smartgarage.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void disableUser(Long targetUserId, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (admin.getId().equals(target.getId())) {
            throw new IllegalStateException("You cannot disable your own account");
        }

        if ("ADMIN".equalsIgnoreCase(target.getRole())) {
            throw new IllegalStateException("You cannot disable another admin");
        }

        if (!target.isActive()) {
            throw new IllegalStateException("User is already disabled");
        }

        target.setActive(false);
        userRepository.save(target);

        // 🔥 AUDIT
        auditService.log(
                AuditModule.USER_MANAGEMENT,
                admin.getId(),
                adminEmail,
                "ADMIN",
                "USER_DISABLED",
                "USER",
                target.getId(),
                "ACTIVE",
                "DISABLED"
        );
    }

    @Override
    public void enableUser(Long targetUserId, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (target.isActive()) {
            throw new IllegalStateException("User is already active");
        }

        target.setActive(true);
        userRepository.save(target);

        // 🔥 AUDIT
        auditService.log(
                AuditModule.USER_MANAGEMENT,
                admin.getId(),
                adminEmail,
                "ADMIN",
                "USER_ENABLED",
                "USER",
                target.getId(),
                "DISABLED",
                "ACTIVE"
        );
    }
}
