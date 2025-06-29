package com.vibecodingdemo.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotServiceTest {

    @Mock
    private UserService userService;
    
    @Mock
    private RestTemplate restTemplate;
    
    private TelegramBotService telegramBotService;
    
    private static final String TEST_BOT_TOKEN = "test-bot-token";
    private static final String TEST_BOT_USERNAME = "test-bot";
    private static final String TEST_CHAT_ID = "123456789";
    
    @BeforeEach
    void setUp() {
        telegramBotService = new TelegramBotService(userService);
        
        // Set private fields using reflection
        ReflectionTestUtils.setField(telegramBotService, "botToken", TEST_BOT_TOKEN);
        ReflectionTestUtils.setField(telegramBotService, "botUsername", TEST_BOT_USERNAME);
        ReflectionTestUtils.setField(telegramBotService, "restTemplate", restTemplate);
    }
    
    @Test
    void testSendMessage_Success() {
        // Given
        String message = "Test message";
        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"ok\":true}", HttpStatus.OK);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(mockResponse);
        
        // When
        boolean result = telegramBotService.sendMessage(TEST_CHAT_ID, message);
        
        // Then
        assertTrue(result);
        verify(restTemplate).exchange(anyString(), any(), any(), eq(String.class));
    }
    
    @Test
    void testSendMessage_JsonFormatting() {
        // Given
        String jsonMessage = "{\"event\":\"test\",\"data\":\"value\"}";
        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"ok\":true}", HttpStatus.OK);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(mockResponse);
        
        // When
        boolean result = telegramBotService.sendMessage(TEST_CHAT_ID, jsonMessage);
        
        // Then
        assertTrue(result);
        verify(restTemplate).exchange(anyString(), any(), any(), eq(String.class));
    }
    
    @Test
    void testSendMessage_Failure() {
        // Given
        String message = "Test message";
        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"ok\":false}", HttpStatus.BAD_REQUEST);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(mockResponse);
        
        // When
        boolean result = telegramBotService.sendMessage(TEST_CHAT_ID, message);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testHandleStartCommand_Success() {
        // Given
        String command = "/start 123456";
        String activationCode = "123456";
        
        doNothing().when(userService).activateTelegramBot(activationCode, TEST_CHAT_ID);
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"ok\":true}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(mockResponse);
        
        // When
        telegramBotService.handleStartCommand(TEST_CHAT_ID, command);
        
        // Then
        verify(userService).activateTelegramBot(activationCode, TEST_CHAT_ID);
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
    }
    
    @Test
    void testHandleStartCommand_InvalidFormat() {
        // Given
        String invalidCommand = "/start";
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"ok\":true}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(mockResponse);
        
        // When
        telegramBotService.handleStartCommand(TEST_CHAT_ID, invalidCommand);
        
        // Then
        verify(userService, never()).activateTelegramBot(anyString(), anyString());
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
    }
    
    @Test
    void testHandleStartCommand_ActivationFailure() {
        // Given
        String command = "/start 123456";
        String activationCode = "123456";
        
        doThrow(new IllegalArgumentException("Invalid activation code"))
            .when(userService).activateTelegramBot(activationCode, TEST_CHAT_ID);
        
        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"ok\":true}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(mockResponse);
        
        // When
        telegramBotService.handleStartCommand(TEST_CHAT_ID, command);
        
        // Then
        verify(userService).activateTelegramBot(activationCode, TEST_CHAT_ID);
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
    }
    
    @Test
    void testGetBotUsername() {
        // When
        String username = telegramBotService.getBotUsername();
        
        // Then
        assertEquals(TEST_BOT_USERNAME, username);
    }
    
    @Test
    void testGetBotToken() {
        // When
        String token = telegramBotService.getBotToken();
        
        // Then
        assertEquals(TEST_BOT_TOKEN, token);
    }
} 