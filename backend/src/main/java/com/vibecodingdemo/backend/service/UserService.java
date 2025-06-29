package com.vibecodingdemo.backend.service;

import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new user with the given username
     * @param username the username for the new user
     * @return the created user
     * @throws IllegalArgumentException if username is null, empty, or already exists
     */
    public User registerUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        if (userRepository.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User user = new User(username.trim());
        return userRepository.save(user);
    }

    /**
     * Find a user by username
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
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
        builder.authorities("USER"); // Basic user role

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
} 