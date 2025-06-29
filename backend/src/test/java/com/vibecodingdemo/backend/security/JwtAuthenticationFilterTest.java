package com.vibecodingdemo.backend.security;

import com.vibecodingdemo.backend.service.UserService;
import com.vibecodingdemo.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Clear SecurityContext before each test
        SecurityContextHolder.clearContext();
        
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_NoAuthenticationSet() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_InvalidAuthorizationHeader_NoAuthenticationSet() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic invalidheader");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_InvalidToken_NoAuthenticationSet() throws ServletException, IOException {
        // Given
        String token = "invalid.jwt.token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenThrow(new IllegalArgumentException("Invalid token"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(userService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_UserNotFound_NoAuthenticationSet() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        String username = "nonexistentuser";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenThrow(new UsernameNotFoundException("User not found"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString(), any(UserDetails.class));
    }

    @Test
    void doFilterInternal_InvalidTokenForUser_NoAuthenticationSet() throws ServletException, IOException {
        // Given
        String token = "invalid.jwt.token";
        String authHeader = "Bearer " + token;
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_AuthenticationAlreadySet_SkipsTokenProcessing() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        String username = "testuser";

        // Set existing authentication
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn(username);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(existingAuth, authentication); // Should remain unchanged
        verify(filterChain).doFilter(request, response);
        verify(userService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateToken(anyString(), any(UserDetails.class));
    }

    @Test
    void doFilterInternal_EmptyBearerToken_NoAuthenticationSet() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer ";

        when(request.getHeader("Authorization")).thenReturn(authHeader);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_WhitespaceToken_HandledGracefully() throws ServletException, IOException {
        // Given
        String token = "   ";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        // No need to mock jwtUtil.extractUsername since it won't be called for empty tokens

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }
} 