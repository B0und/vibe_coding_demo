package com.vibecodingdemo.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.UUID;

@Configuration
public class LoggingConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor());
    }

    /**
     * Interceptor to populate MDC with contextual information for logging
     */
    public static class LoggingInterceptor implements HandlerInterceptor {

        private static final String REQUEST_ID_KEY = "requestId";
        private static final String USER_ID_KEY = "userId";
        private static final String IP_ADDRESS_KEY = "ipAddress";
        private static final String USER_AGENT_KEY = "userAgent";
        private static final String REQUEST_METHOD_KEY = "requestMethod";
        private static final String REQUEST_URI_KEY = "requestUri";

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            try {
                // Generate unique request ID
                String requestId = UUID.randomUUID().toString();
                MDC.put(REQUEST_ID_KEY, requestId);
                
                // Add request ID to response header for client-side correlation
                response.setHeader("X-Request-ID", requestId);

                // Get authenticated user ID if available
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getName())) {
                    MDC.put(USER_ID_KEY, authentication.getName());
                } else {
                    MDC.put(USER_ID_KEY, "anonymous");
                }

                // Add request details
                MDC.put(REQUEST_METHOD_KEY, request.getMethod());
                MDC.put(REQUEST_URI_KEY, request.getRequestURI());
                MDC.put(IP_ADDRESS_KEY, getClientIpAddress(request));
                
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && !userAgent.isEmpty()) {
                    MDC.put(USER_AGENT_KEY, userAgent);
                }

                logger.debug("Request started: {} {} from {}", 
                           request.getMethod(), request.getRequestURI(), getClientIpAddress(request));

            } catch (Exception e) {
                logger.warn("Failed to populate MDC context", e);
            }

            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                  Object handler, Exception ex) {
            try {
                logger.debug("Request completed: {} {} - Status: {}", 
                           request.getMethod(), request.getRequestURI(), response.getStatus());
            } catch (Exception e) {
                logger.warn("Failed to log request completion", e);
            } finally {
                // Clean up MDC to prevent memory leaks
                MDC.clear();
            }
        }

        /**
         * Extract client IP address from request, considering proxy headers
         */
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
    }
} 