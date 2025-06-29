package com.vibecodingdemo.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotUpdateListenerTest {

    @Mock
    private TelegramBotService telegramBotService;
    
    @Mock
    private Update update;
    
    @Mock
    private Message message;
    
    private TelegramBotUpdateListener updateListener;
    
    private static final String TEST_BOT_TOKEN = "test-bot-token";
    private static final String TEST_BOT_USERNAME = "test-bot";
    private static final String TEST_CHAT_ID = "123456789";
    
    @BeforeEach
    void setUp() {
        updateListener = new TelegramBotUpdateListener(telegramBotService);
        
        // Set private fields using reflection
        ReflectionTestUtils.setField(updateListener, "botToken", TEST_BOT_TOKEN);
        ReflectionTestUtils.setField(updateListener, "botUsername", TEST_BOT_USERNAME);
    }
    
    @Test
    void testGetBotToken() {
        // When
        String token = updateListener.getBotToken();
        
        // Then
        assertEquals(TEST_BOT_TOKEN, token);
    }
    
    @Test
    void testGetBotUsername() {
        // When
        String username = updateListener.getBotUsername();
        
        // Then
        assertEquals(TEST_BOT_USERNAME, username);
    }
    
    @Test
    void testOnUpdateReceived_StartCommand() {
        // Given
        String startCommand = "/start 123456";
        
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(Long.valueOf(TEST_CHAT_ID));
        when(message.getText()).thenReturn(startCommand);
        
        // When
        updateListener.onUpdateReceived(update);
        
        // Then
        verify(telegramBotService).handleStartCommand(TEST_CHAT_ID, startCommand);
        verify(telegramBotService, never()).sendMessage(eq(TEST_CHAT_ID), anyString());
    }
    
    @Test
    void testOnUpdateReceived_HelpCommand() {
        // Given
        String helpCommand = "/help";
        
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(Long.valueOf(TEST_CHAT_ID));
        when(message.getText()).thenReturn(helpCommand);
        
        // When
        updateListener.onUpdateReceived(update);
        
        // Then
        verify(telegramBotService).sendMessage(eq(TEST_CHAT_ID), contains("VibeCodeDemo Bot Help"));
        verify(telegramBotService, never()).handleStartCommand(anyString(), anyString());
    }
    
    @Test
    void testOnUpdateReceived_RegularMessage() {
        // Given
        String regularMessage = "Hello bot";
        
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(Long.valueOf(TEST_CHAT_ID));
        when(message.getText()).thenReturn(regularMessage);
        
        // When
        updateListener.onUpdateReceived(update);
        
        // Then
        verify(telegramBotService).sendMessage(eq(TEST_CHAT_ID), contains("Hello! To activate notifications"));
        verify(telegramBotService, never()).handleStartCommand(anyString(), anyString());
    }
    
    @Test
    void testOnUpdateReceived_NoMessage() {
        // Given
        when(update.hasMessage()).thenReturn(false);
        
        // When
        updateListener.onUpdateReceived(update);
        
        // Then
        verify(telegramBotService, never()).sendMessage(anyString(), anyString());
        verify(telegramBotService, never()).handleStartCommand(anyString(), anyString());
    }
    
    @Test
    void testOnUpdateReceived_NullText() {
        // Given
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(Long.valueOf(TEST_CHAT_ID));
        when(message.getText()).thenReturn(null);
        
        // When
        updateListener.onUpdateReceived(update);
        
        // Then
        verify(telegramBotService).sendMessage(eq(TEST_CHAT_ID), contains("Hello! To activate notifications"));
        verify(telegramBotService, never()).handleStartCommand(anyString(), anyString());
    }
} 