package com.vibecodingdemo.backend.controller;

import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.security.SecurityUtils;
import com.vibecodingdemo.backend.service.UserService;
import com.vibecodingdemo.backend.util.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final SecurityUtils securityUtils;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil, SecurityUtils securityUtils) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginUserRequest request) {
        try {
            // Validate request
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username is required"));
            }

            // Find the user
            Optional<User> userOpt = userService.findByUsername(request.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();

            // Generate JWT tokens
            UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().toString(),
                "createdAt", user.getCreatedAt()
            ));
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Login failed"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        try {
            // Validate request
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username is required"));
            }

            // Register the user
            User user = userService.registerUser(request.getUsername());

            // Generate JWT tokens
            UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().toString(),
                "createdAt", user.getCreatedAt()
            ));
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Registration failed"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            // Validate request
            if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Refresh token is required"));
            }

            String refreshToken = request.getRefreshToken();

            // Validate refresh token
            if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid or expired refresh token"));
            }

            // Extract username from refresh token
            String username = jwtUtil.extractUsername(refreshToken);

            // Load user details
            UserDetails userDetails = userService.loadUserByUsername(username);

            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(userDetails);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "Token refresh failed"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            // Get the currently authenticated user using SecurityUtils
            Optional<User> userOpt = securityUtils.getCurrentUser();
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
            }

            User user = userOpt.get();
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole().toString());
            response.put("createdAt", user.getCreatedAt());
            response.put("updatedAt", user.getUpdatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve user information"));
        }
    }

    @PutMapping("/profile/telegram-recipients")
    public ResponseEntity<?> updateTelegramRecipients(@Valid @RequestBody UpdateTelegramRecipientsRequest request) {
        try {
            // Get the currently authenticated user using SecurityUtils
            Optional<String> usernameOpt = securityUtils.getCurrentUsername();
            
            if (usernameOpt.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
            }

            String username = usernameOpt.get();
            
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
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update Telegram recipients"));
        }
    }

    @PostMapping("/profile/telegram-activation-code")
    public ResponseEntity<?> generateTelegramActivationCode() {
        try {
            // Get the currently authenticated user using SecurityUtils
            Optional<String> usernameOpt = securityUtils.getCurrentUsername();
            
            if (usernameOpt.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
            }

            String username = usernameOpt.get();
            
            // Generate activation code
            String activationCode = userService.generateTelegramActivationCode(username);

            return ResponseEntity.ok(Map.of("activationCode", activationCode));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate activation code"));
        }
    }

    @PostMapping("/telegram-activate")
    public ResponseEntity<?> activateTelegramBot(@Valid @RequestBody ActivateTelegramBotRequest request) {
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

    // DTO class for user login request
    public static class LoginUserRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
        private String username;

        public LoginUserRequest() {}

        public LoginUserRequest(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    // DTO class for user registration request
    public static class RegisterUserRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
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
        @Size(max = 500, message = "Recipients list cannot exceed 500 characters")
        @Pattern(regexp = "^[a-zA-Z0-9@._,;\\s-]*$", message = "Recipients can only contain letters, numbers, @, dots, commas, semicolons, spaces, underscores, and hyphens")
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

    // DTO class for Telegram bot activation request
    public static class ActivateTelegramBotRequest {
        @NotBlank(message = "Activation code is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "Activation code must be exactly 6 digits")
        private String code;
        
        @NotBlank(message = "Chat ID is required")
        @Pattern(regexp = "^-?[0-9]+$", message = "Chat ID must be a valid number")
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

    // DTO class for refresh token request
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;

        public RefreshTokenRequest() {}

        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
} 