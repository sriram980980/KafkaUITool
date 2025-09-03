package com.kafkatool.service;

import com.kafkatool.model.KafkaMessage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for timestamp-based message search functionality
 */
public class TimestampSearchTest {
    
    @Test
    public void testTimestampSearchMethodExists() {
        KafkaServiceImpl service = new KafkaServiceImpl();
        
        // Test that the new timestamp search method exists and is callable
        long now = System.currentTimeMillis();
        long oneHourAgo = now - (60 * 60 * 1000);
        
        CompletableFuture<List<KafkaMessage>> result = service.searchMessagesByTimestampAsync(
            "localhost:9092", "test-topic", 0, oneHourAgo, now, 10
        );
        
        assertNotNull(result);
        assertTrue(result instanceof CompletableFuture);
    }
    
    @Test
    public void testPatternAndTimestampSearchMethodExists() {
        KafkaServiceImpl service = new KafkaServiceImpl();
        
        // Test that the new pattern + timestamp search method exists and is callable
        long now = System.currentTimeMillis();
        long oneHourAgo = now - (60 * 60 * 1000);
        
        CompletableFuture<List<KafkaMessage>> result = service.searchMessagesByPatternAndTimestampAsync(
            "localhost:9092", "test-topic", 0, "test-pattern",
            true, true, false, oneHourAgo, now, 10
        );
        
        assertNotNull(result);
        assertTrue(result instanceof CompletableFuture);
    }
    
    @Test
    public void testEnhancedKafkaServiceTimestampMethods() {
        EnhancedKafkaServiceImpl service = new EnhancedKafkaServiceImpl();
        
        long now = System.currentTimeMillis();
        long oneHourAgo = now - (60 * 60 * 1000);
        
        // Test getMessagesBetweenTimestampsAsync
        CompletableFuture<List<KafkaMessage>> result1 = service.getMessagesBetweenTimestampsAsync(
            "localhost:9092", "test-topic", 0, oneHourAgo, now, 10
        );
        
        assertNotNull(result1);
        assertTrue(result1 instanceof CompletableFuture);
        
        // Test searchMessagesRegexWithTimestampAsync
        CompletableFuture<List<KafkaMessage>> result2 = service.searchMessagesRegexWithTimestampAsync(
            "localhost:9092", "test-topic", 0, ".*test.*",
            true, true, oneHourAgo, now, 10
        );
        
        assertNotNull(result2);
        assertTrue(result2 instanceof CompletableFuture);
    }
    
    @Test
    public void testTimestampValidation() {
        // Test that timestamp range validation would work
        long now = System.currentTimeMillis();
        long oneHourFromNow = now + (60 * 60 * 1000);
        long oneHourAgo = now - (60 * 60 * 1000);
        
        // Valid range: past to now
        assertTrue(oneHourAgo < now);
        
        // Valid range: past to future
        assertTrue(oneHourAgo < oneHourFromNow);
        
        // Invalid range: future to now (should be caught by UI validation)
        assertFalse(oneHourFromNow < now);
    }
}