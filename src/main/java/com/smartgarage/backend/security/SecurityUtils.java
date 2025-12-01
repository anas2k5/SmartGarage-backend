package com.smartgarage.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper to read authenticated username/email from SecurityContext.
 * Assumes Authentication.getName() returns the user's username/email.
 */
public final class SecurityUtils {
    private SecurityUtils() {}

    public static String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getName();
    }
}
