package com.kafkatool.ui;

import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.TopicInfo;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

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
        Dialog<ClusterInfo> dialog = new Dialog<>();
        dialog.setTitle("Add Kafka Cluster");
        dialog.setHeaderText("Enter cluster connection details");
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Cluster Name");
        TextField brokersField = new TextField();
        brokersField.setPromptText("localhost:9092");
        CheckBox connectByDefaultBox = new CheckBox("Connect by default");
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Broker URLs:"), 0, 1);
        grid.add(brokersField, 1, 1);
        grid.add(connectByDefaultBox, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        Platform.runLater(nameField::requestFocus);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String name = nameField.getText().trim();
                String brokers = brokersField.getText().trim();
                if (!name.isEmpty() && !brokers.isEmpty()) {
                    return new ClusterInfo(name, brokers, connectByDefaultBox.isSelected());
                }
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Show dialog to edit an existing cluster
     */
    public static Optional<ClusterInfo> showEditClusterDialog(ClusterInfo cluster) {
        Dialog<ClusterInfo> dialog = new Dialog<>();
        dialog.setTitle("Edit Kafka Cluster");
        dialog.setHeaderText("Update cluster connection details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField(cluster.getName());
        TextField brokersField = new TextField(cluster.getBrokerUrls());
        CheckBox connectByDefaultBox = new CheckBox("Connect by default");
        connectByDefaultBox.setSelected(cluster.isConnectByDefault());
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Broker URLs:"), 0, 1);
        grid.add(brokersField, 1, 1);
        grid.add(connectByDefaultBox, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String brokers = brokersField.getText().trim();
                if (!name.isEmpty() && !brokers.isEmpty()) {
                    ClusterInfo updated = new ClusterInfo(name, brokers, connectByDefaultBox.isSelected());
                    updated.setStatus(cluster.getStatus());
                    updated.setKafkaVersion(cluster.getKafkaVersion());
                    return updated;
                }
            }
            return null;
        });
        
        return dialog.showAndWait();
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
}