package com.kafkatool.ui;

import com.kafkatool.model.LLMProviderInfo;
import com.kafkatool.ui.controller.MainController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.ComboBoxMatchers.hasItems;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * UI Regression tests for the chat functionality and LLM provider connection
 * Tests the fixes for dropdown connection mapping and dynamic LLM provider selection
 */
@ExtendWith(ApplicationExtension.class)
public class ChatUIRegressionTest {

    private FxRobot robot;
    private MainController controller;
    private Stage primaryStage;

    @Start
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        
        // Load the main FXML - would need to be updated to include the new LLM provider ComboBox
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        this.controller = loader.getController();
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Kafka UI Tool - Regression Test");
        stage.show();
    }

    @BeforeEach
    public void setUp(FxRobot robot) {
        this.robot = robot;
    }

    @AfterEach
    public void tearDown() {
        Platform.runLater(() -> {
            if (primaryStage != null) {
                primaryStage.close();
            }
        });
    }

    @Test
    public void testLLMProviderDropdownMapping() {
        // Test Case 1: Verify LLM provider dropdown is populated
        robot.sleep(1000); // Wait for initialization
        
        ComboBox<LLMProviderInfo> llmProviderCombo = robot.lookup("#llmProviderComboBox").query();
        assertNotNull(llmProviderCombo, "LLM provider ComboBox should exist");
        
        // Should have default providers loaded
        assertTrue(llmProviderCombo.getItems().size() > 0, "Should have default LLM providers");
        
        // Test the toString() format for dropdown display (name + type)
        Platform.runLater(() -> {
            if (!llmProviderCombo.getItems().isEmpty()) {
                LLMProviderInfo firstProvider = llmProviderCombo.getItems().get(0);
                String displayText = firstProvider.toString();
                assertTrue(displayText.contains("("), "Display text should contain parentheses for type");
                assertTrue(displayText.contains(")"), "Display text should contain closing parentheses");
                
                // Test that getMapKey() returns just the name for map operations
                String mapKey = firstProvider.getMapKey();
                assertFalse(mapKey.contains("("), "Map key should not contain parentheses");
                assertFalse(mapKey.contains(")"), "Map key should not contain parentheses");
            }
        });
    }

    @Test
    public void testChatConnectionRequiresProviderSelection() {
        // Test Case 2: Connect button should be disabled until provider is selected
        robot.sleep(1000);
        
        Button connectChatButton = robot.lookup("#connectChatButton").query();
        assertNotNull(connectChatButton, "Connect chat button should exist");
        
        // Initially should be disabled (no provider selected)
        assertTrue(connectChatButton.isDisabled(), "Connect button should be disabled initially");
        
        // Select a provider
        ComboBox<LLMProviderInfo> llmProviderCombo = robot.lookup("#llmProviderComboBox").query();
        if (llmProviderCombo != null && !llmProviderCombo.getItems().isEmpty()) {
            Platform.runLater(() -> {
                llmProviderCombo.getSelectionModel().selectFirst();
            });
            
            robot.sleep(500); // Wait for selection to process
            
            // Now connect button should be enabled
            assertFalse(connectChatButton.isDisabled(), "Connect button should be enabled after provider selection");
        }
    }

    @Test
    public void testChatConnectionWithSelectedProvider() {
        // Test Case 3: Chat connection should use selected provider, not default to ollama
        robot.sleep(1000);
        
        ComboBox<LLMProviderInfo> llmProviderCombo = robot.lookup("#llmProviderComboBox").query();
        Button connectChatButton = robot.lookup("#connectChatButton").query();
        TextArea chatMessagesArea = robot.lookup("#chatMessagesArea").query();
        
        if (llmProviderCombo != null && !llmProviderCombo.getItems().isEmpty()) {
            // Select a specific provider (not necessarily ollama)
            Platform.runLater(() -> {
                // Find a non-ollama provider if available
                LLMProviderInfo selectedProvider = llmProviderCombo.getItems().stream()
                    .filter(p -> !p.getType().equals("ollama"))
                    .findFirst()
                    .orElse(llmProviderCombo.getItems().get(0));
                
                llmProviderCombo.getSelectionModel().select(selectedProvider);
            });
            
            robot.sleep(500);
            
            // Click connect
            robot.clickOn(connectChatButton);
            robot.sleep(500);
            
            // Verify connection message uses the selected provider
            String chatText = chatMessagesArea.getText();
            assertTrue(chatText.contains("Connected to"), "Chat should show connection message");
            
            // Should not hardcode "ollama" - should use selected provider
            LLMProviderInfo selectedProvider = llmProviderCombo.getSelectionModel().getSelectedItem();
            if (selectedProvider != null) {
                assertTrue(chatText.contains(selectedProvider.toString()), 
                    "Connection message should reference the selected provider");
            }
        }
    }

    @Test
    public void testChatMessageSending() {
        // Test Case 4: Chat message sending works with selected provider
        robot.sleep(1000);
        
        ComboBox<LLMProviderInfo> llmProviderCombo = robot.lookup("#llmProviderComboBox").query();
        Button connectChatButton = robot.lookup("#connectChatButton").query();
        TextField chatInputField = robot.lookup("#chatInputField").query();
        TextArea chatMessagesArea = robot.lookup("#chatMessagesArea").query();
        
        if (llmProviderCombo != null && !llmProviderCombo.getItems().isEmpty()) {
            // Select provider and connect
            Platform.runLater(() -> {
                llmProviderCombo.getSelectionModel().selectFirst();
            });
            robot.sleep(500);
            robot.clickOn(connectChatButton);
            robot.sleep(500);
            
            // Send a test message
            robot.clickOn(chatInputField);
            robot.write("Test message for regression testing");
            robot.press(javafx.scene.input.KeyCode.ENTER);
            robot.sleep(1000);
            
            // Verify message appears in chat area
            String chatText = chatMessagesArea.getText();
            assertTrue(chatText.contains("Test message for regression testing"), 
                "Sent message should appear in chat area");
            assertTrue(chatText.contains("You:"), "Message should be attributed to user");
            
            // Should get a response from the selected provider
            robot.sleep(2000); // Wait for simulated response
            chatText = chatMessagesArea.getText();
            
            LLMProviderInfo selectedProvider = llmProviderCombo.getSelectionModel().getSelectedItem();
            if (selectedProvider != null) {
                assertTrue(chatText.contains(selectedProvider.getMapKey()), 
                    "Response should be attributed to the selected provider");
            }
        }
    }

    @Test
    public void testProviderSelectionPersistence() {
        // Test Case 5: Verify provider selection state is maintained
        robot.sleep(1000);
        
        ComboBox<LLMProviderInfo> llmProviderCombo = robot.lookup("#llmProviderComboBox").query();
        
        if (llmProviderCombo != null && llmProviderCombo.getItems().size() > 1) {
            // Select second provider
            Platform.runLater(() -> {
                llmProviderCombo.getSelectionModel().select(1);
            });
            robot.sleep(500);
            
            // Verify selection is maintained
            LLMProviderInfo selectedProvider = llmProviderCombo.getSelectionModel().getSelectedItem();
            assertNotNull(selectedProvider, "Provider selection should be maintained");
            assertEquals(llmProviderCombo.getItems().get(1), selectedProvider, 
                "Selected provider should be the second item");
        }
    }

    /**
     * This test specifically validates the fix for the dropdown mapping issue
     * mentioned in the problem statement
     */
    @Test
    public void testDropdownMappingIssue() {
        // Test Case 6: Validate that dropdown display != map key
        robot.sleep(1000);
        
        ComboBox<LLMProviderInfo> llmProviderCombo = robot.lookup("#llmProviderComboBox").query();
        
        if (llmProviderCombo != null && !llmProviderCombo.getItems().isEmpty()) {
            Platform.runLater(() -> {
                // Simulate the original problem: dropdown shows "name (type)" 
                // but map lookup should use just "name"
                for (LLMProviderInfo provider : llmProviderCombo.getItems()) {
                    String dropdownDisplay = provider.toString(); // "name (type)"
                    String mapKey = provider.getMapKey(); // "name" only
                    
                    // These should be different
                    assertNotEquals(dropdownDisplay, mapKey, 
                        "Dropdown display should be different from map key");
                    
                    // Map key should not contain parentheses
                    assertFalse(mapKey.contains("("), "Map key should not contain parentheses");
                    assertFalse(mapKey.contains(")"), "Map key should not contain parentheses");
                    
                    // Dropdown display should contain parentheses for type
                    assertTrue(dropdownDisplay.contains("("), "Dropdown display should contain type in parentheses");
                    assertTrue(dropdownDisplay.contains(")"), "Dropdown display should contain closing parentheses");
                }
            });
        }
    }
}