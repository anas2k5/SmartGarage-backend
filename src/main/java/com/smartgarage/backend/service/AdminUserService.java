package com.smartgarage.backend.service;

import com.smartgarage.backend.model.User;
import java.util.List;

public interface AdminUserService {
    List<User> getAllUsers();
    void disableUser(Long targetUserId, String adminEmail);
    void enableUser(Long targetUserId, String adminEmail);
}
