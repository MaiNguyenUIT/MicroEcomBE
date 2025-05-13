package com.example.user_service.controller;

import com.example.user_service.dto.UserInforDTO;
import com.example.user_service.model.User;
import com.example.user_service.service.UserService;
import com.example.user_service.service.RequestHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.*;

@RestController
@RequestMapping("api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RequestHelperService requestHelperService;

    private void logRequest(String authenticatedUserId, String traceId, String clientIp, String endpoint, String httpMethod, String action, Object... additionalKVs) {
        MDC.put("userId", authenticatedUserId);
        MDC.put("traceId", traceId);
        MDC.put("clientIp", clientIp);
        MDC.put("endpoint", endpoint);
        MDC.put("httpMethod", httpMethod);
        MDC.put("action", action);
        for (int i = 0; i < additionalKVs.length; i += 2) {
            if (i + 1 < additionalKVs.length) {
                MDC.put(additionalKVs[i].toString(), additionalKVs[i + 1].toString());
            }
        }
        logger.info("Request initiated.", kv("log_event_type", "API_REQUEST"));
    }

    private void logResponse(HttpStatus status, String message, Object... additionalKVs) {
        for (int i = 0; i < additionalKVs.length; i += 2) {
            if (i + 1 < additionalKVs.length) {
                MDC.put(additionalKVs[i].toString(), additionalKVs[i + 1].toString());
            }
        }
        logger.info(message, kv("log_event_type", "API_RESPONSE"), kv("http_status_code", status.value()));
    }

    private void logErrorResponse(HttpStatus status, String message, Throwable e, Object... additionalKVs) {
        for (int i = 0; i < additionalKVs.length; i += 2) {
            if (i + 1 < additionalKVs.length) {
                MDC.put(additionalKVs[i].toString(), additionalKVs[i + 1].toString());
            }
        }
        logger.error(message, e, kv("log_event_type", "API_RESPONSE"), kv("http_status_code", status.value()));
    }

    @GetMapping("")
    public ResponseEntity<User> getCurrentUser(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceIdHeader,
            HttpServletRequest request) {

        String authenticatedUserId = requestHelperService.getCurrentAuthenticatedUserId();
        String traceId = requestHelperService.getOrCreateTraceId(traceIdHeader);
        String clientIp = requestHelperService.getClientIp(request);
        String endpoint = "/api/user";
        String httpMethod = "GET";
        String action = "getCurrentUserProfile";

        logRequest(authenticatedUserId, traceId, clientIp, endpoint, httpMethod, action);

        try {
            User user = userService.findUserByJwtToken();
            if (user != null) {
                logResponse(HttpStatus.OK, "Successfully retrieved profile for user: {}", "userId", user.getId());
                return ResponseEntity.ok(user);
            } else {
                logResponse(HttpStatus.NOT_FOUND, "User profile not found for current authenticated user.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            logErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving current user's profile. Error: {}", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @PathVariable String id,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceIdHeader,
            HttpServletRequest request) {

        String authenticatedUserId = requestHelperService.getCurrentAuthenticatedUserId();
        String traceId = requestHelperService.getOrCreateTraceId(traceIdHeader);
        String clientIp = requestHelperService.getClientIp(request);
        String endpoint = "/api/user/" + id;
        String httpMethod = "GET";
        String action = "getUserProfileById";

        logRequest(authenticatedUserId, traceId, clientIp, endpoint, httpMethod, action, "targetUserId", id);

        try {
            User user = userService.findUserByUserId(id);
            if (user != null) {
                logResponse(HttpStatus.OK, "Successfully retrieved profile for user ID: {}", "userId", id);
                return ResponseEntity.ok(user);
            } else {
                logResponse(HttpStatus.NOT_FOUND, "User profile not found for ID: {}", "userId", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            logErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving user profile for ID: {}. Error: {}", e, "userId", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            MDC.clear();
        }
    }

    @PutMapping("/information")
    public ResponseEntity<User> updateUserInformation(
            @RequestBody UserInforDTO userInforDTO,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceIdHeader,
            HttpServletRequest request) {

        String authenticatedUserId = requestHelperService.getCurrentAuthenticatedUserId();
        String traceId = requestHelperService.getOrCreateTraceId(traceIdHeader);
        String clientIp = requestHelperService.getClientIp(request);
        String endpoint = "/api/user/information";
        String httpMethod = "PUT";
        String action = "updateUserInformation";

        logRequest(authenticatedUserId, traceId, clientIp, endpoint, httpMethod, action);

        try {
            User user = userService.findUserByJwtToken();
            if (user == null) {
                logResponse(HttpStatus.UNAUTHORIZED, "Cannot update information. Authenticated user not found for token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            User updatedUser = userService.updateUserInformation(userInforDTO, user);
            logResponse(HttpStatus.OK, "Successfully updated information for user: {}", "userId", authenticatedUserId);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating user information for user: {}. Error: {}", e, "userId", authenticatedUserId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            MDC.clear();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUser(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceIdHeader,
            HttpServletRequest request) {

        String adminUserId = requestHelperService.getCurrentAuthenticatedUserId();
        String traceId = requestHelperService.getOrCreateTraceId(traceIdHeader);
        String clientIp = requestHelperService.getClientIp(request);
        String endpoint = "/api/user/users";
        String httpMethod = "GET";
        String action = "getAllUsersByAdmin";

        logRequest(adminUserId, traceId, clientIp, endpoint, httpMethod, action);

        try {
            List<User> users = userService.getAllUser();
            logResponse(HttpStatus.OK, "Admin ({}) successfully retrieved {} users.", adminUserId, users != null ? users.size() : 0);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Admin ({}) failed to get all users. Error: {}", e, "adminUserId", adminUserId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            MDC.clear();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sellers")
    public ResponseEntity<List<User>> getAllSeller(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceIdHeader,
            HttpServletRequest request) {

        String adminUserId = requestHelperService.getCurrentAuthenticatedUserId();
        String traceId = requestHelperService.getOrCreateTraceId(traceIdHeader);
        String clientIp = requestHelperService.getClientIp(request);
        String endpoint = "/api/user/sellers";
        String httpMethod = "GET";
        String action = "getAllSellersByAdmin";

        logRequest(adminUserId, traceId, clientIp, endpoint, httpMethod, action);

        try {
            List<User> sellers = userService.getAllSeller();
            logResponse(HttpStatus.OK, "Admin ({}) successfully retrieved {} sellers.", adminUserId, sellers != null ? sellers.size() : 0);
            return ResponseEntity.ok(sellers);
        } catch (Exception e) {
            logErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Admin ({}) failed to get all sellers. Error: {}", e, "adminUserId", adminUserId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            MDC.clear();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/block")
    public ResponseEntity<User> blockUser(
            @RequestParam String userId,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceIdHeader,
            HttpServletRequest request) {

        String adminUserId = requestHelperService.getCurrentAuthenticatedUserId();
        String traceId = requestHelperService.getOrCreateTraceId(traceIdHeader);
        String clientIp = requestHelperService.getClientIp(request);
        String endpoint = "/api/user/block";
        String httpMethod = "PUT";
        String action = "blockUserByAdmin";

        logRequest(adminUserId, traceId, clientIp, endpoint, httpMethod, action, "targetUserId", userId);

        try {
            User blockedUser = userService.blockUser(userId);
            logResponse(HttpStatus.OK, "Admin ({}) successfully blocked user with ID: {}", adminUserId, userId);
            return ResponseEntity.ok(blockedUser);
        } catch (Exception e) {
            logErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Admin ({}) failed to block user with ID: {}. Error: {}", e, "adminUserId", adminUserId, "targetUserId", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            MDC.clear();
        }
    }
}
