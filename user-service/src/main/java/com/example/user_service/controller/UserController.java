package com.example.user_service.controller;

import com.example.user_service.dto.UserInforDTO;
import com.example.user_service.model.User;
import com.example.user_service.service.UserService;
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

import jakarta.servlet.http.HttpServletRequest; // Sử dụng jakarta nếu bạn dùng Spring Boot 3+
// import javax.servlet.http.HttpServletRequest; // Sử dụng javax nếu bạn dùng Spring Boot 2.x
import java.util.List;
import java.util.UUID;

// Import cho Structured Arguments
import static net.logstash.logback.argument.StructuredArguments.*;

@RestController
@RequestMapping("api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // --- Helper Methods for Contextual Information ---
    /**
     * Lấy User ID của người dùng hiện tại đã được xác thực.
     * PHIÊN BẢN NÀY GIẢ SỬ BẠN ĐANG DÙNG SPRING SECURITY.
     * BẠN CẦN ĐIỀU CHỈNH CHO PHÙ HỢP VỚI CÁCH XÁC THỰC CỦA MÌNH.
     */
    private String getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                // Nếu bạn dùng UserDetails mặc định, getUsername() thường là định danh.
                // Nếu userId của bạn là một trường khác trong UserDetails tùy chỉnh, hãy lấy nó.
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                // Đôi khi principal chỉ là một String (ví dụ: subject từ JWT đã được parse)
                return (String) principal;
            }
            // Thêm các trường hợp khác nếu bạn có custom Principal object
            // else if (principal instanceof YourCustomPrincipal) {
            //     return ((YourCustomPrincipal) principal).getUserId();
            // }
            logger.warn("Could not determine authenticated userId from principal of type: {}", principal.getClass().getName());
            return "unknown_authenticated_user_type";
        }
        logger.warn("No authenticated user found in SecurityContext or user is anonymous for current operation.");
        return "unauthenticated_or_anonymous";
    }

    private String getOrCreateTraceId(String incomingTraceId) {
        if (incomingTraceId != null && !incomingTraceId.isEmpty()) {
            return incomingTraceId;
        }
        return UUID.randomUUID().toString();
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
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
        }
        if (remoteAddr != null && remoteAddr.contains(",")) {
            remoteAddr = remoteAddr.split(",")[0].trim();
        }
        return remoteAddr;
    }
    // --- End Helper Methods ---

    @GetMapping("")
    public ResponseEntity<User> getUser(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceIdHeader,
            HttpServletRequest request) {

        String authenticatedUserId = getCurrentAuthenticatedUserId();
        String traceId = getOrCreateTraceId(traceIdHeader);
        String clientIp = getClientIp(request);

        MDC.put("userId", authenticatedUserId);
        MDC.put("traceId", traceId);
        MDC.put("clientIp", clientIp);
        MDC.put("endpoint", "/api/user");
        MDC.put("httpMethod", "GET");
        MDC.put("action", "getCurrentUserProfile");

        logger.info("Attempting to get current user's profile.",
                kv("log_event_type", "API_REQUEST")
        );
        try {
            User user = userService.findUserByJwtToken();
            if (user != null) {
                logger.info("Successfully retrieved profile for user: {}",
                        user.getId(),
                        kv("log_event_type", "API_RESPONSE"),
                        kv("http_status_code", HttpStatus.OK.value())
                );
            } else {
                logger.warn("User profile not found for current authenticated user.",
                        kv("log_event_type", "API_RESPONSE"),
                        kv("http_status_code", HttpStatus.NOT_FOUND.value())
                );
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error retrieving current user's profile. Error: {}",
                    e.getMessage(),
                    e,
                    kv("log_event_type", "API_RESPONSE"),
                    kv("http_status_code", HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
            @PathVariable String id,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceIdHeader,
            HttpServletRequest request) {

        String authenticatedUserId = getCurrentAuthenticatedUserId(); // Admin/user thực hiện request
        String traceId = getOrCreateTraceId(traceIdHeader);
        String clientIp = getClientIp(request);

        MDC.put("userId", authenticatedUserId);
        MDC.put("traceId", traceId);
        MDC.put("clientIp", clientIp);
        MDC.put("endpoint", "/api/user/" + id);
        MDC.put("httpMethod", "GET");
        MDC.put("action", "getUserProfileById");
        MDC.put("targetUserId", id); // ID của người dùng được truy vấn

        logger.info("Attempting to get user profile by ID: {}. Requested by: {}",
                id,
                authenticatedUserId,
                kv("log_event_type", "API_REQUEST")
        );
        try {
            User user = userService.findUserByUserId(id);
            if (user != null) {
                logger.info("Successfully retrieved profile for user ID: {}",
                        id,
                        kv("log_event_type", "API_RESPONSE"),
                        kv("http_status_code", HttpStatus.OK.value())
                );
            } else {
                logger.warn("User profile not found for ID: {}",
                        id,
                        kv("log_event_type", "API_RESPONSE"),
                        kv("http_status_code", HttpStatus.NOT_FOUND.value())
                );
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error retrieving user profile for ID: {}. Error: {}",
                    id,
                    e.getMessage(),
                    e,
                    kv("log_event_type", "API_RESPONSE"),
                    kv("http_status_code", HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            MDC.clear();
        }
    }

    @PutMapping("/information")
    public ResponseEntity<User> updateUserInformation(
            @RequestBody UserInforDTO userInforDTO, // Cẩn thận khi log toàn bộ DTO này nếu có PII
            @RequestHeader(value = "X-Trace-Id", required = false) String traceIdHeader,
            HttpServletRequest request) {

        String authenticatedUserId = getCurrentAuthenticatedUserId();
        String traceId = getOrCreateTraceId(traceIdHeader);
        String clientIp = getClientIp(request);

        MDC.put("userId", authenticatedUserId);
        MDC.put("traceId", traceId);
        MDC.put("clientIp", clientIp);
        MDC.put("endpoint", "/api/user/information");
        MDC.put("httpMethod", "PUT");
        MDC.put("action", "updateUserInformation");

        // Chỉ log một phần thông tin từ DTO nếu cần, hoặc một thông báo chung
        logger.info("Attempting to update user information for user: {}. Request DTO provided.",
                authenticatedUserId,
                // userInforDTO, // Tránh log toàn bộ DTO nếu có PII không cần thiết
                kv("log_event_type", "API_REQUEST")
        );
        try {
            User user = userService.findUserByJwtToken();
            if (user == null) {
                 logger.warn("Cannot update information. Authenticated user not found for token.",
                        kv("log_event_type", "API_RESPONSE"),
                        kv("http_status_code", HttpStatus.UNAUTHORIZED.value())
                );
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            User updatedUser = userService.updateUserInformation(userInforDTO, user);
            logger.info("Successfully updated information for user: {}",
                    authenticatedUserId,
                    kv("log_event_type", "API_RESPONSE"),
                    kv("http_status_code", HttpStatus.OK.value())
            );
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating user information for user: {}. Error: {}",
                    authenticatedUserId,
                    e.getMessage(),
                    e,
                    kv("log_event_type", "API_RESPONSE"),
                    kv("http_status_code", HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
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

        String adminUserId = getCurrentAuthenticatedUserId();
        String traceId = getOrCreateTraceId(traceIdHeader);
        String clientIp = getClientIp(request);

        MDC.put("userId", adminUserId); // Admin thực hiện request
        MDC.put("traceId", traceId);
        MDC.put("clientIp", clientIp);
        MDC.put("endpoint", "/api/user/users");
        MDC.put("httpMethod", "GET");
        MDC.put("action", "getAllUsersByAdmin");

        logger.info("Admin ({}) attempting to get all users.",
                adminUserId,
                kv("log_event_type", "API_REQUEST")
        );
        try {
            List<User> users = userService.getAllUser();
            logger.info("Admin ({}) successfully retrieved {} users.",
                    adminUserId,
                    users != null ? users.size() : 0,
                    kv("log_event_type", "API_RESPONSE"),
                    kv("http_status_code", HttpStatus.OK.value())
            );
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Admin ({}) failed to get all users. Error: {}",
                    adminUserId,
                    e.getMessage(),
                    e,
                    kv("log_event_type", "API_RESPONSE"),
                    kv("http_status_code", HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
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

        String adminUserId = getCurrentAuthenticatedUserId();
        String traceId = getOrCreateTraceId(traceIdHeader);
        String clientIp = getClientIp(request);

        MDC.put("userId", adminUserId);
        MDC.put("traceId", traceId);
        MDC.put("clientIp", clientIp);
        MDC.put("endpoint", "/api/user/sellers");
        MDC.put("httpMethod", "GET");
        MDC.put("action", "getAllSellersByAdmin");

        logger.info("Admin ({}) attempting to get all sellers.",
                adminUserId,
                kv("log_event_type", "API_REQUEST")
        );
        try {
            List<User> sellers = userService.getAllSeller();
            logger.info("Admin ({}) successfully retrieved {} sellers.",
                    adminUserId,
                    sellers != null ? sellers.size() : 0,
                    kv("log_event_type", "API_RESPONSE"),
                    kv("http_status_code", HttpStatus.OK.value())
            );
            return ResponseEntity.ok(sellers);
        } catch (Exception e) {
            logger.error("Admin ({}) failed to get all sellers. Error: {}",
                    adminUserId,
                    e.getMessage(),
                    e,
                    kv("log_event_type", "API_RESPONSE"),
                    kv("http_status_code", HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            MDC.clear();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/block")
    public ResponseEntity<User> blockUser(
            @RequestParam String userId, // ID của người dùng bị block
            @RequestHeader(value = "X-Trace-Id", required = false) String traceIdHeader,
            HttpServletRequest request) {

        String adminUserId = getCurrentAuthenticatedUserId(); // Admin thực hiện request
        String traceId = getOrCreateTraceId(traceIdHeader);
        String clientIp = getClientIp(request);

        MDC.put("userId", adminUserId); // Admin
        MDC.put("traceId", traceId);
        MDC.put("clientIp", clientIp);
        MDC.put("endpoint", "/api/user/block");
        MDC.put("httpMethod", "PUT");
        MDC.put("action", "blockUserByAdmin");
        MDC.put("targetUserId", userId); // Người dùng bị block

        logger.info("Admin ({}) attempting to block user with ID: {}",
                adminUserId,
                userId,
                kv("log_event_type", "API_REQUEST")
        );
        try {
            User blockedUser = userService.blockUser(userId);
            logger.info("Admin ({}) successfully blocked user with ID: {}",
                    adminUserId,
                    userId,
                    kv("log_event_type", "API_RESPONSE"),
                    kv("http_status_code", HttpStatus.OK.value())
            );
            return ResponseEntity.ok(blockedUser);
        } catch (Exception e) {
            logger.error("Admin ({}) failed to block user with ID: {}. Error: {}",
                    adminUserId,
                    userId,
                    e.getMessage(),
                    e,
                    kv("log_event_type", "API_RESPONSE"),
                    kv("http_status_code", HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            MDC.clear();
        }
    }
}
