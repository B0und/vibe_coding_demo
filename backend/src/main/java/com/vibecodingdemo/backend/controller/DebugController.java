package com.vibecodingdemo.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Value("${telegram.bot.token:NOT_SET}")
    private String botToken;

    @Value("${telegram.bot.username:NOT_SET}")
    private String botUsername;

    @GetMapping("/telegram-config")
    public Map<String, String> getTelegramConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("botToken", botToken.length() > 10 ? botToken.substring(0, 10) + "..." : botToken);
        config.put("botUsername", botUsername);
        config.put("tokenLength", String.valueOf(botToken.length()));
        return config;
    }
} 