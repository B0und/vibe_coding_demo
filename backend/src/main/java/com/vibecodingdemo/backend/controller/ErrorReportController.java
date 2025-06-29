package com.vibecodingdemo.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.vibecodingdemo.backend.config.MetricsConfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/client-errors")
public class ErrorReportController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorReportController.class);
    
    @Autowired
    private MetricsConfig metricsConfig;

    @PostMapping
    public ResponseEntity<Map<String, Object>> reportClientError(
            @Valid @RequestBody ClientErrorReport errorReport,
            HttpServletRequest request,
            Authentication authentication) {
        
        try {
            String userId = authentication != null ? authentication.getName() : "anonymous";
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(request);
            
            // Increment metrics
            metricsConfig.incrementClientErrors();
            if (errorReport.isCritical()) {
                metricsConfig.incrementCriticalClientErrors();
            }
            
            // Log the client error with full context
            logger.error("CLIENT_ERROR reported | User: {} | IP: {} | UserAgent: {} | URL: {} | Message: {} | Stack: {} | Component: {} | Props: {}", 
                        userId,
                        ipAddress,
                        userAgent,
                        errorReport.getUrl(),
                        errorReport.getMessage(),
                        errorReport.getStack(),
                        errorReport.getComponentStack(),
                        errorReport.getProps());

            // Additional structured logging for monitoring/alerting
            if (errorReport.isCritical()) {
                logger.error("CRITICAL_CLIENT_ERROR | User: {} | Message: {} | URL: {}", 
                           userId, errorReport.getMessage(), errorReport.getUrl());
            }

            return ResponseEntity.ok(Map.of(
                "status", "received",
                "timestamp", LocalDateTime.now(),
                "message", "Error report received and logged"
            ));
            
        } catch (Exception e) {
            logger.error("Failed to process client error report", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to process error report"
            ));
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * DTO for client error reports
     */
    public static class ClientErrorReport {
        
        @NotBlank(message = "Error message is required")
        private String message;
        
        private String stack;
        
        @NotBlank(message = "URL is required")
        private String url;
        
        private String componentStack;
        
        private Map<String, Object> props;
        
        @NotNull(message = "Timestamp is required")
        private LocalDateTime timestamp;
        
        private String userAgent;
        
        private boolean critical = false;

        // Getters and setters
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getStack() {
            return stack;
        }

        public void setStack(String stack) {
            this.stack = stack;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getComponentStack() {
            return componentStack;
        }

        public void setComponentStack(String componentStack) {
            this.componentStack = componentStack;
        }

        public Map<String, Object> getProps() {
            return props;
        }

        public void setProps(Map<String, Object> props) {
            this.props = props;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public boolean isCritical() {
            return critical;
        }

        public void setCritical(boolean critical) {
            this.critical = critical;
        }
    }
} 