package com.kafkatool.ui;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.TopicInfo;
import com.kafkatool.util.KafkaAuthenticationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
        
        // Add all components to main container
        mainContainer.getChildren().addAll(
            new Label("Connection Details:"),
            basicGrid,
            new Separator(),
            authLabel,
            authTypeCombo,
            authDetailsContainer
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
                    
                    // Validate authentication configuration
                    if (!KafkaAuthenticationUtil.validateAuthenticationConfig(authType, authConfig)) {
                        showErrorDialog("Validation Error", "Invalid Authentication Configuration", 
                            "Please fill in all required authentication fields for " + authType.getDisplayName());
                        return null;
                    }
                }
                
                ClusterInfo cluster = new ClusterInfo(name, brokers, connectByDefaultBox.isSelected(), authType, authConfig);
                
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
     * Show search messages dialog
     */
    public static Optional<SearchCriteria> showSearchMessagesDialog() {
        Dialog<SearchCriteria> dialog = new Dialog<>();
        dialog.setTitle("Search Messages");
        dialog.setHeaderText("Enter search criteria");
        
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
        searchValueBox.setSelected(true);
        Spinner<Integer> maxResultsSpinner = new Spinner<>(1, 10000, 100);
        
        grid.add(new Label("Pattern:"), 0, 0);
        grid.add(patternField, 1, 0);
        grid.add(searchKeyBox, 1, 1);
        grid.add(searchValueBox, 1, 2);
        grid.add(new Label("Max Results:"), 0, 3);
        grid.add(maxResultsSpinner, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        Platform.runLater(patternField::requestFocus);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == searchButtonType) {
                String pattern = patternField.getText().trim();
                if (!pattern.isEmpty()) {
                    return new SearchCriteria(pattern, searchKeyBox.isSelected(), 
                        searchValueBox.isSelected(), maxResultsSpinner.getValue());
                }
            }
            return null;
        });
        
        return dialog.showAndWait();
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
        
        public MessageData(String key, String value, Map<String, String> headers, int partition) {
            this.key = key;
            this.value = value;
            this.headers = headers;
            this.partition = partition;
        }
        
        public String getKey() { return key; }
        public String getValue() { return value; }
        public Map<String, String> getHeaders() { return headers; }
        public int getPartition() { return partition; }
    }
    
    public static class SearchCriteria {
        private final String searchPattern;
        private final boolean searchInKey;
        private final boolean searchInValue;
        private final int maxResults;
        
        public SearchCriteria(String searchPattern, boolean searchInKey, boolean searchInValue, int maxResults) {
            this.searchPattern = searchPattern;
            this.searchInKey = searchInKey;
            this.searchInValue = searchInValue;
            this.maxResults = maxResults;
        }
        
        public String getSearchPattern() { return searchPattern; }
        public boolean isSearchInKey() { return searchInKey; }
        public boolean isSearchInValue() { return searchInValue; }
        public int getMaxResults() { return maxResults; }
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
}