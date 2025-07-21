package com.kafkatool.controller.enhanced;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.kafkatool.model.*;
import com.kafkatool.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Kafka Connect management
 */
public class KafkaConnectController {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaConnectController.class);
    
    @FXML private TextField connectUrlField;
    @FXML private Button connectButton;
    @FXML private TableView<ConnectorInfo> connectorsTable;
    @FXML private TableColumn<ConnectorInfo, String> nameColumn;
    @FXML private TableColumn<ConnectorInfo, String> statusColumn;
    @FXML private TableColumn<ConnectorInfo, String> typeColumn;
    @FXML private TableColumn<ConnectorInfo, String> classColumn;
    @FXML private TableColumn<ConnectorInfo, Integer> tasksColumn;
    @FXML private TextArea connectorConfigArea;
    @FXML private Button createConnectorButton;
    @FXML private Button deleteConnectorButton;
    @FXML private Button pauseResumeButton;
    @FXML private Button restartButton;
    @FXML private VBox connectorDetailsPane;
    @FXML private Label statusLabel;
    @FXML private Label clusterInfoLabel;
    
    private KafkaConnectService connectService = new KafkaConnectServiceImpl();
    private ObservableList<ConnectorInfo> connectors = FXCollections.observableArrayList();
    private ConnectorInfo selectedConnector;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void initialize() {
        setupUI();
        setupEventHandlers();
    }
    
    private void setupUI() {
        // Setup table columns
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        classColumn.setCellValueFactory(cellData -> cellData.getValue().classNameProperty());
        tasksColumn.setCellValueFactory(cellData -> cellData.getValue().tasksRunningProperty().asObject());
        
        connectorsTable.setItems(connectors);
        
        statusLabel.setText("Not connected");
        connectorDetailsPane.setDisable(true);
    }
    
    private void setupEventHandlers() {
        connectButton.setOnAction(e -> connectToCluster());
        connectorsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedConnector = newSelection;
                if (newSelection != null) {
                    loadConnectorDetails();
                    updateButtonStates();
                }
            }
        );
        createConnectorButton.setOnAction(e -> createNewConnector());
        deleteConnectorButton.setOnAction(e -> deleteSelectedConnector());
        pauseResumeButton.setOnAction(e -> pauseResumeConnector());
        restartButton.setOnAction(e -> restartConnector());
    }
    
    private void connectToCluster() {
        String url = connectUrlField.getText().trim();
        if (url.isEmpty()) {
            showError("Please enter Kafka Connect URL");
            return;
        }
        
        statusLabel.setText("Connecting!");
        connectButton.setDisable(true);
        
        connectService.testConnectionAsync(url)
            .thenAccept(connected -> {
                if (connected) {
                    statusLabel.setText("Connected");
                    connectorDetailsPane.setDisable(false);
                    loadClusterInfo();
                    loadConnectors();
                } else {
                    statusLabel.setText("Connection failed");
                }
            })
            .exceptionally(throwable -> {
                statusLabel.setText("Connection error: " + throwable.getMessage());
                return null;
            })
            .whenComplete((result, throwable) -> {
                connectButton.setDisable(false);
            });
    }
    
    private void loadClusterInfo() {
        String url = connectUrlField.getText().trim();
        
        connectService.getClusterInfoAsync(url)
            .thenAccept(clusterInfo -> {
                String version = (String) clusterInfo.get("version");
                String commit = (String) clusterInfo.get("commit");
                clusterInfoLabel.setText(String.format("Kafka Connect %s (commit: %s)", version, commit));
            })
            .exceptionally(throwable -> {
                clusterInfoLabel.setText("Unable to load cluster info");
                return null;
            });
    }
    
    private void loadConnectors() {
        String url = connectUrlField.getText().trim();
        
        connectService.getConnectorsAsync(url)
            .thenAccept(connectorList -> {
                connectors.clear();
                connectors.addAll(connectorList);
            })
            .exceptionally(throwable -> {
                showError("Failed to load connectors: " + throwable.getMessage());
                return null;
            });
    }
    
    private void loadConnectorDetails() {
        if (selectedConnector == null) return;
        
        String url = connectUrlField.getText().trim();
        String connectorName = selectedConnector.getName();
        
        connectService.getConnectorConfigAsync(url, connectorName)
            .thenAccept(config -> {
                StringBuilder configText = new StringBuilder();
                config.forEach((key, value) -> 
                    configText.append(key).append("=").append(value).append("\n"));
                connectorConfigArea.setText(configText.toString());
            })
            .exceptionally(throwable -> {
                connectorConfigArea.setText("Failed to load configuration: " + throwable.getMessage());
                return null;
            });
    }
    
    private void updateButtonStates() {
        boolean hasSelection = selectedConnector != null;
        deleteConnectorButton.setDisable(!hasSelection);
        pauseResumeButton.setDisable(!hasSelection);
        restartButton.setDisable(!hasSelection);
        
        if (hasSelection) {
            String status = selectedConnector.getStatus();
            if ("RUNNING".equals(status)) {
                pauseResumeButton.setText("Pause");
            } else if ("PAUSED".equals(status)) {
                pauseResumeButton.setText("Resume");
            } else {
                pauseResumeButton.setText("Pause/Resume");
            }
        }
    }
    
    private void createNewConnector() {
        // Create connector configuration dialog
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create New Connector");
        dialog.setHeaderText("Configure New Kafka Connector");
        
        // Set button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("connector-name");
        TextField classField = new TextField();
        classField.setPromptText("org.apache.kafka.connect.file.FileStreamSourceConnector");
        TextField tasksMaxField = new TextField("1");
        TextArea configArea = new TextArea();
        configArea.setPromptText("Additional configuration (JSON format):\n{\n  \"file\": \"/tmp/source.txt\",\n  \"topic\": \"my-topic\"\n}");
        configArea.setPrefRowCount(8);
        
        grid.add(new Label("Connector Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Connector Class:"), 0, 1);
        grid.add(classField, 1, 1);
        grid.add(new Label("Max Tasks:"), 0, 2);
        grid.add(tasksMaxField, 1, 2);
        grid.add(new Label("Configuration:"), 0, 3);
        grid.add(configArea, 1, 3);
        
        // Enable/disable create button
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);
        
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty() || classField.getText().trim().isEmpty());
        });
        classField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty() || nameField.getText().trim().isEmpty());
        });
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert result when Create button is pressed
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Map<String, String> config = new HashMap<>();
                config.put("name", nameField.getText().trim());
                config.put("connector.class", classField.getText().trim());
                config.put("tasks.max", tasksMaxField.getText().trim());
                
                // Parse additional configuration
                String additionalConfig = configArea.getText().trim();
                if (!additionalConfig.isEmpty()) {
                    try {
                        JsonNode configNode = objectMapper.readTree(additionalConfig);
                        configNode.fields().forEachRemaining(entry -> {
                            config.put(entry.getKey(), entry.getValue().asText());
                        });
                    } catch (Exception e) {
                        showError("Invalid JSON configuration: " + e.getMessage());
                        return null;
                    }
                }
                
                return config;
            }
            return null;
        });
        
        // Show dialog and handle result
        dialog.showAndWait().ifPresent(config -> {
            String url = connectUrlField.getText().trim();
            String connectorName = config.get("name");
            config.remove("name"); // Remove name from config as it's passed separately
            
            statusLabel.setText("Creating connector");
            
            connectService.createOrUpdateConnectorAsync(url, connectorName, config)
                .thenAccept(connectorInfo -> {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Connected");
                        showInfo("Connector '" + connectorName + "' created successfully");
                        loadConnectors();
                    });
                })
                .exceptionally(throwable -> {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Connected");
                        showError("Failed to create connector: " + throwable.getMessage());
                    });
                    return null;
                });
        });
    }
    
    private void deleteSelectedConnector() {
        if (selectedConnector == null) {
            showError("Please select a connector to delete");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Connector");
        confirmDialog.setContentText("Are you sure you want to delete connector '" + selectedConnector.getName() + "'?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String url = connectUrlField.getText().trim();
                String connectorName = selectedConnector.getName();
                
                connectService.deleteConnectorAsync(url, connectorName)
                    .thenRun(() -> {
                        showInfo("Connector '" + connectorName + "' deleted successfully");
                        loadConnectors();
                    })
                    .exceptionally(throwable -> {
                        showError("Failed to delete connector: " + throwable.getMessage());
                        return null;
                    });
            }
        });
    }
    
    private void pauseResumeConnector() {
        if (selectedConnector == null) return;
        
        String url = connectUrlField.getText().trim();
        String connectorName = selectedConnector.getName();
        String status = selectedConnector.getStatus();
        
        if ("RUNNING".equals(status)) {
            connectService.pauseConnectorAsync(url, connectorName)
                .thenRun(() -> {
                    showInfo("Connector '" + connectorName + "' paused successfully");
                    loadConnectors();
                })
                .exceptionally(throwable -> {
                    showError("Failed to pause connector: " + throwable.getMessage());
                    return null;
                });
        } else if ("PAUSED".equals(status)) {
            connectService.resumeConnectorAsync(url, connectorName)
                .thenRun(() -> {
                    showInfo("Connector '" + connectorName + "' resumed successfully");
                    loadConnectors();
                })
                .exceptionally(throwable -> {
                    showError("Failed to resume connector: " + throwable.getMessage());
                    return null;
                });
        }
    }
    
    private void restartConnector() {
        if (selectedConnector == null) return;
        
        String url = connectUrlField.getText().trim();
        String connectorName = selectedConnector.getName();
        
        connectService.restartConnectorAsync(url, connectorName)
            .thenRun(() -> {
                showInfo("Connector '" + connectorName + "' restarted successfully");
                loadConnectors();
            })
            .exceptionally(throwable -> {
                showError("Failed to restart connector: " + throwable.getMessage());
                return null;
            });
    }
    
    public void setKafkaConnectService(KafkaConnectService service) {
        this.connectService = service;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}