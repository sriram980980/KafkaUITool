package com.kafkatool.ui;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.KafkaMessage;
import com.kafkatool.model.TopicInfo;
import com.kafkatool.util.KafkaAuthenticationUtil;
import com.kafkatool.util.JsonFormatter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for creating common dialogs
 */
public class DialogHelper {
    
    /**
     * Show dialog to add a new cluster
     */
    public static Optional<ClusterInfo> showAddClusterDialog() {
        return showClusterDialog(null, "Add Kafka Cluster", "Enter cluster connection details", "Add");
    }
    
    /**
     * Enhanced cluster dialog with authentication support
     */
    private static Optional<ClusterInfo> showClusterDialog(ClusterInfo existingCluster, String title, String header, String actionButtonText) {
        Dialog<ClusterInfo> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        
        ButtonType actionButtonType = new ButtonType(actionButtonText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(actionButtonType, ButtonType.CANCEL);
        
        // Main container
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        
        // Basic connection details
        GridPane basicGrid = new GridPane();
        basicGrid.setHgap(10);
        basicGrid.setVgap(10);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Cluster Name");
        TextField brokersField = new TextField();
        brokersField.setPromptText("localhost:9092");
        CheckBox connectByDefaultBox = new CheckBox("Connect by default");
        
        if (existingCluster != null) {
            nameField.setText(existingCluster.getName());
            brokersField.setText(existingCluster.getBrokerUrls());
            connectByDefaultBox.setSelected(existingCluster.isConnectByDefault());
        }
        
        basicGrid.add(new Label("Name:"), 0, 0);
        basicGrid.add(nameField, 1, 0);
        basicGrid.add(new Label("Broker URLs:"), 0, 1);
        basicGrid.add(brokersField, 1, 1);
        basicGrid.add(connectByDefaultBox, 1, 2);
        
        // Authentication section
        Label authLabel = new Label("Authentication:");
        authLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<AuthenticationType> authTypeCombo = new ComboBox<>(FXCollections.observableArrayList(AuthenticationType.values()));
        authTypeCombo.setValue(existingCluster != null ? existingCluster.getAuthenticationType() : AuthenticationType.NONE);
        
        // Authentication details container
        VBox authDetailsContainer = new VBox(10);
        
        // Create authentication fields that will be shown/hidden based on type
        AuthenticationFields authFields = new AuthenticationFields(existingCluster != null ? existingCluster.getAuthenticationConfig() : null);
        
        // Update authentication fields when type changes
        authTypeCombo.setOnAction(e -> {
            authDetailsContainer.getChildren().clear();
            AuthenticationType selectedType = authTypeCombo.getValue();
            if (selectedType != AuthenticationType.NONE) {
                authDetailsContainer.getChildren().addAll(authFields.getFieldsForType(selectedType));
            }
        });
        
        // Initialize with current auth type
        if (authTypeCombo.getValue() != AuthenticationType.NONE) {
            authDetailsContainer.getChildren().addAll(authFields.getFieldsForType(authTypeCombo.getValue()));
        }
        
        // Schema Registry section
        Label schemaRegistryLabel = new Label("Schema Registry:");
        schemaRegistryLabel.setStyle("-fx-font-weight: bold;");
        
        CheckBox enableSchemaRegistryBox = new CheckBox("Enable Schema Registry");
        if (existingCluster != null) {
            enableSchemaRegistryBox.setSelected(existingCluster.isSchemaRegistryEnabled());
        }
        
        // Schema Registry URL field
        TextField schemaRegistryUrlField = new TextField();
        schemaRegistryUrlField.setPromptText("http://localhost:8081");
        if (existingCluster != null && existingCluster.getSchemaRegistryUrl() != null) {
            schemaRegistryUrlField.setText(existingCluster.getSchemaRegistryUrl());
        }
        
        // Schema Registry authentication
        ComboBox<AuthenticationType> schemaAuthTypeCombo = new ComboBox<>(FXCollections.observableArrayList(AuthenticationType.values()));
        schemaAuthTypeCombo.setValue(existingCluster != null ? existingCluster.getSchemaRegistryAuthType() : AuthenticationType.NONE);
        
        // Schema Registry authentication details container
        VBox schemaAuthDetailsContainer = new VBox(10);
        
        // Create Schema Registry authentication fields
        AuthenticationFields schemaAuthFields = new AuthenticationFields(existingCluster != null ? existingCluster.getSchemaRegistryAuthConfig() : null);
        
        // Update Schema Registry authentication fields when type changes
        schemaAuthTypeCombo.setOnAction(e -> {
            schemaAuthDetailsContainer.getChildren().clear();
            AuthenticationType selectedType = schemaAuthTypeCombo.getValue();
            if (selectedType != AuthenticationType.NONE) {
                schemaAuthDetailsContainer.getChildren().addAll(schemaAuthFields.getFieldsForType(selectedType));
            }
        });
        
        // Initialize Schema Registry auth fields with current type
        if (schemaAuthTypeCombo.getValue() != AuthenticationType.NONE) {
            schemaAuthDetailsContainer.getChildren().addAll(schemaAuthFields.getFieldsForType(schemaAuthTypeCombo.getValue()));
        }
        
        // Schema Registry fields container (hidden by default)
        VBox schemaRegistryContainer = new VBox(10);
        GridPane schemaRegistryGrid = new GridPane();
        schemaRegistryGrid.setHgap(10);
        schemaRegistryGrid.setVgap(10);
        schemaRegistryGrid.add(new Label("URL:"), 0, 0);
        schemaRegistryGrid.add(schemaRegistryUrlField, 1, 0);
        
        schemaRegistryContainer.getChildren().addAll(
            schemaRegistryGrid,
            new Label("Authentication Type:"),
            schemaAuthTypeCombo,
            schemaAuthDetailsContainer
        );
        
        // Show/hide Schema Registry fields based on checkbox
        schemaRegistryContainer.setVisible(enableSchemaRegistryBox.isSelected());
        schemaRegistryContainer.setManaged(enableSchemaRegistryBox.isSelected());
        
        enableSchemaRegistryBox.setOnAction(e -> {
            boolean enabled = enableSchemaRegistryBox.isSelected();
            schemaRegistryContainer.setVisible(enabled);
            schemaRegistryContainer.setManaged(enabled);
        });
        
        // Add all components to main container
        mainContainer.getChildren().addAll(
            new Label("Connection Details:"),
            basicGrid,
            new Separator(),
            authLabel,
            authTypeCombo,
            authDetailsContainer,
            new Separator(),
            schemaRegistryLabel,
            enableSchemaRegistryBox,
            schemaRegistryContainer
        );
        
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(500, 600);
        dialog.getDialogPane().setContent(scrollPane);
        
        Platform.runLater(nameField::requestFocus);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == actionButtonType) {
                String name = nameField.getText().trim();
                String brokers = brokersField.getText().trim();
                
                if (name.isEmpty() || brokers.isEmpty()) {
                    return null;
                }
                
                AuthenticationType authType = authTypeCombo.getValue();
                AuthenticationConfig authConfig = null;
                
                if (authType != AuthenticationType.NONE) {
                    authConfig = authFields.createAuthenticationConfig(authType);
                    
                    // Validate authentication configuration with detailed error reporting
                    KafkaAuthenticationUtil.ValidationResult result = 
                        KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(authType, authConfig);
                    if (!result.isValid()) {
                        showErrorDialog("Authentication Validation Error", 
                            "Invalid " + authType.getDisplayName() + " Configuration", 
                            result.getErrorMessage());
                        return null;
                    }
                }
                
                // Handle Schema Registry configuration
                boolean schemaRegistryEnabled = enableSchemaRegistryBox.isSelected();
                String schemaRegistryUrl = null;
                AuthenticationType schemaAuthType = AuthenticationType.NONE;
                AuthenticationConfig schemaAuthConfig = null;
                
                if (schemaRegistryEnabled) {
                    schemaRegistryUrl = schemaRegistryUrlField.getText().trim();
                    
                    // Validate Schema Registry URL
                    if (schemaRegistryUrl.isEmpty()) {
                        showErrorDialog("Validation Error", "Schema Registry URL Required", 
                            "Please enter a Schema Registry URL when Schema Registry is enabled.");
                        return null;
                    }
                    
                    // Validate URL format
                    if (!schemaRegistryUrl.startsWith("http://") && !schemaRegistryUrl.startsWith("https://")) {
                        showErrorDialog("Validation Error", "Invalid Schema Registry URL", 
                            "Schema Registry URL must start with http:// or https://");
                        return null;
                    }
                    
                    schemaAuthType = schemaAuthTypeCombo.getValue();
                    
                    if (schemaAuthType != AuthenticationType.NONE) {
                        schemaAuthConfig = schemaAuthFields.createAuthenticationConfig(schemaAuthType);
                        
                        // Validate Schema Registry authentication configuration with detailed error reporting
                        KafkaAuthenticationUtil.ValidationResult result = 
                            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(schemaAuthType, schemaAuthConfig);
                        if (!result.isValid()) {
                            showErrorDialog("Schema Registry Authentication Validation Error", 
                                "Invalid Schema Registry " + schemaAuthType.getDisplayName() + " Configuration", 
                                result.getErrorMessage());
                            return null;
                        }
                    }
                }
                
                ClusterInfo cluster = new ClusterInfo(name, brokers, connectByDefaultBox.isSelected(), 
                    authType, authConfig, schemaRegistryEnabled, schemaRegistryUrl, schemaAuthType, schemaAuthConfig);
                
                // Copy existing status and version if editing
                if (existingCluster != null) {
                    cluster.setStatus(existingCluster.getStatus());
                    cluster.setKafkaVersion(existingCluster.getKafkaVersion());
                }
                
                return cluster;
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Show dialog to edit an existing cluster
     */
    public static Optional<ClusterInfo> showEditClusterDialog(ClusterInfo cluster) {
        return showClusterDialog(cluster, "Edit Kafka Cluster", "Update cluster connection details", "Save");
    }
    
    /**
     * Show dialog to create a new topic
     */
    public static Optional<TopicInfo> showCreateTopicDialog() {
        Dialog<TopicInfo> dialog = new Dialog<>();
        dialog.setTitle("Create Topic");
        dialog.setHeaderText("Enter topic details");
        
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Topic Name");
        Spinner<Integer> partitionsSpinner = new Spinner<>(1, 1000, 3);
        Spinner<Integer> replicationSpinner = new Spinner<>(1, 10, 1);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Partitions:"), 0, 1);
        grid.add(partitionsSpinner, 1, 1);
        grid.add(new Label("Replication Factor:"), 0, 2);
        grid.add(replicationSpinner, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        Platform.runLater(nameField::requestFocus);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    return new TopicInfo(name, partitionsSpinner.getValue(), replicationSpinner.getValue().shortValue());
                }
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Show topic configuration dialog
     */
    public static void showTopicConfigDialog(String topicName, Map<String, String> config) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Topic Configuration");
        dialog.setHeaderText("Configuration for topic: " + topicName);
        
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        for (Map.Entry<String, String> entry : config.entrySet()) {
            Label label = new Label(entry.getKey() + ": " + entry.getValue());
            content.getChildren().add(label);
        }
        
        if (config.isEmpty()) {
            content.getChildren().add(new Label("No custom configuration found."));
        }
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(400, 300);
        scrollPane.setFitToWidth(true);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }
    
    /**
     * Show message production dialog
     */
    public static Optional<MessageData> showProduceMessageDialog(String topicName) {
        Dialog<MessageData> dialog = new Dialog<>();
        dialog.setTitle("Produce Message");
        dialog.setHeaderText("Send message to topic: " + topicName);
        
        ButtonType sendButtonType = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField keyField = new TextField();
        keyField.setPromptText("Message Key (optional)");
        TextArea valueArea = new TextArea();
        valueArea.setPromptText("Message Value");
        valueArea.setPrefRowCount(5);
        Spinner<Integer> partitionSpinner = new Spinner<>(-1, 100, -1);
        partitionSpinner.getValueFactory().setValue(-1); // -1 means auto-select partition
        TextField headersField = new TextField();
        headersField.setPromptText("key1=value1,key2=value2");
        
        grid.add(new Label("Key:"), 0, 0);
        grid.add(keyField, 1, 0);
        grid.add(new Label("Value:"), 0, 1);
        grid.add(valueArea, 1, 1);
        grid.add(new Label("Partition:"), 0, 2);
        grid.add(partitionSpinner, 1, 2);
        grid.add(new Label("Headers:"), 0, 3);
        grid.add(headersField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == sendButtonType) {
                String key = keyField.getText().trim();
                String value = valueArea.getText();
                int partition = partitionSpinner.getValue();
                
                Map<String, String> headers = new HashMap<>();
                String headersText = headersField.getText().trim();
                if (!headersText.isEmpty()) {
                    for (String header : headersText.split(",")) {
                        String[] parts = header.split("=", 2);
                        if (parts.length == 2) {
                            headers.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
                
                return new MessageData(key, value, headers, partition);
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Show enhanced message production dialog with schema support
     */
    public static Optional<MessageData> showEnhancedProduceMessageDialog(String topicName) {
        Dialog<MessageData> dialog = new Dialog<>();
        dialog.setTitle("Produce Message - Enhanced");
        dialog.setHeaderText("Send message to topic: " + topicName);
        
        ButtonType sendButtonType = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        ButtonType validateButtonType = new ButtonType("Validate", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, validateButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefWidth(700);
        content.setPrefHeight(600);
        
        // Message format selection
        HBox formatRow = new HBox(10);
        ComboBox<MessageFormat> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll(MessageFormat.values());
        formatCombo.setValue(MessageFormat.STRING);
        formatRow.getChildren().addAll(new Label("Message Format:"), formatCombo);
        
        // Schema selection (enabled for AVRO/PROTOBUF)
        HBox schemaRow = new HBox(10);
        ComboBox<String> schemaCombo = new ComboBox<>();
        schemaCombo.setPromptText("Select schema (optional)");
        schemaCombo.setDisable(true);
        Button browseSchemaButton = new Button("Browse Schemas");
        browseSchemaButton.setDisable(true);
        schemaRow.getChildren().addAll(new Label("Schema:"), schemaCombo, browseSchemaButton);
        
        // Enable schema selection for AVRO/PROTOBUF
        formatCombo.setOnAction(e -> {
            MessageFormat format = formatCombo.getValue();
            boolean needsSchema = format == MessageFormat.AVRO || format == MessageFormat.PROTOBUF;
            schemaCombo.setDisable(!needsSchema);
            browseSchemaButton.setDisable(!needsSchema);
            
            if (needsSchema) {
                // Populate schema combo based on format
                schemaCombo.getItems().clear();
                if (format == MessageFormat.AVRO) {
                    schemaCombo.getItems().addAll("user-value", "order-value", "event-value");
                } else if (format == MessageFormat.PROTOBUF) {
                    schemaCombo.getItems().addAll("UserMessage", "OrderMessage", "EventMessage");
                }
            }
        });
        
        // Message fields
        GridPane messageGrid = new GridPane();
        messageGrid.setHgap(10);
        messageGrid.setVgap(10);
        
        TextField keyField = new TextField();
        keyField.setPromptText("Message Key (optional)");
        TextArea valueArea = new TextArea();
        valueArea.setPromptText("Message Value (JSON for AVRO/Protobuf)");
        valueArea.setPrefRowCount(8);
        valueArea.setWrapText(true);
        
        Spinner<Integer> partitionSpinner = new Spinner<>(-1, 100, -1);
        partitionSpinner.getValueFactory().setValue(-1);
        TextField headersField = new TextField();
        headersField.setPromptText("key1=value1,key2=value2");
        
        messageGrid.add(new Label("Key:"), 0, 0);
        messageGrid.add(keyField, 1, 0);
        messageGrid.add(new Label("Value:"), 0, 1);
        messageGrid.add(valueArea, 1, 1);
        messageGrid.add(new Label("Partition:"), 0, 2);
        messageGrid.add(partitionSpinner, 1, 2);
        messageGrid.add(new Label("Headers:"), 0, 3);
        messageGrid.add(headersField, 1, 3);
        
        // Schema preview area (collapsible) - Enhanced with editing capabilities
        TitledPane schemaPane = new TitledPane();
        schemaPane.setText("Schema Editor");
        schemaPane.setExpanded(false);
        
        VBox schemaContainer = new VBox(5);
        
        // Schema editing area with enhanced features
        TextArea schemaPreviewArea = new TextArea();
        schemaPreviewArea.setPromptText("Select a schema to view its definition, or create a custom schema");
        schemaPreviewArea.setEditable(true); // Made editable for enhanced UX
        schemaPreviewArea.setPrefRowCount(8);
        schemaPreviewArea.setWrapText(true);
        schemaPreviewArea.getStyleClass().add("schema-editor");
        
        // Schema actions toolbar
        HBox schemaActions = new HBox(5);
        Button formatSchemaButton = new Button("Format");
        Button validateSchemaButton = new Button("Validate");
        Button resetSchemaButton = new Button("Reset");
        Button loadSchemaButton = new Button("Load File");
        
        formatSchemaButton.getStyleClass().add("action-button");
        validateSchemaButton.getStyleClass().add("action-button");
        resetSchemaButton.getStyleClass().add("action-button");
        loadSchemaButton.getStyleClass().add("action-button");
        
        schemaActions.getChildren().addAll(formatSchemaButton, validateSchemaButton, resetSchemaButton, loadSchemaButton);
        
        // Schema validation feedback
        Label schemaValidationLabel = new Label();
        schemaValidationLabel.getStyleClass().add("validation-feedback");
        
        schemaContainer.getChildren().addAll(schemaPreviewArea, schemaActions, schemaValidationLabel);
        schemaPane.setContent(schemaContainer);
        
        // Validation feedback area
        Label validationLabel = new Label();
        validationLabel.getStyleClass().add("validation-feedback");
        
        content.getChildren().addAll(formatRow, schemaRow, messageGrid, schemaPane, validationLabel);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        dialog.getDialogPane().setContent(scrollPane);
        
        // Schema selection handler
        schemaCombo.setOnAction(e -> {
            String selectedSchema = schemaCombo.getValue();
            if (selectedSchema != null) {
                // Show sample schema (simplified)
                String sampleSchema = generateSampleSchema(formatCombo.getValue(), selectedSchema);
                schemaPreviewArea.setText(sampleSchema);
                schemaPane.setExpanded(true);
            }
        });
        
        // Validation button handler
        Button validateButton = (Button) dialog.getDialogPane().lookupButton(validateButtonType);
        validateButton.setOnAction(e -> {
            MessageFormat format = formatCombo.getValue();
            String value = valueArea.getText();
            String schema = schemaCombo.getValue();
            
            if (format == MessageFormat.JSON || format == MessageFormat.STRING) {
                validationLabel.setText("✓ No validation needed for " + format);
                validationLabel.setStyle("-fx-text-fill: green;");
            } else if (format == MessageFormat.AVRO && schema != null) {
                // Simplified validation
                if (isValidJson(value)) {
                    validationLabel.setText("✓ Message appears valid for Avro schema");
                    validationLabel.setStyle("-fx-text-fill: green;");
                } else {
                    validationLabel.setText("✗ Invalid JSON format for Avro message");
                    validationLabel.setStyle("-fx-text-fill: red;");
                }
            } else if (format == MessageFormat.PROTOBUF && schema != null) {
                // Simplified validation
                if (isValidJson(value)) {
                    validationLabel.setText("✓ Message appears valid for Protobuf schema");
                    validationLabel.setStyle("-fx-text-fill: green;");
                } else {
                    validationLabel.setText("✗ Invalid JSON format for Protobuf message");
                    validationLabel.setStyle("-fx-text-fill: red;");
                }
            } else {
                validationLabel.setText("⚠ Please select a schema for validation");
                validationLabel.setStyle("-fx-text-fill: orange;");
            }
        });
        
        // Enhanced schema editor button handlers
        formatSchemaButton.setOnAction(e -> {
            String currentSchema = schemaPreviewArea.getText();
            if (currentSchema != null && !currentSchema.trim().isEmpty()) {
                MessageFormat format = formatCombo.getValue();
                String formattedSchema = formatSchema(currentSchema, format);
                schemaPreviewArea.setText(formattedSchema);
                schemaValidationLabel.setText("✓ Schema formatted");
                schemaValidationLabel.setStyle("-fx-text-fill: green;");
            }
        });
        
        validateSchemaButton.setOnAction(e -> {
            String currentSchema = schemaPreviewArea.getText();
            MessageFormat format = formatCombo.getValue();
            if (currentSchema != null && !currentSchema.trim().isEmpty()) {
                String validationResult = validateSchema(currentSchema, format);
                schemaValidationLabel.setText(validationResult);
                if (validationResult.startsWith("✓")) {
                    schemaValidationLabel.setStyle("-fx-text-fill: green;");
                } else {
                    schemaValidationLabel.setStyle("-fx-text-fill: red;");
                }
            } else {
                schemaValidationLabel.setText("⚠ No schema to validate");
                schemaValidationLabel.setStyle("-fx-text-fill: orange;");
            }
        });
        
        resetSchemaButton.setOnAction(e -> {
            MessageFormat format = formatCombo.getValue();
            String schema = schemaCombo.getValue();
            if (format != null && schema != null) {
                String sampleSchema = generateSampleSchema(format, schema);
                schemaPreviewArea.setText(sampleSchema);
                schemaValidationLabel.setText("Schema reset to default");
                schemaValidationLabel.setStyle("-fx-text-fill: blue;");
            }
        });
        
        loadSchemaButton.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Load Schema File");
            MessageFormat format = formatCombo.getValue();
            if (format == MessageFormat.AVRO) {
                fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Avro Schema", "*.avsc", "*.json"));
            } else if (format == MessageFormat.PROTOBUF) {
                fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Protobuf Schema", "*.proto"));
            }
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*"));
            
            java.io.File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                try {
                    String schemaContent = java.nio.file.Files.readString(selectedFile.toPath());
                    schemaPreviewArea.setText(schemaContent);
                    schemaValidationLabel.setText("✓ Schema loaded from file: " + selectedFile.getName());
                    schemaValidationLabel.setStyle("-fx-text-fill: green;");
                    schemaPane.setExpanded(true);
                } catch (Exception ex) {
                    schemaValidationLabel.setText("✗ Failed to load file: " + ex.getMessage());
                    schemaValidationLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == sendButtonType) {
                String key = keyField.getText().trim();
                String value = valueArea.getText();
                int partition = partitionSpinner.getValue();
                MessageFormat format = formatCombo.getValue();
                String schema = schemaCombo.getValue();
                
                Map<String, String> headers = new HashMap<>();
                String headersText = headersField.getText().trim();
                if (!headersText.isEmpty()) {
                    for (String header : headersText.split(",")) {
                        String[] parts = header.split("=", 2);
                        if (parts.length == 2) {
                            headers.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
                
                return new MessageData(key, value, headers, partition, format, schema);
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    private static String generateSampleSchema(MessageFormat format, String schemaName) {
        if (format == MessageFormat.AVRO) {
            return "{\n" +
                   "  \"type\": \"record\",\n" +
                   "  \"name\": \"" + schemaName + "\",\n" +
                   "  \"fields\": [\n" +
                   "    {\"name\": \"id\", \"type\": \"string\"},\n" +
                   "    {\"name\": \"timestamp\", \"type\": \"long\"},\n" +
                   "    {\"name\": \"data\", \"type\": \"string\"}\n" +
                   "  ]\n" +
                   "}";
        } else if (format == MessageFormat.PROTOBUF) {
            return "syntax = \"proto3\";\n\n" +
                   "message " + schemaName + " {\n" +
                   "  string id = 1;\n" +
                   "  int64 timestamp = 2;\n" +
                   "  string data = 3;\n" +
                   "}";
        }
        return "No schema available";
    }
    
    private static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) return false;
        String trimmed = json.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
    
    /**
     * Show search messages dialog
     */
    public static Optional<SearchCriteria> showSearchMessagesDialog() {
        Dialog<SearchCriteria> dialog = new Dialog<>();
        dialog.setTitle("Search Messages");
        dialog.setHeaderText("Enter search criteria and preview options");
        
        ButtonType searchButtonType = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField patternField = new TextField();
        patternField.setPromptText("Search pattern");
        CheckBox searchKeyBox = new CheckBox("Search in key");
        CheckBox searchValueBox = new CheckBox("Search in value");
        CheckBox searchHeadersBox = new CheckBox("Search in headers");
        CheckBox enablePreviewBox = new CheckBox("Enable message preview");
        searchValueBox.setSelected(true);
        enablePreviewBox.setSelected(true);
        Spinner<Integer> maxResultsSpinner = new Spinner<>(1, 10000, 100);
        
        grid.add(new Label("Pattern:"), 0, 0);
        grid.add(patternField, 1, 0);
        grid.add(new Label("Search in:"), 0, 1);
        grid.add(searchKeyBox, 1, 1);
        grid.add(searchValueBox, 1, 2);
        grid.add(searchHeadersBox, 1, 3);
        grid.add(new Label("Options:"), 0, 4);
        grid.add(enablePreviewBox, 1, 4);
        grid.add(new Label("Max Results:"), 0, 5);
        grid.add(maxResultsSpinner, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        Platform.runLater(patternField::requestFocus);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == searchButtonType) {
                String pattern = patternField.getText().trim();
                if (!pattern.isEmpty()) {
                    return new SearchCriteria(pattern, searchKeyBox.isSelected(), 
                        searchValueBox.isSelected(), searchHeadersBox.isSelected(),
                        maxResultsSpinner.getValue(), enablePreviewBox.isSelected());
                }
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Show message preview dialog with expandable sections
     */
    public static void showMessagePreviewDialog(KafkaMessage message) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Message Preview");
        dialog.setHeaderText("Topic: " + message.getTopic() + " | Partition: " + 
                           message.getPartition() + " | Offset: " + message.getOffset());
        
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefWidth(600);
        content.setPrefHeight(500);
        
        // Message metadata
        GridPane metadataGrid = new GridPane();
        metadataGrid.setHgap(10);
        metadataGrid.setVgap(5);
        metadataGrid.add(new Label("Timestamp:"), 0, 0);
        metadataGrid.add(new Label(message.getTimestamp() != null ? 
            message.getTimestamp().toString() : "N/A"), 1, 0);
        
        // Key section (expandable)
        TitledPane keyPane = new TitledPane();
        keyPane.setText("Message Key" + (message.getKey() != null ? " (" + message.getKey().length() + " chars)" : " (null)"));
        keyPane.setExpanded(false);
        TextArea keyArea = new TextArea();
        keyArea.setText(message.getKey() != null ? message.getKey() : "null");
        keyArea.setEditable(false);
        keyArea.setPrefRowCount(8);
        keyArea.setWrapText(true);
        keyPane.setContent(keyArea);
        
        // Value section (expandable)
        TitledPane valuePane = new TitledPane();
        valuePane.setText("Message Value" + (message.getValue() != null ? " (" + message.getValue().length() + " chars)" : " (null)"));
        valuePane.setExpanded(true);
        TextArea valueArea = new TextArea();
        valueArea.setText(message.getValue() != null ? message.getValue() : "null");
        valueArea.setEditable(false);
        valueArea.setPrefRowCount(12);
        valueArea.setWrapText(true);
        valuePane.setContent(valueArea);
        
        // Headers section (expandable)
        TitledPane headersPane = new TitledPane();
        Map<String, String> headers = message.getHeaders();
        int headerCount = headers != null ? headers.size() : 0;
        headersPane.setText("Message Headers (" + headerCount + " headers)");
        headersPane.setExpanded(false);
        
        if (headers != null && !headers.isEmpty()) {
            VBox headersContent = new VBox(5);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                HBox headerRow = new HBox(10);
                Label keyLabel = new Label(entry.getKey() + ":");
                keyLabel.getStyleClass().add("header-key");
                Label valueLabel = new Label(entry.getValue());
                valueLabel.getStyleClass().add("header-value");
                headerRow.getChildren().addAll(keyLabel, valueLabel);
                headersContent.getChildren().add(headerRow);
            }
            ScrollPane headersScroll = new ScrollPane(headersContent);
            headersScroll.setPrefHeight(100);
            headersScroll.setFitToWidth(true);
            headersPane.setContent(headersScroll);
        } else {
            headersPane.setContent(new Label("No headers"));
        }
        
        content.getChildren().addAll(metadataGrid, keyPane, valuePane, headersPane);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }
    
    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Show error dialog
     */
    public static void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Show information dialog
     */
    public static void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Show settings dialog (simplified)
     */
    public static void showSettingsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Application Settings");
        alert.setContentText("Settings functionality will be implemented in future versions.");
        alert.showAndWait();
    }
    
    /**
     * Show about dialog
     */
    public static void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Kafka UI Tool v2.0");
        alert.setContentText("Cross-platform Kafka management tool built with JavaFX\n\n" +
            "Features:\n" +
            "• Cluster management\n" +
            "• Topic management\n" +
            "• Message viewing and producing\n" +
            "• Search capabilities\n" +
            "• Modern, responsive UI");
        alert.showAndWait();
    }
    
    // Helper classes for dialog results
    public static class MessageData {
        private final String key;
        private final String value;
        private final Map<String, String> headers;
        private final int partition;
        private final MessageFormat format;
        private final String schemaName;
        
        public MessageData(String key, String value, Map<String, String> headers, int partition) {
            this(key, value, headers, partition, MessageFormat.STRING, null);
        }
        
        public MessageData(String key, String value, Map<String, String> headers, int partition, 
                          MessageFormat format, String schemaName) {
            this.key = key;
            this.value = value;
            this.headers = headers;
            this.partition = partition;
            this.format = format;
            this.schemaName = schemaName;
        }
        
        public String getKey() { return key; }
        public String getValue() { return value; }
        public Map<String, String> getHeaders() { return headers; }
        public int getPartition() { return partition; }
        public MessageFormat getFormat() { return format; }
        public String getSchemaName() { return schemaName; }
    }
    
    public enum MessageFormat {
        STRING, JSON, AVRO, PROTOBUF
    }
    
    public static class SearchCriteria {
        private final String searchPattern;
        private final boolean searchInKey;
        private final boolean searchInValue;
        private final boolean searchInHeaders;
        private final int maxResults;
        private final boolean enablePreview;
        
        public SearchCriteria(String searchPattern, boolean searchInKey, boolean searchInValue, 
                            boolean searchInHeaders, int maxResults, boolean enablePreview) {
            this.searchPattern = searchPattern;
            this.searchInKey = searchInKey;
            this.searchInValue = searchInValue;
            this.searchInHeaders = searchInHeaders;
            this.maxResults = maxResults;
            this.enablePreview = enablePreview;
        }
        
        // Backward compatibility constructor
        public SearchCriteria(String searchPattern, boolean searchInKey, boolean searchInValue, int maxResults) {
            this(searchPattern, searchInKey, searchInValue, false, maxResults, false);
        }
        
        public String getSearchPattern() { return searchPattern; }
        public boolean isSearchInKey() { return searchInKey; }
        public boolean isSearchInValue() { return searchInValue; }
        public boolean isSearchInHeaders() { return searchInHeaders; }
        public int getMaxResults() { return maxResults; }
        public boolean isEnablePreview() { return enablePreview; }
    }
    
    /**
     * Helper class for managing authentication form fields
     */
    private static class AuthenticationFields {
        
        // SASL fields
        private final TextField usernameField = new TextField();
        private final PasswordField passwordField = new PasswordField();
        
        // SSL fields
        private final TextField keystoreLocationField = new TextField();
        private final PasswordField keystorePasswordField = new PasswordField();
        private final PasswordField keyPasswordField = new PasswordField();
        private final TextField truststoreLocationField = new TextField();
        private final PasswordField truststorePasswordField = new PasswordField();
        
        // Kerberos fields
        private final TextField kerberosServiceNameField = new TextField();
        private final TextField kerberosRealmField = new TextField();
        private final TextField kerberosKeytabField = new TextField();
        private final TextField kerberosPrincipalField = new TextField();
        
        // Additional properties
        private final TextArea additionalPropertiesField = new TextArea();
        
        public AuthenticationFields(AuthenticationConfig existingConfig) {
            setupFields();
            if (existingConfig != null) {
                loadExistingConfig(existingConfig);
            }
        }
        
        private void setupFields() {
            // Setup field properties and prompts
            usernameField.setPromptText("Username");
            passwordField.setPromptText("Password");
            
            keystoreLocationField.setPromptText("Keystore file path");
            keystorePasswordField.setPromptText("Keystore password");
            keyPasswordField.setPromptText("Key password (optional)");
            truststoreLocationField.setPromptText("Truststore file path");
            truststorePasswordField.setPromptText("Truststore password");
            
            kerberosServiceNameField.setPromptText("kafka");
            kerberosRealmField.setPromptText("EXAMPLE.COM");
            kerberosKeytabField.setPromptText("Keytab file path (optional)");
            kerberosPrincipalField.setPromptText("Principal name");
            
            additionalPropertiesField.setPromptText("Additional properties (key=value format, one per line)");
            additionalPropertiesField.setPrefRowCount(3);
            additionalPropertiesField.setWrapText(true);
        }
        
        private void loadExistingConfig(AuthenticationConfig config) {
            if (config.getUsername() != null) usernameField.setText(config.getUsername());
            if (config.getPassword() != null) passwordField.setText(config.getPassword());
            
            if (config.getKeystoreLocation() != null) keystoreLocationField.setText(config.getKeystoreLocation());
            if (config.getKeystorePassword() != null) keystorePasswordField.setText(config.getKeystorePassword());
            if (config.getKeyPassword() != null) keyPasswordField.setText(config.getKeyPassword());
            if (config.getTruststoreLocation() != null) truststoreLocationField.setText(config.getTruststoreLocation());
            if (config.getTruststorePassword() != null) truststorePasswordField.setText(config.getTruststorePassword());
            
            if (config.getKerberosServiceName() != null) kerberosServiceNameField.setText(config.getKerberosServiceName());
            if (config.getKerberosRealm() != null) kerberosRealmField.setText(config.getKerberosRealm());
            if (config.getKerberosKeytab() != null) kerberosKeytabField.setText(config.getKerberosKeytab());
            if (config.getKerberosPrincipal() != null) kerberosPrincipalField.setText(config.getKerberosPrincipal());
            
            if (config.getAdditionalProperties() != null) additionalPropertiesField.setText(config.getAdditionalProperties());
        }
        
        public VBox[] getFieldsForType(AuthenticationType type) {
            switch (type) {
                case SASL_PLAIN:
                case SASL_SCRAM_SHA_256:
                case SASL_SCRAM_SHA_512:
                    return new VBox[] { createSaslFields() };
                
                case SSL:
                    return new VBox[] { createSslFields() };
                
                case SASL_SSL:
                    return new VBox[] { createSaslFields(), createSslFields() };
                
                case KERBEROS:
                    return new VBox[] { createKerberosFields() };
                
                default:
                    return new VBox[0];
            }
        }
        
        private VBox createSaslFields() {
            VBox container = new VBox(5);
            container.getChildren().addAll(
                new Label("SASL Authentication:"),
                createFieldRow("Username:", usernameField),
                createFieldRow("Password:", passwordField)
            );
            return container;
        }
        
        private VBox createSslFields() {
            VBox container = new VBox(5);
            container.getChildren().addAll(
                new Label("SSL Configuration:"),
                createFieldRow("Truststore Location:", truststoreLocationField),
                createFieldRow("Truststore Password:", truststorePasswordField),
                createFieldRow("Keystore Location:", keystoreLocationField),
                createFieldRow("Keystore Password:", keystorePasswordField),
                createFieldRow("Key Password:", keyPasswordField)
            );
            return container;
        }
        
        private VBox createKerberosFields() {
            VBox container = new VBox(5);
            container.getChildren().addAll(
                new Label("Kerberos Configuration:"),
                createFieldRow("Service Name:", kerberosServiceNameField),
                createFieldRow("Realm:", kerberosRealmField),
                createFieldRow("Principal:", kerberosPrincipalField),
                createFieldRow("Keytab:", kerberosKeytabField)
            );
            return container;
        }
        
        private GridPane createFieldRow(String labelText, Control field) {
            GridPane row = new GridPane();
            row.setHgap(10);
            row.add(new Label(labelText), 0, 0);
            row.add(field, 1, 0);
            field.setPrefWidth(300);
            return row;
        }
        
        public AuthenticationConfig createAuthenticationConfig(AuthenticationType type) {
            AuthenticationConfig config = new AuthenticationConfig();
            
            // Set common fields
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (!username.isEmpty()) config.setUsername(username);
            if (!password.isEmpty()) config.setPassword(password);
            
            String keystoreLocation = keystoreLocationField.getText().trim();
            String keystorePassword = keystorePasswordField.getText();
            String keyPassword = keyPasswordField.getText();
            String truststoreLocation = truststoreLocationField.getText().trim();
            String truststorePassword = truststorePasswordField.getText();
            
            if (!keystoreLocation.isEmpty()) config.setKeystoreLocation(keystoreLocation);
            if (!keystorePassword.isEmpty()) config.setKeystorePassword(keystorePassword);
            if (!keyPassword.isEmpty()) config.setKeyPassword(keyPassword);
            if (!truststoreLocation.isEmpty()) config.setTruststoreLocation(truststoreLocation);
            if (!truststorePassword.isEmpty()) config.setTruststorePassword(truststorePassword);
            
            String serviceName = kerberosServiceNameField.getText().trim();
            String realm = kerberosRealmField.getText().trim();
            String keytab = kerberosKeytabField.getText().trim();
            String principal = kerberosPrincipalField.getText().trim();
            
            if (!serviceName.isEmpty()) config.setKerberosServiceName(serviceName);
            if (!realm.isEmpty()) config.setKerberosRealm(realm);
            if (!keytab.isEmpty()) config.setKerberosKeytab(keytab);
            if (!principal.isEmpty()) config.setKerberosPrincipal(principal);
            
            String additionalProps = additionalPropertiesField.getText().trim();
            if (!additionalProps.isEmpty()) config.setAdditionalProperties(additionalProps);
            
            return config;
        }
    }
    
    /**
     * Format schema content based on its type
     */
    private static String formatSchema(String schema, MessageFormat format) {
        if (schema == null || schema.trim().isEmpty()) {
            return schema;
        }
        
        try {
            if (format == MessageFormat.AVRO) {
                // Try to format as JSON for Avro schemas
                String formattedJson = JsonFormatter.formatJson(schema);
                return formattedJson != null ? formattedJson : schema;
            } else if (format == MessageFormat.PROTOBUF) {
                // Basic formatting for proto files - add proper indentation
                return formatProtoSchema(schema);
            }
            return schema;
        } catch (Exception e) {
            return schema; // Return original if formatting fails
        }
    }
    
    /**
     * Basic protobuf schema formatting
     */
    private static String formatProtoSchema(String protoSchema) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = protoSchema.split("\n");
        int indentLevel = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            // Decrease indent for closing braces
            if (trimmed.equals("}")) {
                indentLevel--;
            }
            
            // Add indentation
            formatted.append("  ".repeat(Math.max(0, indentLevel)));
            formatted.append(trimmed);
            formatted.append("\n");
            
            // Increase indent for opening braces
            if (trimmed.endsWith("{")) {
                indentLevel++;
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * Validate schema content based on its type
     */
    private static String validateSchema(String schema, MessageFormat format) {
        if (schema == null || schema.trim().isEmpty()) {
            return "✗ Schema is empty";
        }
        
        try {
            if (format == MessageFormat.AVRO) {
                // Basic JSON validation for Avro schemas
                if (isValidJson(schema)) {
                    // Additional Avro-specific checks
                    if (schema.contains("\"type\"") && (schema.contains("\"record\"") || 
                        schema.contains("\"string\"") || schema.contains("\"int\"") || 
                        schema.contains("\"long\"") || schema.contains("\"double\"") || 
                        schema.contains("\"boolean\"") || schema.contains("\"array\"") || 
                        schema.contains("\"map\""))) {
                        return "✓ Valid Avro schema structure";
                    } else {
                        return "⚠ Valid JSON but may not be a complete Avro schema";
                    }
                } else {
                    return "✗ Invalid JSON format for Avro schema";
                }
            } else if (format == MessageFormat.PROTOBUF) {
                // Basic protobuf validation
                if (schema.contains("syntax") && schema.contains("message") && schema.contains("{")) {
                    return "✓ Valid ProtoBuff schema structure";
                } else {
                    return "⚠ Schema may be incomplete - ensure it has syntax, message definitions";
                }
            }
            return "✓ Schema format appears valid";
        } catch (Exception e) {
            return "✗ Schema validation error: " + e.getMessage();
        }
    }
}