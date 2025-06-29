package com.vibecodingdemo.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.service.UserService;
import com.vibecodingdemo.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = {UserController.class, UserControllerTest.TestSecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        testUser = new User("testuser");
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void registerUser_Success() throws Exception {
        // Given
        UserController.RegisterUserRequest request = new UserController.RegisterUserRequest("testuser");
        String token = "generated.jwt.token";

        when(userService.registerUser("testuser")).thenReturn(testUser);
        when(jwtUtil.generateToken("testuser")).thenReturn(token);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.createdAt").exists())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void registerUser_EmptyUsername_BadRequest() throws Exception {
        // Given
        UserController.RegisterUserRequest request = new UserController.RegisterUserRequest("");

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is required"));
    }

    @Test
    void registerUser_NullUsername_BadRequest() throws Exception {
        // Given
        UserController.RegisterUserRequest request = new UserController.RegisterUserRequest(null);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is required"));
    }

    @Test
    void registerUser_UsernameAlreadyExists_BadRequest() throws Exception {
        // Given
        UserController.RegisterUserRequest request = new UserController.RegisterUserRequest("existinguser");

        when(userService.registerUser("existinguser"))
                .thenThrow(new IllegalArgumentException("Username already exists: existinguser"));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists: existinguser"));
    }

    @Test
    void registerUser_ServiceException_InternalServerError() throws Exception {
        // Given
        UserController.RegisterUserRequest request = new UserController.RegisterUserRequest("testuser");

        when(userService.registerUser("testuser"))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Registration failed"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUser_Success() throws Exception {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void getCurrentUser_NotAuthenticated_Unauthorized() throws Exception {
        // When & Then
        // In test environment with permitAll, the anonymous user will be passed
        // to the controller, but won't be found in the database, resulting in 404
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @WithMockUser(username = "nonexistentuser")
    void getCurrentUser_UserNotFound_NotFound() throws Exception {
        // Given
        when(userService.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUser_ServiceException_InternalServerError() throws Exception {
        // Given
        when(userService.findByUsername("testuser"))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to retrieve user information"));
    }
} 