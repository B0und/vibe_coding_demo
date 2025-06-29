package com.vibecodingdemo.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Standardized error response structure
     */
    private Map<String, Object> createErrorResponse(String message, HttpStatus status, String errorCode, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", status.value());
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("requestId", requestId);
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return errorResponse;
    }

    /**
     * Log error with contextual information
     */
    private void logError(String errorType, Exception ex, WebRequest request, String requestId) {
        logger.error("Error Type: {} | Request ID: {} | Path: {} | Message: {} | Exception: {}", 
                    errorType, 
                    requestId, 
                    request.getDescription(false),
                    ex.getMessage(),
                    ex.getClass().getSimpleName(),
                    ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getMessage(), HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", request);
        
        logError("RESOURCE_NOT_FOUND", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEventNotFoundException(
            EventNotFoundException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getMessage(), HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", request);
        
        logError("EVENT_NOT_FOUND", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(AlreadySubscribedException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadySubscribedException(
            AlreadySubscribedException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getMessage(), HttpStatus.CONFLICT, "ALREADY_SUBSCRIBED", request);
        
        logError("ALREADY_SUBSCRIBED", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(NotSubscribedException.class)
    public ResponseEntity<Map<String, Object>> handleNotSubscribedException(
            NotSubscribedException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getMessage(), HttpStatus.BAD_REQUEST, "NOT_SUBSCRIBED", request);
        
        logError("NOT_SUBSCRIBED", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            "Validation failed", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", request);
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errorResponse.put("fieldErrors", fieldErrors);
        
        logError("VALIDATION_ERROR", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getMessage(), HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", request);
        
        logError("INVALID_ARGUMENT", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            "Authentication failed", HttpStatus.UNAUTHORIZED, "AUTHENTICATION_ERROR", request);
        
        logError("AUTHENTICATION_ERROR", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            "Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", request);
        
        logError("INVALID_CREDENTIALS", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            "Access denied", HttpStatus.FORBIDDEN, "ACCESS_DENIED", request);
        
        logError("ACCESS_DENIED", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            "Endpoint not found", HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND", request);
        
        logError("ENDPOINT_NOT_FOUND", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Map<String, Object>> handleExternalServiceException(
            ExternalServiceException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            "External service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_SERVICE_ERROR", request);
        
        logError("EXTERNAL_SERVICE_ERROR", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleCustomValidationException(
            ValidationException ex, WebRequest request) {
        Map<String, Object> errorResponse = createErrorResponse(
            ex.getMessage(), HttpStatus.BAD_REQUEST, "BUSINESS_VALIDATION_ERROR", request);
        
        logError("BUSINESS_VALIDATION_ERROR", ex, request, (String) errorResponse.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        // Sanitize error message for production
        String message = "prod".equals(activeProfile) ? 
            "An unexpected error occurred. Please contact support." : 
            ex.getMessage();
            
        Map<String, Object> errorResponse = createErrorResponse(
            message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", request);
        
        // Always log the full exception details regardless of environment
        logError("INTERNAL_SERVER_ERROR", ex, request, (String) errorResponse.get("requestId"));
        
        // Only include stack trace in non-production environments
        if (!"prod".equals(activeProfile)) {
            errorResponse.put("details", ex.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 