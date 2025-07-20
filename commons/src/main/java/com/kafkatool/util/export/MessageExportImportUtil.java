package com.kafkatool.util.export;

import com.kafkatool.model.KafkaMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for exporting and importing Kafka messages in various formats
 */
public class MessageExportImportUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageExportImportUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Export messages to JSON format
     */
    public static void exportToJson(List<KafkaMessage> messages, File outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            // Create export metadata
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("exportTimestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            exportData.put("totalMessages", messages.size());
            exportData.put("exportVersion", "1.0");
            
            // Convert messages to exportable format
            List<Map<String, Object>> exportMessages = messages.stream()
                .map(MessageExportImportUtil::messageToMap)
                .collect(Collectors.toList());
            
            exportData.put("messages", exportMessages);
            
            // Write to file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, exportData);
            
            logger.info("Exported {} messages to JSON file: {}", messages.size(), outputFile.getAbsolutePath());
        }
    }
    
    /**
     * Export messages to CSV format
     */
    public static void exportToCsv(List<KafkaMessage> messages, File outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            // Write CSV header
            csvPrinter.printRecord("Topic", "Partition", "Offset", "Timestamp", "Key", "Value", "Headers");
            
            // Write message data
            for (KafkaMessage message : messages) {
                csvPrinter.printRecord(
                    message.getTopic(),
                    message.getPartition(),
                    message.getOffset(),
                    message.getTimestamp() != null ? message.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                    message.getKey(),
                    message.getValue(),
                    headersToString(message.getHeaders())
                );
            }
            
            logger.info("Exported {} messages to CSV file: {}", messages.size(), outputFile.getAbsolutePath());
        }
    }
    
    /**
     * Export messages to Avro format (simplified JSON representation)
     */
    public static void exportToAvro(List<KafkaMessage> messages, File outputFile) throws IOException {
        // For now, export as JSON with Avro-like structure
        // In a full implementation, this would use actual Avro serialization
        
        try (FileWriter writer = new FileWriter(outputFile)) {
            Map<String, Object> avroExport = new HashMap<>();
            avroExport.put("namespace", "com.kafkatool.avro");
            avroExport.put("type", "record");
            avroExport.put("name", "KafkaMessageExport");
            avroExport.put("fields", Arrays.asList(
                Map.of("name", "topic", "type", "string"),
                Map.of("name", "partition", "type", "int"),
                Map.of("name", "offset", "type", "long"),
                Map.of("name", "timestamp", "type", Arrays.asList("null", "string")),
                Map.of("name", "key", "type", Arrays.asList("null", "string")),
                Map.of("name", "value", "type", Arrays.asList("null", "string")),
                Map.of("name", "headers", "type", Arrays.asList("null", "string"))
            ));
            
            List<Map<String, Object>> records = messages.stream()
                .map(message -> {
                    Map<String, Object> record = new HashMap<>();
                    record.put("topic", message.getTopic());
                    record.put("partition", message.getPartition());
                    record.put("offset", message.getOffset());
                    record.put("timestamp", message.getTimestamp() != null ? 
                        message.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
                    record.put("key", message.getKey());
                    record.put("value", message.getValue());
                    record.put("headers", headersToString(message.getHeaders()));
                    return record;
                })
                .collect(Collectors.toList());
            
            avroExport.put("records", records);
            
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, avroExport);
            
            logger.info("Exported {} messages to Avro-JSON file: {}", messages.size(), outputFile.getAbsolutePath());
        }
    }
    
    /**
     * Import messages from JSON format
     */
    public static List<KafkaMessage> importFromJson(File inputFile) throws IOException {
        try (FileReader reader = new FileReader(inputFile)) {
            JsonNode rootNode = objectMapper.readTree(reader);
            JsonNode messagesNode = rootNode.get("messages");
            
            List<KafkaMessage> messages = new ArrayList<>();
            
            if (messagesNode != null && messagesNode.isArray()) {
                for (JsonNode messageNode : messagesNode) {
                    KafkaMessage message = mapToMessage(objectMapper.convertValue(messageNode, Map.class));
                    messages.add(message);
                }
            }
            
            logger.info("Imported {} messages from JSON file: {}", messages.size(), inputFile.getAbsolutePath());
            return messages;
        }
    }
    
    /**
     * Import messages from CSV format
     */
    public static List<KafkaMessage> importFromCsv(File inputFile) throws IOException {
        List<KafkaMessage> messages = new ArrayList<>();
        
        try (FileReader reader = new FileReader(inputFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                KafkaMessage message = new KafkaMessage();
                message.setTopic(record.get("Topic"));
                message.setPartition(Integer.parseInt(record.get("Partition")));
                message.setOffset(Long.parseLong(record.get("Offset")));
                
                String timestampStr = record.get("Timestamp");
                if (timestampStr != null && !timestampStr.isEmpty()) {
                    message.setTimestamp(LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                
                message.setKey(record.get("Key"));
                message.setValue(record.get("Value"));
                message.setHeaders(parseHeadersFromString(record.get("Headers")));
                
                messages.add(message);
            }
        }
        
        logger.info("Imported {} messages from CSV file: {}", messages.size(), inputFile.getAbsolutePath());
        return messages;
    }
    
    /**
     * Batch export messages with progress tracking
     */
    public static void exportWithProgress(List<KafkaMessage> messages, File outputFile, 
                                        String format, ProgressCallback callback) throws IOException {
        int batchSize = 1000;
        int totalMessages = messages.size();
        int processedMessages = 0;
        
        callback.onProgress(0, totalMessages, "Starting export...");
        
        switch (format.toLowerCase()) {
            case "json":
                exportToJson(messages, outputFile);
                break;
            case "csv":
                exportToCsv(messages, outputFile);
                break;
            case "avro":
                exportToAvro(messages, outputFile);
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
        
        callback.onProgress(totalMessages, totalMessages, "Export completed successfully");
    }
    
    /**
     * Batch import messages with progress tracking
     */
    public static List<KafkaMessage> importWithProgress(File inputFile, String format, 
                                                      ProgressCallback callback) throws IOException {
        callback.onProgress(0, 100, "Starting import...");
        
        List<KafkaMessage> messages;
        
        switch (format.toLowerCase()) {
            case "json":
                messages = importFromJson(inputFile);
                break;
            case "csv":
                messages = importFromCsv(inputFile);
                break;
            default:
                throw new IllegalArgumentException("Unsupported import format: " + format);
        }
        
        callback.onProgress(100, 100, "Import completed successfully");
        return messages;
    }
    
    /**
     * Filter messages during export/import
     */
    public static List<KafkaMessage> filterMessages(List<KafkaMessage> messages, MessageFilter filter) {
        return messages.stream()
            .filter(filter::accept)
            .collect(Collectors.toList());
    }
    
    /**
     * Transform messages during export/import
     */
    public static List<KafkaMessage> transformMessages(List<KafkaMessage> messages, MessageTransformer transformer) {
        return messages.stream()
            .map(transformer::transform)
            .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private static Map<String, Object> messageToMap(KafkaMessage message) {
        Map<String, Object> map = new HashMap<>();
        map.put("topic", message.getTopic());
        map.put("partition", message.getPartition());
        map.put("offset", message.getOffset());
        map.put("timestamp", message.getTimestamp() != null ? 
            message.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        map.put("key", message.getKey());
        map.put("value", message.getValue());
        map.put("headers", message.getHeaders());
        return map;
    }
    
    private static KafkaMessage mapToMessage(Map<String, Object> map) {
        KafkaMessage message = new KafkaMessage();
        message.setTopic((String) map.get("topic"));
        message.setPartition((Integer) map.get("partition"));
        message.setOffset(((Number) map.get("offset")).longValue());
        
        String timestampStr = (String) map.get("timestamp");
        if (timestampStr != null) {
            message.setTimestamp(LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        message.setKey((String) map.get("key"));
        message.setValue((String) map.get("value"));
        message.setHeaders((Map<String, String>) map.get("headers"));
        
        return message;
    }
    
    private static String headersToString(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        return headers.entrySet().stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue())
            .collect(Collectors.joining(";"));
    }
    
    private static Map<String, String> parseHeadersFromString(String headersStr) {
        if (headersStr == null || headersStr.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, String> headers = new HashMap<>();
        String[] pairs = headersStr.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                headers.put(keyValue[0], keyValue[1]);
            }
        }
        return headers;
    }
    
    // Functional interfaces for filtering and transformation
    
    @FunctionalInterface
    public interface MessageFilter {
        boolean accept(KafkaMessage message);
    }
    
    @FunctionalInterface
    public interface MessageTransformer {
        KafkaMessage transform(KafkaMessage message);
    }
    
    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(int processed, int total, String status);
    }
}