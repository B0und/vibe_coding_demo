package com.vibecodingdemo.backend.exception;

/**
 * Exception thrown when external services (Kafka, Telegram, etc.) are unavailable or fail
 */
public class ExternalServiceException extends RuntimeException {
    
    private final String serviceName;
    
    public ExternalServiceException(String serviceName, String message) {
        super(String.format("External service '%s' error: %s", serviceName, message));
        this.serviceName = serviceName;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("External service '%s' error: %s", serviceName, message), cause);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
} 