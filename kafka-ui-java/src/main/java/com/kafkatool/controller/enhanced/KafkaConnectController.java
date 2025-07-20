package com.kafkatool.controller.enhanced;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.kafkatool.model.*;
import com.kafkatool.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        statusLabel.setText("Connecting...");
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
        // TODO: Implement connector creation dialog
        showInfo("Connector creation dialog not implemented yet");
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