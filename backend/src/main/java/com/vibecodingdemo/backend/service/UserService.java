package com.vibecodingdemo.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final SecureRandom secureRandom;

    @Autowired
    public UserService(UserRepository userRepository, CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Get all users
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @CacheEvict(value = "users", key = "#username")
    public User registerUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        // Check if user already exists and return it
        Optional<User> existingUser = userRepository.findByUsername(username.trim());
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Create new user if doesn't exist
        User user = new User(username.trim());
        return userRepository.save(user);
    }

    /**
     * Find a user by username
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    @Cacheable(value = "users", key = "#username")
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username.trim());
    }

    /**
     * Load user by username for Spring Security
     * @param username the username to load
     * @return UserDetails for the user
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.getUsername());
        // Since we don't have passwords in our MVP, we'll use a placeholder
        builder.password("{noop}"); // {noop} means no password encoder
        
        // Set authorities based on user role
        String authority = user.getRole() == User.Role.ADMIN ? "ADMIN" : "USER";
        builder.authorities(authority);

        return builder.build();
    }

    /**
     * Get user by ID
     * @param id the user ID
     * @return Optional containing the user if found, empty otherwise
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Update the Telegram recipients for a user
     * @param username the username of the user
     * @param recipients semicolon-separated list of Telegram recipients
     * @throws IllegalArgumentException if user is not found
     */
    @CacheEvict(value = "users", key = "#username")
    public void updateTelegramRecipients(String username, String recipients) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        user.setTelegramRecipients(recipients);
        userRepository.save(user);
    }

    /**
     * Generate a unique one-time activation code for Telegram bot activation
     * @param username the username of the user
     * @return the generated 6-digit activation code
     * @throws IllegalArgumentException if user is not found
     */
    public String generateTelegramActivationCode(String username) {
        // Verify user exists
        userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Generate a secure 6-digit code
        int code = 100000 + secureRandom.nextInt(900000);
        String activationCode = String.valueOf(code);

        // Store in cache with username as key
        org.springframework.cache.Cache cache = cacheManager.getCache("activationCodes");
        if (cache != null) {
            cache.put(username, activationCode);
        }

        return activationCode;
    }

    /**
     * Activate Telegram bot by linking a chat ID to a user account using an activation code
     * @param code the activation code
     * @param chatId the Telegram chat ID
     * @throws IllegalArgumentException if code is invalid or expired
     */
    @CacheEvict(value = "users", allEntries = true) // Clear all user cache since we don't know which user
    public void activateTelegramBot(String code, String chatId) {
        org.springframework.cache.Cache cache = cacheManager.getCache("activationCodes");
        if (cache == null) {
            throw new IllegalArgumentException("Cache not available");
        }

        // Find the username associated with this code by checking all cached values
        String foundUsername = null;
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
            (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
        
        for (Object key : nativeCache.asMap().keySet()) {
            Object cachedValue = cache.get(key);
            if (cachedValue != null && code.equals(cachedValue.toString())) {
                foundUsername = (String) key;
                break;
            }
        }

        if (foundUsername == null) {
            throw new IllegalArgumentException("Invalid or expired activation code");
        }

        final String username = foundUsername; // Make it effectively final for lambda

        // Update user's Telegram chat ID
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        user.setTelegramChatId(chatId);
        userRepository.save(user);

        // Remove the used activation code from cache
        cache.evictIfPresent(username);
    }

    /**
     * Activate Telegram bot directly using username and chat ID (simplified flow)
     * @param username the username of the user
     * @param chatId the Telegram chat ID
     * @throws IllegalArgumentException if user is not found
     */
    @CacheEvict(value = "users", key = "#username")
    public void activateTelegramBotDirect(String username, String chatId) {
        // Update user's Telegram chat ID directly
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        user.setTelegramChatId(chatId);
        userRepository.save(user);
    }

    /**
     * Create an admin user
     * @param username the username for the admin user
     * @return the created admin user
     * @throws IllegalArgumentException if username is null or empty, or if user already exists
     */
    @CacheEvict(value = "users", key = "#username")
    public User createAdminUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByUsername(username.trim());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User already exists: " + username);
        }

        // Create new admin user
        User user = new User(username.trim(), User.Role.ADMIN);
        return userRepository.save(user);
    }

    /**
     * Promote a user to admin role
     * @param username the username of the user to promote
     * @throws IllegalArgumentException if user is not found
     */
    @CacheEvict(value = "users", key = "#username")
    public void promoteToAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        user.setRole(User.Role.ADMIN);
        userRepository.save(user);
    }

    /**
     * Check if a user has admin role
     * @param username the username to check
     * @return true if user is admin, false otherwise
     */
    @Cacheable(value = "users", key = "#username")
    public boolean isAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRole() == User.Role.ADMIN)
                .orElse(false);
    }
} 