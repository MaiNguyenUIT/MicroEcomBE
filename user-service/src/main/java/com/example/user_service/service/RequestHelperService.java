package com.example.user_service.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface RequestHelperService {
    public String getCurrentAuthenticatedUserId();
    public String getOrCreateTraceId(String traceIdHeader);
    public String getClientIp(HttpServletRequest request);
}