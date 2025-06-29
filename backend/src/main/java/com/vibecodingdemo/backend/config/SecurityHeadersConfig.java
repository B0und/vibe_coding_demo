package com.vibecodingdemo.backend.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class SecurityHeadersConfig {

    @Bean
    public Filter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }

    private static class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                // Content Security Policy
                httpResponse.setHeader("Content-Security-Policy",
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: https:; " +
                    "font-src 'self' https: data:; " +
                    "connect-src 'self' https: wss:; " +
                    "media-src 'self'; " +
                    "object-src 'none'; " +
                    "child-src 'none'; " +
                    "frame-src 'none'; " +
                    "worker-src 'none'; " +
                    "frame-ancestors 'none'; " +
                    "form-action 'self'; " +
                    "base-uri 'self'");

                // X-Content-Type-Options
                httpResponse.setHeader("X-Content-Type-Options", "nosniff");

                // X-Frame-Options
                httpResponse.setHeader("X-Frame-Options", "DENY");

                // X-XSS-Protection (legacy but still useful for older browsers)
                httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

                // Referrer Policy
                httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

                // Permissions Policy (Feature Policy)
                httpResponse.setHeader("Permissions-Policy",
                    "camera=(), " +
                    "microphone=(), " +
                    "geolocation=(), " +
                    "interest-cohort=()");

                // Strict-Transport-Security (only in HTTPS environments)
                String protocol = request.getScheme();
                if ("https".equals(protocol)) {
                    httpResponse.setHeader("Strict-Transport-Security", 
                        "max-age=31536000; includeSubDomains; preload");
                }

                // Cache Control for sensitive endpoints
                String requestURI = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
                if (requestURI.contains("/api/users/") || requestURI.contains("/api/admin/")) {
                    httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    httpResponse.setHeader("Pragma", "no-cache");
                    httpResponse.setHeader("Expires", "0");
                }
            }

            chain.doFilter(request, response);
        }
    }
} 