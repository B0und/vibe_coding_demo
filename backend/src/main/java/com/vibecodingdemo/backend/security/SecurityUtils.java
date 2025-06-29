package com.vibecodingdemo.backend.security;

import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    private final UserService userService;

    @Autowired
    public SecurityUtils(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get the currently authenticated user
     * @return Optional containing the current user, empty if not authenticated
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    /**
     * Get the currently authenticated username
     * @return Optional containing the current username, empty if not authenticated
     */
    public Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        return Optional.of(authentication.getName());
    }

    /**
     * Check if the current user has admin role
     * @return true if current user is admin, false otherwise
     */
    public boolean isCurrentUserAdmin() {
        return getCurrentUser()
                .map(User::isAdmin)
                .orElse(false);
    }

    /**
     * Check if the current user is the owner of the resource
     * @param resourceOwnerId the ID of the resource owner
     * @return true if current user owns the resource, false otherwise
     */
    public boolean isCurrentUserOwner(Long resourceOwnerId) {
        return getCurrentUser()
                .map(user -> user.getId().equals(resourceOwnerId))
                .orElse(false);
    }

    /**
     * Check if the current user is the owner of the resource by username
     * @param resourceOwnerUsername the username of the resource owner
     * @return true if current user owns the resource, false otherwise
     */
    public boolean isCurrentUserOwner(String resourceOwnerUsername) {
        return getCurrentUsername()
                .map(username -> username.equals(resourceOwnerUsername))
                .orElse(false);
    }

    /**
     * Check if the current user can access the resource (either owner or admin)
     * @param resourceOwnerId the ID of the resource owner
     * @return true if current user can access the resource, false otherwise
     */
    public boolean canCurrentUserAccess(Long resourceOwnerId) {
        return isCurrentUserAdmin() || isCurrentUserOwner(resourceOwnerId);
    }

    /**
     * Check if the current user can access the resource by username (either owner or admin)
     * @param resourceOwnerUsername the username of the resource owner
     * @return true if current user can access the resource, false otherwise
     */
    public boolean canCurrentUserAccess(String resourceOwnerUsername) {
        return isCurrentUserAdmin() || isCurrentUserOwner(resourceOwnerUsername);
    }

    /**
     * Throw an exception if the current user cannot access the resource
     * @param resourceOwnerId the ID of the resource owner
     * @throws SecurityException if access is denied
     */
    public void requireAccessToResource(Long resourceOwnerId) throws SecurityException {
        if (!canCurrentUserAccess(resourceOwnerId)) {
            throw new SecurityException("Access denied: insufficient permissions");
        }
    }

    /**
     * Throw an exception if the current user cannot access the resource by username
     * @param resourceOwnerUsername the username of the resource owner
     * @throws SecurityException if access is denied
     */
    public void requireAccessToResource(String resourceOwnerUsername) throws SecurityException {
        if (!canCurrentUserAccess(resourceOwnerUsername)) {
            throw new SecurityException("Access denied: insufficient permissions");
        }
    }

    /**
     * Throw an exception if the current user is not an admin
     * @throws SecurityException if user is not admin
     */
    public void requireAdminRole() throws SecurityException {
        if (!isCurrentUserAdmin()) {
            throw new SecurityException("Access denied: admin role required");
        }
    }
} 