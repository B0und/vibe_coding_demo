package com.vibecodingdemo.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotMessageFormattingTest {

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
        
        // Mock successful response for all tests
        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"ok\":true}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(mockResponse);
    }
    
    @Test
    void testSendMessage_SimpleJsonFormatting() {
        // Given
        String jsonMessage = "{\"event\":\"user_registered\",\"username\":\"john_doe\"}";
        
        // When
        telegramBotService.sendMessage(TEST_CHAT_ID, jsonMessage);
        
        // Then
        verify(restTemplate).exchange(
            anyString(), 
            any(), 
            argThat(request -> {
                @SuppressWarnings("unchecked")
                var body = (java.util.Map<String, Object>) request.getBody();
                String text = (String) body.get("text");
                return text.contains("🔔") && 
                       text.contains("<b>Notification</b>") &&
                       text.contains("<b>event:</b> user_registered") &&
                       text.contains("<b>username:</b> john_doe");
            }), 
            eq(String.class)
        );
    }
    
    @Test
    void testSendMessage_NestedJsonFormatting() {
        // Given
        String nestedJsonMessage = "{\"event\":\"order_created\",\"data\":{\"order_id\":123,\"amount\":99.99}}";
        
        // When
        telegramBotService.sendMessage(TEST_CHAT_ID, nestedJsonMessage);
        
        // Then
        verify(restTemplate).exchange(
            anyString(), 
            any(), 
            argThat(request -> {
                @SuppressWarnings("unchecked")
                var body = (java.util.Map<String, Object>) request.getBody();
                String text = (String) body.get("text");
                return text.contains("🔔") && 
                       text.contains("<b>Notification</b>") &&
                       text.contains("<b>event:</b> order_created") &&
                       text.contains("<b>data:</b>") &&
                       text.contains("<b>order_id:</b> 123") &&
                       text.contains("<b>amount:</b> 99.99");
            }), 
            eq(String.class)
        );
    }
    
    @Test
    void testSendMessage_JsonArrayFormatting() {
        // Given
        String arrayJsonMessage = "{\"notifications\":[\"email_sent\",\"sms_sent\"],\"status\":\"completed\"}";
        
        // When
        telegramBotService.sendMessage(TEST_CHAT_ID, arrayJsonMessage);
        
        // Then
        verify(restTemplate).exchange(
            anyString(), 
            any(), 
            argThat(request -> {
                @SuppressWarnings("unchecked")
                var body = (java.util.Map<String, Object>) request.getBody();
                String text = (String) body.get("text");
                return text.contains("🔔") && 
                       text.contains("<b>Notification</b>") &&
                       text.contains("<b>notifications:</b>") &&
                       text.contains("• email_sent") &&
                       text.contains("• sms_sent") &&
                       text.contains("<b>status:</b> completed");
            }), 
            eq(String.class)
        );
    }
    
    @Test
    void testSendMessage_ComplexJsonFormatting() {
        // Given
        String complexJsonMessage = "{\"event\":\"payment_processed\",\"user\":{\"id\":123,\"name\":\"John Doe\"},\"items\":[{\"name\":\"Product A\",\"price\":29.99},{\"name\":\"Product B\",\"price\":19.99}],\"total\":49.98}";
        
        // When
        telegramBotService.sendMessage(TEST_CHAT_ID, complexJsonMessage);
        
        // Then
        verify(restTemplate).exchange(
            anyString(), 
            any(), 
            argThat(request -> {
                @SuppressWarnings("unchecked")
                var body = (java.util.Map<String, Object>) request.getBody();
                String text = (String) body.get("text");
                return text.contains("🔔") && 
                       text.contains("<b>Notification</b>") &&
                       text.contains("<b>event:</b> payment_processed") &&
                       text.contains("<b>user:</b>") &&
                       text.contains("<b>id:</b> 123") &&
                       text.contains("<b>name:</b> John Doe") &&
                       text.contains("<b>items:</b>") &&
                       text.contains("Product A") &&
                       text.contains("29.99") &&
                       text.contains("Product B") &&
                       text.contains("19.99") &&
                       text.contains("<b>total:</b> 49.98");
            }), 
            eq(String.class)
        );
    }
    
    @Test
    void testSendMessage_PlainTextPassthrough() {
        // Given
        String plainTextMessage = "This is a plain text message";
        
        // When
        telegramBotService.sendMessage(TEST_CHAT_ID, plainTextMessage);
        
        // Then
        verify(restTemplate).exchange(
            anyString(), 
            any(), 
            argThat(request -> {
                @SuppressWarnings("unchecked")
                var body = (java.util.Map<String, Object>) request.getBody();
                String text = (String) body.get("text");
                return text.equals(plainTextMessage);
            }), 
            eq(String.class)
        );
    }
    
    @Test
    void testSendMessage_InvalidJsonPassthrough() {
        // Given
        String invalidJsonMessage = "{invalid json content}";
        
        // When
        telegramBotService.sendMessage(TEST_CHAT_ID, invalidJsonMessage);
        
        // Then
        verify(restTemplate).exchange(
            anyString(), 
            any(), 
            argThat(request -> {
                @SuppressWarnings("unchecked")
                var body = (java.util.Map<String, Object>) request.getBody();
                String text = (String) body.get("text");
                return text.equals(invalidJsonMessage);
            }), 
            eq(String.class)
        );
    }
    
    @Test
    void testSendMessage_EmptyJsonFormatting() {
        // Given
        String emptyJsonMessage = "{}";
        
        // When
        telegramBotService.sendMessage(TEST_CHAT_ID, emptyJsonMessage);
        
        // Then
        verify(restTemplate).exchange(
            anyString(), 
            any(), 
            argThat(request -> {
                @SuppressWarnings("unchecked")
                var body = (java.util.Map<String, Object>) request.getBody();
                String text = (String) body.get("text");
                return text.contains("🔔") && 
                       text.contains("<b>Notification</b>");
            }), 
            eq(String.class)
        );
    }
    
    @Test
    void testSendMessage_NullMessageHandling() {
        // Given
        String nullMessage = null;
        
        // When
        telegramBotService.sendMessage(TEST_CHAT_ID, nullMessage);
        
        // Then
        verify(restTemplate).exchange(
            anyString(), 
            any(), 
            argThat(request -> {
                @SuppressWarnings("unchecked")
                var body = (java.util.Map<String, Object>) request.getBody();
                String text = (String) body.get("text");
                return text == null;
            }), 
            eq(String.class)
        );
    }
} 