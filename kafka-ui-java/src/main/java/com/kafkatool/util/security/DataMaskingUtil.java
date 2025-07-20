package com.kafkatool.util.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Data masking and encryption utilities for sensitive data protection
 */
public class DataMaskingUtil {
    
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // Common patterns for sensitive data
    private static final Map<String, Pattern> SENSITIVE_PATTERNS = new HashMap<>();
    
    static {
        SENSITIVE_PATTERNS.put("email", Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"));
        SENSITIVE_PATTERNS.put("phone", Pattern.compile("\\b\\d{3}-\\d{3}-\\d{4}\\b|\\b\\(\\d{3}\\)\\s*\\d{3}-\\d{4}\\b"));
        SENSITIVE_PATTERNS.put("ssn", Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"));
        SENSITIVE_PATTERNS.put("credit_card", Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"));
        SENSITIVE_PATTERNS.put("ip_address", Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b"));
    }
    
    /**
     * Mask sensitive data based on configured rules
     */
    public static String maskData(String data, Map<String, String> maskingRules) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        String maskedData = data;
        
        for (Map.Entry<String, String> rule : maskingRules.entrySet()) {
            String patternName = rule.getKey();
            String maskingType = rule.getValue();
            
            Pattern pattern = SENSITIVE_PATTERNS.get(patternName);
            if (pattern != null) {
                maskedData = applyMasking(maskedData, pattern, maskingType);
            }
        }
        
        return maskedData;
    }
    
    /**
     * Apply specific masking strategy to matched patterns
     */
    private static String applyMasking(String data, Pattern pattern, String maskingType) {
        switch (maskingType.toLowerCase()) {
            case "full":
                return pattern.matcher(data).replaceAll("***MASKED***");
            case "partial":
                return pattern.matcher(data).replaceAll(match -> {
                    String matched = match.group();
                    if (matched.length() <= 4) {
                        return "*".repeat(matched.length());
                    }
                    return matched.substring(0, 2) + "*".repeat(matched.length() - 4) + matched.substring(matched.length() - 2);
                });
            case "hash":
                return pattern.matcher(data).replaceAll(match -> 
                    "HASH:" + passwordEncoder.encode(match.group()).substring(0, 10));
            case "encrypt":
                return pattern.matcher(data).replaceAll(match -> {
                    try {
                        return "ENC:" + encrypt(match.group());
                    } catch (Exception e) {
                        return "***ENCRYPTED***";
                    }
                });
            default:
                return data;
        }
    }
    
    /**
     * Generate a new AES encryption key
     */
    public static SecretKey generateEncryptionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }
    
    /**
     * Encrypt sensitive data
     */
    public static String encrypt(String data) throws Exception {
        SecretKey secretKey = generateEncryptionKey();
        return encrypt(data, secretKey);
    }
    
    /**
     * Encrypt data with provided key
     */
    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }
    
    /**
     * Decrypt data with provided key
     */
    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData);
    }
    
    /**
     * Create secret key from string
     */
    public static SecretKey createKeyFromString(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ENCRYPTION_ALGORITHM);
    }
    
    /**
     * Convert secret key to string for storage
     */
    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    /**
     * Generate secure random password
     */
    public static String generateSecurePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Hash password for storage
     */
    public static String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
    
    /**
     * Verify password against hash
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
    
    /**
     * Validate data against GDPR compliance rules
     */
    public static boolean isGdprCompliant(String data, Map<String, String> complianceRules) {
        // Check if data contains PII that needs to be handled according to GDPR
        for (String patternName : SENSITIVE_PATTERNS.keySet()) {
            Pattern pattern = SENSITIVE_PATTERNS.get(patternName);
            if (pattern.matcher(data).find()) {
                String rule = complianceRules.get(patternName);
                if (rule == null || rule.equals("none")) {
                    return false; // PII found but no compliance rule defined
                }
            }
        }
        return true;
    }
    
    /**
     * Anonymize data for analytics while preserving utility
     */
    public static String anonymizeForAnalytics(String data) {
        String anonymized = data;
        
        // Replace specific identifiers with generic tokens
        anonymized = SENSITIVE_PATTERNS.get("email").matcher(anonymized)
            .replaceAll("user@domain.com");
        anonymized = SENSITIVE_PATTERNS.get("phone").matcher(anonymized)
            .replaceAll("xxx-xxx-xxxx");
        anonymized = SENSITIVE_PATTERNS.get("ssn").matcher(anonymized)
            .replaceAll("xxx-xx-xxxx");
        anonymized = SENSITIVE_PATTERNS.get("credit_card").matcher(anonymized)
            .replaceAll("xxxx-xxxx-xxxx-xxxx");
        
        return anonymized;
    }
}