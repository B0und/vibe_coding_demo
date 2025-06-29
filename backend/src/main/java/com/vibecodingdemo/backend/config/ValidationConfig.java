package com.vibecodingdemo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.regex.Pattern;

@Configuration
public class ValidationConfig {

    /**
     * Filter to sanitize request parameters and prevent XSS attacks
     */
    @Bean
    public Filter xssProtectionFilter() {
        return new XSSProtectionFilter();
    }

    private static class XSSProtectionFilter implements Filter {

        private static final Pattern[] XSS_PATTERNS = {
            // Script fragments
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Event handlers
            Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
            // HTML tags that can be dangerous
            Pattern.compile("<iframe", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<object", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<embed", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<link", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<meta", Pattern.CASE_INSENSITIVE)
        };

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                XSSRequestWrapper wrappedRequest = new XSSRequestWrapper(httpRequest);
                chain.doFilter(wrappedRequest, response);
            } else {
                chain.doFilter(request, response);
            }
        }

        private static class XSSRequestWrapper extends HttpServletRequestWrapper {

            public XSSRequestWrapper(HttpServletRequest request) {
                super(request);
            }

            @Override
            public String[] getParameterValues(String parameter) {
                String[] values = super.getParameterValues(parameter);
                if (values == null) {
                    return null;
                }
                
                String[] sanitizedValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    sanitizedValues[i] = sanitizeInput(values[i]);
                }
                return sanitizedValues;
            }

            @Override
            public String getParameter(String parameter) {
                String value = super.getParameter(parameter);
                return value != null ? sanitizeInput(value) : null;
            }

            @Override
            public String getHeader(String name) {
                String value = super.getHeader(name);
                return value != null ? sanitizeInput(value) : null;
            }

            private String sanitizeInput(String value) {
                if (value == null) {
                    return null;
                }

                String sanitized = value;
                
                // Remove XSS patterns
                for (Pattern pattern : XSS_PATTERNS) {
                    sanitized = pattern.matcher(sanitized).replaceAll("");
                }

                // HTML encode special characters
                sanitized = sanitized.replace("&", "&amp;")
                                   .replace("<", "&lt;")
                                   .replace(">", "&gt;")
                                   .replace("\"", "&quot;")
                                   .replace("'", "&#x27;")
                                   .replace("/", "&#x2F;");

                return sanitized;
            }
        }
    }
} 