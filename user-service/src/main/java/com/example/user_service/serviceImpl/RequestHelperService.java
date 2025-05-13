package com.example.user_service.serviceImpl;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RequestHelperService implements com.example.user_service.service.RequestHelperService {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestHelperService.class);

    @Override
    public String getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
            logger.warn("Could not determine authenticated userId from principal of type: {}", principal.getClass().getName());
            return "unknown_authenticated_user_type";
        }
        logger.warn("No authenticated user found or user is anonymous for current operation.");
        return "unauthenticated_or_anonymous";
    }

    @Override
    public String getOrCreateTraceId(String traceIdHeader) {
        return (traceIdHeader != null && !traceIdHeader.isEmpty()) ?
               traceIdHeader : UUID.randomUUID().toString();
    }

    @Override
    public String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddr == null || "".equals(remoteAddr) || "unknown".equalsIgnoreCase(remoteAddr)) {
            remoteAddr = request.getHeader("Proxy-Client-IP");
        }
        if (remoteAddr == null || "".equals(remoteAddr) || "unknown".equalsIgnoreCase(remoteAddr)) {
            remoteAddr = request.getHeader("WL-Proxy-Client-IP");
        }
        if (remoteAddr == null || "".equals(remoteAddr) || "unknown".equalsIgnoreCase(remoteAddr)) {
            remoteAddr = request.getHeader("HTTP_CLIENT_IP");
        }
        if (remoteAddr == null || "".equals(remoteAddr) || "unknown".equalsIgnoreCase(remoteAddr)) {
            remoteAddr = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (remoteAddr == null || "".equals(remoteAddr) || "unknown".equalsIgnoreCase(remoteAddr)) {
            remoteAddr = request.getRemoteAddr();
        }
        return (remoteAddr != null && remoteAddr.contains(",")) ?
               remoteAddr.split(",")[0].trim() : remoteAddr;
    }
}
