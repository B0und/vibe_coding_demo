package com.vibecodingdemo.backend.repository;

import com.vibecodingdemo.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndRetrieveUser() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setTelegramRecipients("user1;user2;user3");
        user.setTelegramChatId("123456789");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getTelegramRecipients()).isEqualTo("user1;user2;user3");
        assertThat(savedUser.getTelegramChatId()).isEqualTo("123456789");
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserById() {
        // Given
        User user = new User();
        user.setUsername("findbyid");
        user.setTelegramRecipients("recipient1");
        User savedUser = userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("findbyid");
        assertThat(foundUser.get().getTelegramRecipients()).isEqualTo("recipient1");
    }

    @Test
    void shouldFindUserByUsername() {
        // Given
        User user = new User();
        user.setUsername("uniqueuser");
        user.setTelegramRecipients("test@example.com");
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByUsername("uniqueuser");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("uniqueuser");
        assertThat(foundUser.get().getTelegramRecipients()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundByUsername() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldCheckIfUserExistsByUsername() {
        // Given
        User user = new User();
        user.setUsername("existinguser");
        userRepository.save(user);

        // When & Then
        assertThat(userRepository.existsByUsername("existinguser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistentuser")).isFalse();
    }

    @Test
    void shouldHandleNullableTelegramChatId() {
        // Given
        User user = new User();
        user.setUsername("nullchatid");
        user.setTelegramRecipients("recipient");
        // telegramChatId is intentionally left null

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getTelegramChatId()).isNull();
        
        // Verify we can retrieve it
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getTelegramChatId()).isNull();
    }

    @Test
    void shouldEnforceUsernameUniqueness() {
        // Given
        User user1 = new User();
        user1.setUsername("duplicatetest");
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("duplicatetest");

        // When & Then
        // This should throw a constraint violation exception
        try {
            userRepository.save(user2);
            userRepository.flush(); // Force the constraint check
            assertThat(false).as("Expected constraint violation").isTrue();
        } catch (Exception e) {
            // Expected behavior - unique constraint violation
            assertThat(e.getMessage()).containsIgnoringCase("constraint");
        }
    }

    @Test
    void shouldUpdateUserTimestamp() throws InterruptedException {
        // Given
        User user = new User();
        user.setUsername("timestamptest");
        User savedUser = userRepository.save(user);
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);
        
        // When
        savedUser.setTelegramRecipients("updated");
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertThat(updatedUser.getUpdatedAt()).isAfter(updatedUser.getCreatedAt());
    }
} 