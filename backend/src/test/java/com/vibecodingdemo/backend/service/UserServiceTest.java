package com.vibecodingdemo.backend.service;

import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser");
        testUser.setId(1L);
    }

    @Test
    void registerUser_Success() {
        // Given
        String username = "newuser";
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.registerUser(username);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository).existsByUsername(username);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        // Given
        String username = "existinguser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(username)
        );
        assertEquals("Username already exists: " + username, exception.getMessage());
        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_NullUsername() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(null)
        );
        assertEquals("Username cannot be null or empty", exception.getMessage());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmptyUsername() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser("")
        );
        assertEquals("Username cannot be null or empty", exception.getMessage());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WhitespaceUsername() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser("   ")
        );
        assertEquals("Username cannot be null or empty", exception.getMessage());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_Success() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void findByUsername_NotFound() {
        // Given
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername(username);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void findByUsername_NullUsername() {
        // When
        Optional<User> result = userService.findByUsername(null);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void findByUsername_EmptyUsername() {
        // When
        Optional<User> result = userService.findByUsername("");

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void loadUserByUsername_Success() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = userService.loadUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("USER")));
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Given
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username)
        );
        assertEquals("User not found: " + username, exception.getMessage());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void findById_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userRepository).findById(userId);
    }

    @Test
    void findById_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }
} 