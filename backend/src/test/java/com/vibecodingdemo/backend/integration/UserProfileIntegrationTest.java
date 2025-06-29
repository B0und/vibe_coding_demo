package com.vibecodingdemo.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecodingdemo.backend.controller.UserController;
import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.repository.UserRepository;
import com.vibecodingdemo.backend.service.UserService;
import com.vibecodingdemo.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CacheManager cacheManager;

    private String testUsername = "testuser";
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Create a test user
        User testUser = userService.registerUser(testUsername);
        jwtToken = jwtUtil.generateToken(testUsername);
    }

    @Test
    void testUpdateTelegramRecipients() throws Exception {
        UserController.UpdateTelegramRecipientsRequest request = 
            new UserController.UpdateTelegramRecipientsRequest("user1;user2;user3");

        mockMvc.perform(put("/api/users/profile/telegram-recipients")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Telegram recipients updated successfully"));

        // Verify the database was updated
        User updatedUser = userRepository.findByUsername(testUsername).orElseThrow();
        assertEquals("user1;user2;user3", updatedUser.getTelegramRecipients());
    }

    @Test
    void testUpdateTelegramRecipientsUnauthorized() throws Exception {
        UserController.UpdateTelegramRecipientsRequest request = 
            new UserController.UpdateTelegramRecipientsRequest("user1;user2;user3");

        mockMvc.perform(put("/api/users/profile/telegram-recipients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGenerateTelegramActivationCode() throws Exception {
        mockMvc.perform(post("/api/users/profile/telegram-activation-code")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activationCode").exists())
                .andExpect(jsonPath("$.activationCode").isString());

        // Verify the code is in the cache
        Cache cache = cacheManager.getCache("activationCodes");
        assertNotNull(cache);
        String cachedCode = cache.get(testUsername, String.class);
        assertNotNull(cachedCode);
        assertEquals(6, cachedCode.length());
    }

    @Test
    void testGenerateTelegramActivationCodeUnauthorized() throws Exception {
        mockMvc.perform(post("/api/users/profile/telegram-activation-code"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testActivateTelegramBot() throws Exception {
        // First generate an activation code
        String activationCode = userService.generateTelegramActivationCode(testUsername);
        
        UserController.ActivateTelegramBotRequest request = 
            new UserController.ActivateTelegramBotRequest(activationCode, "123456789");

        mockMvc.perform(post("/api/users/telegram-activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Telegram bot activated successfully"));

        // Verify the database was updated
        User updatedUser = userRepository.findByUsername(testUsername).orElseThrow();
        assertEquals("123456789", updatedUser.getTelegramChatId());

        // Verify the code was removed from cache
        Cache cache = cacheManager.getCache("activationCodes");
        assertNotNull(cache);
        String cachedCode = cache.get(testUsername, String.class);
        assertNull(cachedCode);
    }

    @Test
    void testActivateTelegramBotWithInvalidCode() throws Exception {
        UserController.ActivateTelegramBotRequest request = 
            new UserController.ActivateTelegramBotRequest("999999", "123456789");

        mockMvc.perform(post("/api/users/telegram-activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid or expired activation code"));
    }

    @Test
    void testActivateTelegramBotWithMissingFields() throws Exception {
        UserController.ActivateTelegramBotRequest request = 
            new UserController.ActivateTelegramBotRequest("", "");

        mockMvc.perform(post("/api/users/telegram-activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Activation code is required"));
    }
} 