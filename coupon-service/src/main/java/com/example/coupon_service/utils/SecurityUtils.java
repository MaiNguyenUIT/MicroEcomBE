package com.example.coupon_service.utils; 

import com.example.coupon_service.ENUM.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

public class SecurityUtils {

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated in security context.");
        }
        return authentication.getName();
    }
    public static UserRole getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated in security context.");
        }

        List<String> roles = authentication.getAuthorities().stream()
                                           .map(GrantedAuthority::getAuthority)
                                           .collect(Collectors.toList());

        if (roles.contains("ROLE_ADMIN")) {
            return UserRole.ADMIN;
        } else if (roles.contains("ROLE_SELLER")) {
            return UserRole.SELLER;
        } else {
            throw new IllegalStateException("User role in security context is not recognized for this operation.");
        }
    }
}