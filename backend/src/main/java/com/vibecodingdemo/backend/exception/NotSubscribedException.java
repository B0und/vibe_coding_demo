package com.vibecodingdemo.backend.exception;

public class NotSubscribedException extends RuntimeException {
    
    public NotSubscribedException(String message) {
        super(message);
    }
    
    public NotSubscribedException(String message, Throwable cause) {
        super(message, cause);
    }
} 