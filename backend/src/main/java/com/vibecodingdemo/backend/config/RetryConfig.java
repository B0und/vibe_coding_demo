package com.vibecodingdemo.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryConfig {
    // Spring Retry configuration
    // The @EnableRetry annotation enables retry functionality across the application
} 