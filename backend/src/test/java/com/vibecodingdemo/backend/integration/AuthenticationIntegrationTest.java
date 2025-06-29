package com.vibecodingdemo.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecodingdemo.backend.controller.UserController;
import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        userRepository.deleteAll();
    }

    @Test
    void completeAuthenticationFlow_Success() throws Exception {
        // Test 1: Register a new user and get JWT token
        UserController.RegisterUserRequest registerRequest = 
            new UserController.RegisterUserRequest("integrationtestuser");

        MvcResult registerResult = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("integrationtestuser"))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Extract the JWT token from the response
        String responseContent = registerResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        String jwtToken = responseJson.get("token").asText();
        
        assertNotNull(jwtToken);
        assertFalse(jwtToken.isEmpty());

        // Verify user was created in database
        User savedUser = userRepository.findByUsername("integrationtestuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("integrationtestuser", savedUser.getUsername());

        // Test 2: Use the JWT token to access protected endpoint
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("integrationtestuser"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void accessProtectedEndpoint_WithoutToken_Unauthorized() throws Exception {
        // Test 3: Try to access protected endpoint without token
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithInvalidToken_Unauthorized() throws Exception {
        // Test 4: Try to access protected endpoint with invalid token
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithMalformedAuthHeader_Unauthorized() throws Exception {
        // Test 5: Try to access protected endpoint with malformed auth header
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Basic invalidheader"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerUser_DuplicateUsername_BadRequest() throws Exception {
        // First registration
        UserController.RegisterUserRequest firstRequest = 
            new UserController.RegisterUserRequest("duplicateuser");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // Second registration with same username
        UserController.RegisterUserRequest secondRequest = 
            new UserController.RegisterUserRequest("duplicateuser");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists: duplicateuser"));
    }

    @Test
    void registerUser_EmptyUsername_BadRequest() throws Exception {
        UserController.RegisterUserRequest request = 
            new UserController.RegisterUserRequest("");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is required"));
    }

    @Test
    void registerUser_NullUsername_BadRequest() throws Exception {
        UserController.RegisterUserRequest request = 
            new UserController.RegisterUserRequest(null);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is required"));
    }

    @Test
    void multipleUsersAuthentication_Success() throws Exception {
        // Register first user
        UserController.RegisterUserRequest user1Request = 
            new UserController.RegisterUserRequest("user1");

        MvcResult user1Result = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Request)))
                .andExpect(status().isOk())
                .andReturn();

        String user1Token = objectMapper.readTree(user1Result.getResponse().getContentAsString())
                .get("token").asText();

        // Register second user
        UserController.RegisterUserRequest user2Request = 
            new UserController.RegisterUserRequest("user2");

        MvcResult user2Result = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Request)))
                .andExpect(status().isOk())
                .andReturn();

        String user2Token = objectMapper.readTree(user2Result.getResponse().getContentAsString())
                .get("token").asText();

        // Verify each user can access their own info with their token
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user2"));

        // Verify tokens are different
        assertNotEquals(user1Token, user2Token);
    }

    @Test
    void accessPublicEndpoint_WithoutToken_Success() throws Exception {
        // Registration endpoint should be accessible without authentication
        UserController.RegisterUserRequest request = 
            new UserController.RegisterUserRequest("publicuser");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("publicuser"))
                .andExpect(jsonPath("$.token").exists());
    }
} 