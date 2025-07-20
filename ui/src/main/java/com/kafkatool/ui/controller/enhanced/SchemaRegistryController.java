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
 * Controller for Schema Registry management
 */
public class SchemaRegistryController {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaRegistryController.class);
    
    @FXML private TextField schemaRegistryUrlField;
    @FXML private Button connectButton;
    @FXML private ListView<String> subjectsList;
    @FXML private ListView<Integer> versionsList;
    @FXML private TextArea schemaContent;
    @FXML private ComboBox<String> compatibilityCombo;
    @FXML private Button registerSchemaButton;
    @FXML private Button deleteSubjectButton;
    @FXML private VBox schemaDetailsPane;
    @FXML private Label statusLabel;
    
    private SchemaRegistryService schemaRegistryService;
    private ObservableList<String> subjects = FXCollections.observableArrayList();
    private ObservableList<Integer> versions = FXCollections.observableArrayList();
    private String currentSubject;
    
    public void initialize() {
        setupUI();
        setupEventHandlers();
    }
    
    private void setupUI() {
        subjectsList.setItems(subjects);
        versionsList.setItems(versions);
        
        compatibilityCombo.setItems(FXCollections.observableArrayList(
            "BACKWARD", "BACKWARD_TRANSITIVE", "FORWARD", "FORWARD_TRANSITIVE", 
            "FULL", "FULL_TRANSITIVE", "NONE"
        ));
        
        statusLabel.setText("Not connected");
        schemaDetailsPane.setDisable(true);
    }
    
    private void setupEventHandlers() {
        connectButton.setOnAction(e -> connectToRegistry());
        subjectsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                loadSubjectVersions();
            }
        });
        versionsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                loadSchemaContent();
            }
        });
        registerSchemaButton.setOnAction(e -> registerNewSchema());
        deleteSubjectButton.setOnAction(e -> deleteSelectedSubject());
    }
    
    private void connectToRegistry() {
        String url = schemaRegistryUrlField.getText().trim();
        if (url.isEmpty()) {
            showError("Please enter Schema Registry URL");
            return;
        }
        
        statusLabel.setText("Connecting");
        connectButton.setDisable(true);
        
        schemaRegistryService.testConnectionAsync(url)
            .thenAccept(connected -> {
                if (connected) {
                    statusLabel.setText("Connected");
                    schemaDetailsPane.setDisable(false);
                    loadSubjects();
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
    
    private void loadSubjects() {
        String url = schemaRegistryUrlField.getText().trim();
        
        schemaRegistryService.getSubjectsAsync(url)
            .thenAccept(subjectList -> {
                subjects.clear();
                subjects.addAll(subjectList);
            })
            .exceptionally(throwable -> {
                showError("Failed to load subjects: " + throwable.getMessage());
                return null;
            });
    }
    
    private void loadSubjectVersions() {
        String selectedSubject = subjectsList.getSelectionModel().getSelectedItem();
        if (selectedSubject == null) return;
        
        currentSubject = selectedSubject;
        String url = schemaRegistryUrlField.getText().trim();
        
        schemaRegistryService.getSubjectVersionsAsync(url, selectedSubject)
            .thenAccept(versionList -> {
                versions.clear();
                versions.addAll(versionList);
            })
            .exceptionally(throwable -> {
                showError("Failed to load versions: " + throwable.getMessage());
                return null;
            });
    }
    
    private void loadSchemaContent() {
        Integer selectedVersion = versionsList.getSelectionModel().getSelectedItem();
        if (selectedVersion == null || currentSubject == null) return;
        
        String url = schemaRegistryUrlField.getText().trim();
        
        schemaRegistryService.getSchemaAsync(url, currentSubject, selectedVersion)
            .thenAccept(schemaInfo -> {
                schemaContent.setText(schemaInfo.getSchema());
                compatibilityCombo.setValue(schemaInfo.getCompatibility());
            })
            .exceptionally(throwable -> {
                showError("Failed to load schema: " + throwable.getMessage());
                return null;
            });
    }
    
    private void registerNewSchema() {
        // TODO: Implement schema registration dialog
        showInfo("Schema registration dialog not implemented yet");
    }
    
    private void deleteSelectedSubject() {
        String selectedSubject = subjectsList.getSelectionModel().getSelectedItem();
        if (selectedSubject == null) {
            showError("Please select a subject to delete");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Subject");
        confirmDialog.setContentText("Are you sure you want to delete subject '" + selectedSubject + "'? This will delete all versions.");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String url = schemaRegistryUrlField.getText().trim();
                
                schemaRegistryService.deleteSubjectAsync(url, selectedSubject)
                    .thenAccept(deletedVersions -> {
                        showInfo("Deleted " + deletedVersions.size() + " versions of subject '" + selectedSubject + "'");
                        loadSubjects();
                    })
                    .exceptionally(throwable -> {
                        showError("Failed to delete subject: " + throwable.getMessage());
                        return null;
                    });
            }
        });
    }
    
    public void setSchemaRegistryService(SchemaRegistryService service) {
        this.schemaRegistryService = service;
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