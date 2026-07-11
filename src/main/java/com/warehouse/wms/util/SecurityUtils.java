package com.warehouse.wms.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {
    
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                // You may need to extract user ID from UserDetails
                // This depends on your UserDetails implementation
                return 1L; // Placeholder - replace with actual user ID extraction
            }
        }
        return 1L; // Default user ID for development
    }
}