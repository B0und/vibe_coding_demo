package com.vibecodingdemo.backend.controller;

import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.service.UserService;
import com.vibecodingdemo.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request) {
        try {
            // Validate request
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username is required"));
            }

            // Register the user
            User user = userService.registerUser(request.getUsername());

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getUsername());

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "createdAt", user.getCreatedAt()
            ));
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Registration failed"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            // Get the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("createdAt", user.getCreatedAt());
            response.put("updatedAt", user.getUpdatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve user information"));
        }
    }

    @PutMapping("/profile/telegram-recipients")
    public ResponseEntity<?> updateTelegramRecipients(@RequestBody UpdateTelegramRecipientsRequest request) {
        try {
            // Get the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            
            // Validate request
            if (request.getRecipients() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Recipients field is required"));
            }

            // Update Telegram recipients
            userService.updateTelegramRecipients(username, request.getRecipients());

            return ResponseEntity.ok(Map.of("message", "Telegram recipients updated successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update Telegram recipients"));
        }
    }

    @PostMapping("/profile/telegram-activation-code")
    public ResponseEntity<?> generateTelegramActivationCode() {
        try {
            // Get the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            
            // Generate activation code
            String activationCode = userService.generateTelegramActivationCode(username);

            return ResponseEntity.ok(Map.of("activationCode", activationCode));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate activation code"));
        }
    }

    @PostMapping("/telegram-activate")
    public ResponseEntity<?> activateTelegramBot(@RequestBody ActivateTelegramBotRequest request) {
        try {
            // Validate request
            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Activation code is required"));
            }
            
            if (request.getChatId() == null || request.getChatId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Chat ID is required"));
            }

            // Activate Telegram bot
            userService.activateTelegramBot(request.getCode().trim(), request.getChatId().trim());

            return ResponseEntity.ok(Map.of("message", "Telegram bot activated successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to activate Telegram bot"));
        }
    }

    // DTO class for registration request
    public static class RegisterUserRequest {
        private String username;

        public RegisterUserRequest() {}

        public RegisterUserRequest(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    // DTO class for updating Telegram recipients
    public static class UpdateTelegramRecipientsRequest {
        private String recipients;

        public UpdateTelegramRecipientsRequest() {}

        public UpdateTelegramRecipientsRequest(String recipients) {
            this.recipients = recipients;
        }

        public String getRecipients() {
            return recipients;
        }

        public void setRecipients(String recipients) {
            this.recipients = recipients;
        }
    }

    // DTO class for Telegram bot activation
    public static class ActivateTelegramBotRequest {
        private String code;
        private String chatId;

        public ActivateTelegramBotRequest() {}

        public ActivateTelegramBotRequest(String code, String chatId) {
            this.code = code;
            this.chatId = chatId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }
    }
} 