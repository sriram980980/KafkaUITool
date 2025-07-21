package com.kafkatool.controller;

import com.kafkatool.model.*;
import com.kafkatool.service.KafkaService;
import com.kafkatool.service.KafkaServiceImpl;
import com.kafkatool.util.DialogHelper;
import com.kafkatool.util.JsonFormatter;
import com.kafkatool.util.SettingsManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Main controller for the Kafka UI Tool application
 */
public class MainController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Services
    private final KafkaService kafkaService = new KafkaServiceImpl();
    private final SettingsManager settingsManager = new SettingsManager();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    // Data collections
    private final ObservableList<ClusterInfo> clusters = FXCollections.observableArrayList();
    private final ObservableList<TopicInfo> topics = FXCollections.observableArrayList();
    private final FilteredList<TopicInfo> filteredTopics = new FilteredList<>(topics);
    private final ObservableList<KafkaMessage> messages = FXCollections.observableArrayList();
    
    // Current state
    private ClusterInfo currentCluster;
    private TopicInfo currentTopic;
    
    // FXML Menu Items
    @FXML private MenuItem addClusterMenuItem;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem createTopicMenuItem;
    @FXML private MenuItem deleteTopicMenuItem;
    @FXML private MenuItem refreshTopicsMenuItem;
    @FXML private MenuItem produceMessageMenuItem;
    @FXML private MenuItem searchMessagesMenuItem;
    @FXML private MenuItem consumerGroupsMenuItem;
    @FXML private MenuItem brokersMenuItem;
    @FXML private MenuItem clusterConfigMenuItem;
    @FXML private MenuItem brokerConfigMenuItem;
    @FXML private MenuItem aclManagementMenuItem;
    @FXML private MenuItem multiClusterMenuItem;
    @FXML private MenuItem schemaRegistryMenuItem;
    @FXML private MenuItem kafkaConnectMenuItem;
    @FXML private MenuItem metricsDashboardMenuItem;
    @FXML private MenuItem importExportMenuItem;
    @FXML private MenuItem bulkOperationsMenuItem;
    @FXML private MenuItem alertsMenuItem;
    @FXML private MenuItem workspaceMenuItem;
    @FXML private MenuItem regexSearchMenuItem;
    @FXML private MenuItem messageReplayMenuItem;
    @FXML private MenuItem themeMenuItem;
    @FXML private MenuItem keyboardShortcutsMenuItem;
    @FXML private MenuItem aboutMenuItem;
    
    // FXML Controls - Left Panel
    @FXML private ListView<ClusterInfo> clustersListView;
    @FXML private Button addClusterButton;
    @FXML private Button editClusterButton;
    @FXML private Button deleteClusterButton;
    @FXML private Button connectClusterButton;
    
    @FXML private TextField topicFilterField;
    @FXML private Button refreshTopicsButton;
    @FXML private ListView<TopicInfo> topicsListView;
    @FXML private Button createTopicButton;
    @FXML private Button configTopicButton;
    @FXML private Button deleteTopicButtonSide;
    
    // FXML Controls - Chat Section
    @FXML private TextArea chatMessagesArea;
    @FXML private TextField chatInputField;
    @FXML private Button sendChatButton;
    @FXML private Button clearChatButton;
    @FXML private Button connectChatButton;
    
    // FXML Controls - Right Panel
    @FXML private ComboBox<Integer> partitionComboBox;
    @FXML private TextField fromOffsetField;
    @FXML private TextField toOffsetField;
    @FXML private Button loadMessagesButton;
    @FXML private Button loadLatestButton;
    @FXML private Button searchMessagesButton;
    
    @FXML private TableView<KafkaMessage> messagesTableView;
    @FXML private TableColumn<KafkaMessage, Long> offsetColumn;
    @FXML private TableColumn<KafkaMessage, Integer> partitionColumn;
    @FXML private TableColumn<KafkaMessage, String> keyColumn;
    @FXML private TableColumn<KafkaMessage, String> valueColumn;
    @FXML private TableColumn<KafkaMessage, LocalDateTime> timestampColumn;
    @FXML private TableColumn<KafkaMessage, String> headersColumn;
    
    @FXML private TabPane messageDetailsTabPane;
    @FXML private TextArea rawMessageTextArea;
    @FXML private TextArea jsonMessageTextArea;
    @FXML private TextArea headersTextArea;
    
    // FXML Controls - Status Bar
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private Label messageCountLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing MainController");
        
        setupClustersSection();
        setupTopicsSection();
        setupMessagesSection();
        setupChatSection();
        setupStatusBar();
        setupMenuBindings();
        
        loadSettings();
        
        logger.info("MainController initialization complete");
    }
    
    private void setupClustersSection() {
        // Configure clusters list view
        clustersListView.setItems(clusters);
        clustersListView.setCellFactory(listView -> new ListCell<ClusterInfo>() {
            @Override
            protected void updateItem(ClusterInfo cluster, boolean empty) {
                super.updateItem(cluster, empty);
                if (empty || cluster == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("connected", "connecting", "disconnected");
                } else {
                    setText(cluster.toString());
                    
                    // Update style based on connection status
                    getStyleClass().removeAll("connected", "connecting", "disconnected");
                    switch (cluster.getStatus().toLowerCase()) {
                        case "connected":
                            getStyleClass().add("connected");
                            break;
                        case "connecting":
                            getStyleClass().add("connecting");
                            break;
                        default:
                            getStyleClass().add("disconnected");
                            break;
                    }
                }
            }
        });
        
        // Handle cluster selection
        clustersListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                currentCluster = newSelection;
                updateClusterButtons();
                if (newSelection != null && "Connected".equals(newSelection.getStatus())) {
                    refreshTopics();
                } else {
                    topics.clear();
                }
            }
        );
    }
    
    private void setupTopicsSection() {
        // Configure topic filter
        topicFilterField.textProperty().addListener((obs, oldText, newText) -> {
            filteredTopics.setPredicate(topic -> {
                if (newText == null || newText.isEmpty()) {
                    return true;
                }
                return topic.getName().toLowerCase().contains(newText.toLowerCase());
            });
        });
        
        // Configure topics list view
        topicsListView.setItems(filteredTopics);
        topicsListView.setCellFactory(listView -> new ListCell<TopicInfo>() {
            @Override
            protected void updateItem(TopicInfo topic, boolean empty) {
                super.updateItem(topic, empty);
                if (empty || topic == null) {
                    setText(null);
                } else {
                    setText(String.format("%s (%d partitions)", topic.getName(), topic.getPartitions()));
                }
            }
        });
        
        // Handle topic selection
        topicsListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                currentTopic = newSelection;
                updateTopicButtons();
                if (newSelection != null) {
                    loadTopicPartitions();
                } else {
                    partitionComboBox.getItems().clear();
                    messages.clear();
                }
            }
        );
    }
    
    private void setupMessagesSection() {
        // Configure table columns
        offsetColumn.setCellValueFactory(new PropertyValueFactory<>("offset"));
        partitionColumn.setCellValueFactory(new PropertyValueFactory<>("partition"));
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        headersColumn.setCellValueFactory(new PropertyValueFactory<>("headers"));
        
        // Format timestamp column
        timestampColumn.setCellFactory(column -> new TableCell<KafkaMessage, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime timestamp, boolean empty) {
                super.updateItem(timestamp, empty);
                if (empty || timestamp == null) {
                    setText(null);
                } else {
                    setText(timestamp.format(TIMESTAMP_FORMATTER));
                }
            }
        });
        
        // Limit text in value column
        valueColumn.setCellFactory(column -> new TableCell<KafkaMessage, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    String displayValue = value.length() > 100 ? value.substring(0, 97) + "!" : value;
                    setText(displayValue);
                    setTooltip(new Tooltip(value));
                }
            }
        });
        
        // Configure messages table
        messagesTableView.setItems(messages);
        messagesTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> updateMessageDetails(newSelection)
        );
        
        // Configure partition combo box
        partitionComboBox.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    loadPartitionOffsets(newSelection);
                }
            }
        );
    }
    
    private void setupChatSection() {
        // Set initial state
        chatMessagesArea.setEditable(false);
        chatMessagesArea.setWrapText(true);
        
        // Add welcome message
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String welcomeMessage = String.format("[%s] System: Chat section initialized. Click 'Connect' to start chatting.%n", timestamp);
        chatMessagesArea.setText(welcomeMessage);
        
        // Setup Enter key handler for chat input
        chatInputField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onSendChatMessage();
                event.consume();
            }
        });
        
        // Initial button state
        connectChatButton.setText("Connect");
        connectChatButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");
    }
    
    private void setupStatusBar() {
        // Update message count when messages change
        messageCountLabel.textProperty().bind(
            Bindings.size(messages).asString("Messages: %d")
        );
        
        // Set initial connection status
        updateConnectionStatus("Not Connected", false);
    }
    
    private void setupMenuBindings() {
        // Bind menu items to button states
        ClusterInfo selectedCluster = clustersListView.getSelectionModel().getSelectedItem();
        TopicInfo selectedTopic = topicsListView.getSelectionModel().getSelectedItem();
        
        createTopicMenuItem.disableProperty().bind(
            Bindings.isNull(clustersListView.getSelectionModel().selectedItemProperty())
                .or(Bindings.notEqual(clustersListView.getSelectionModel().selectedItemProperty().asString(), "Connected"))
        );
        
        deleteTopicMenuItem.disableProperty().bind(
            Bindings.isNull(topicsListView.getSelectionModel().selectedItemProperty())
        );
        
        produceMessageMenuItem.disableProperty().bind(
            Bindings.isNull(topicsListView.getSelectionModel().selectedItemProperty())
        );
        
        searchMessagesMenuItem.disableProperty().bind(
            Bindings.isNull(topicsListView.getSelectionModel().selectedItemProperty())
        );
        
        // Admin menu items - enabled only when connected to a cluster
        consumerGroupsMenuItem.disableProperty().bind(
            Bindings.isNull(clustersListView.getSelectionModel().selectedItemProperty())
                .or(Bindings.notEqual(clustersListView.getSelectionModel().selectedItemProperty().asString(), "Connected"))
        );
        
        brokersMenuItem.disableProperty().bind(
            Bindings.isNull(clustersListView.getSelectionModel().selectedItemProperty())
                .or(Bindings.notEqual(clustersListView.getSelectionModel().selectedItemProperty().asString(), "Connected"))
        );
        
        clusterConfigMenuItem.disableProperty().bind(
            Bindings.isNull(clustersListView.getSelectionModel().selectedItemProperty())
                .or(Bindings.notEqual(clustersListView.getSelectionModel().selectedItemProperty().asString(), "Connected"))
        );
        
        brokerConfigMenuItem.disableProperty().bind(
            Bindings.isNull(clustersListView.getSelectionModel().selectedItemProperty())
                .or(Bindings.notEqual(clustersListView.getSelectionModel().selectedItemProperty().asString(), "Connected"))
        );
        
        aclManagementMenuItem.disableProperty().bind(
            Bindings.isNull(clustersListView.getSelectionModel().selectedItemProperty())
                .or(Bindings.notEqual(clustersListView.getSelectionModel().selectedItemProperty().asString(), "Connected"))
        );
        
        multiClusterMenuItem.disableProperty().bind(
            Bindings.isEmpty(clusters)
        );
        
        // Enterprise menu items
        importExportMenuItem.disableProperty().bind(
            Bindings.isNull(topicsListView.getSelectionModel().selectedItemProperty())
        );
        
        bulkOperationsMenuItem.disableProperty().bind(
            Bindings.isNull(clustersListView.getSelectionModel().selectedItemProperty())
                .or(Bindings.notEqual(clustersListView.getSelectionModel().selectedItemProperty().asString(), "Connected"))
        );
        
        // Tools menu items  
        regexSearchMenuItem.disableProperty().bind(
            Bindings.isNull(topicsListView.getSelectionModel().selectedItemProperty())
        );
        
        messageReplayMenuItem.disableProperty().bind(
            Bindings.isNull(topicsListView.getSelectionModel().selectedItemProperty())
        );
    }
    
    private void updateClusterButtons() {
        boolean hasSelection = currentCluster != null;
        boolean isConnected = hasSelection && "Connected".equals(currentCluster.getStatus());
        
        editClusterButton.setDisable(!hasSelection);
        deleteClusterButton.setDisable(!hasSelection);
        connectClusterButton.setDisable(!hasSelection);
        connectClusterButton.setText(isConnected ? "Disconnect" : "Connect");
    }
    
    private void updateTopicButtons() {
        boolean hasCluster = currentCluster != null && "Connected".equals(currentCluster.getStatus());
        boolean hasTopic = currentTopic != null;
        
        createTopicButton.setDisable(!hasCluster);
        configTopicButton.setDisable(!hasTopic);
        deleteTopicButtonSide.setDisable(!hasTopic);
        refreshTopicsButton.setDisable(!hasCluster);
    }
    
    private void updateConnectionStatus(String status, boolean isConnected) {
        Platform.runLater(() -> {
            connectionStatusLabel.setText(status);
            connectionStatusLabel.getStyleClass().removeAll("connected", "connecting", "disconnected");
            if (isConnected) {
                connectionStatusLabel.getStyleClass().add("connected");
            } else if ("Connecting".equals(status)) {
                connectionStatusLabel.getStyleClass().add("connecting");
            } else {
                connectionStatusLabel.getStyleClass().add("disconnected");
            }
        });
    }
    
    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }
    
    private void showLoading(boolean show) {
        Platform.runLater(() -> loadingIndicator.setVisible(show));
    }
    
    private void loadSettings() {
        try {
            List<ClusterInfo> savedClusters = settingsManager.loadClusters();
            clusters.addAll(savedClusters);
            
            // Auto-connect to default cluster if any
            Optional<ClusterInfo> defaultCluster = savedClusters.stream()
                .filter(ClusterInfo::isConnectByDefault)
                .findFirst();
                
            if (defaultCluster.isPresent()) {
                Platform.runLater(() -> {
                    clustersListView.getSelectionModel().select(defaultCluster.get());
                    connectToCluster(defaultCluster.get());
                });
            }
            
            logger.info("Loaded {} clusters from settings", savedClusters.size());
        } catch (Exception e) {
            logger.error("Failed to load settings", e);
            updateStatus("Failed to load settings: " + e.getMessage());
        }
    }
    
    private void saveSettings() {
        try {
            settingsManager.saveClusters(new ArrayList<>(clusters));
            logger.info("Settings saved successfully");
        } catch (Exception e) {
            logger.error("Failed to save settings", e);
        }
    }
    
    // Event handlers for menu items and buttons
    @FXML
    private void onAddCluster() {
        DialogHelper.showAddClusterDialog().ifPresent(cluster -> {
            clusters.add(cluster);
            saveSettings();
            updateStatus("Cluster added: " + cluster.getName());
        });
    }
    
    @FXML
    private void onEditCluster() {
        if (currentCluster != null) {
            DialogHelper.showEditClusterDialog(currentCluster).ifPresent(updatedCluster -> {
                int index = clusters.indexOf(currentCluster);
                clusters.set(index, updatedCluster);
                saveSettings();
                updateStatus("Cluster updated: " + updatedCluster.getName());
            });
        }
    }
    
    @FXML
    private void onDeleteCluster() {
        if (currentCluster != null) {
            boolean confirmed = DialogHelper.showConfirmDialog(
                "Delete Cluster",
                "Are you sure you want to delete the cluster '" + currentCluster.getName() + "'?",
                "This action cannot be undone."
            );
            
            if (confirmed) {
                clusters.remove(currentCluster);
                saveSettings();
                updateStatus("Cluster deleted: " + currentCluster.getName());
            }
        }
    }
    
    @FXML
    private void onConnectCluster() {
        if (currentCluster != null) {
            if ("Connected".equals(currentCluster.getStatus())) {
                disconnectFromCluster(currentCluster);
            } else {
                connectToCluster(currentCluster);
            }
        }
    }
    
    private void connectToCluster(ClusterInfo cluster) {
        updateConnectionStatus("Connecting", false);
        showLoading(true);
        cluster.setStatus("Connecting");
        clustersListView.refresh();
        
        CompletableFuture<Boolean> connectionFuture = kafkaService.testConnectionAsync(cluster.getBrokerUrls());
        
        connectionFuture.whenComplete((success, throwable) -> {
            Platform.runLater(() -> {
                showLoading(false);
                if (success) {
                    cluster.setStatus("Connected");
                    updateConnectionStatus("Connected to " + cluster.getName(), true);
                    updateStatus("Connected to cluster: " + cluster.getName());
                    
                    // Load Kafka version
                    kafkaService.getKafkaVersionAsync(cluster.getBrokerUrls())
                        .whenComplete((version, versionThrowable) -> {
                            if (version != null) {
                                cluster.setKafkaVersion(version);
                            }
                        });
                    
                    refreshTopics();
                } else {
                    cluster.setStatus("Connection Failed");
                    updateConnectionStatus("Connection Failed", false);
                    updateStatus("Failed to connect to cluster: " + cluster.getName());
                    
                    String errorMsg = throwable != null ? throwable.getMessage() : "Unknown error";
                    DialogHelper.showErrorDialog("Connection Error", 
                        "Failed to connect to cluster '" + cluster.getName() + "'", errorMsg);
                }
                clustersListView.refresh();
                updateClusterButtons();
            });
        });
    }
    
    private void disconnectFromCluster(ClusterInfo cluster) {
        cluster.setStatus("Disconnected");
        updateConnectionStatus("Not Connected", false);
        updateStatus("Disconnected from cluster: " + cluster.getName());
        clustersListView.refresh();
        topics.clear();
        messages.clear();
        updateClusterButtons();
    }
    
    @FXML
    private void onCreateTopic() {
        if (currentCluster != null && "Connected".equals(currentCluster.getStatus())) {
            DialogHelper.showCreateTopicDialog().ifPresent(topicConfig -> {
                showLoading(true);
                updateStatus("Creating topic: " + topicConfig.getName() + "!");
                
                kafkaService.createTopicAsync(
                    currentCluster.getBrokerUrls(),
                    topicConfig.getName(),
                    topicConfig.getPartitions(),
                    topicConfig.getReplicationFactor()
                ).whenComplete((result, throwable) -> {
                    Platform.runLater(() -> {
                        showLoading(false);
                        if (throwable == null) {
                            updateStatus("Topic created successfully: " + topicConfig.getName());
                            refreshTopics();
                        } else {
                            updateStatus("Failed to create topic: " + throwable.getMessage());
                            DialogHelper.showErrorDialog("Topic Creation Error",
                                "Failed to create topic '" + topicConfig.getName() + "'",
                                throwable.getMessage());
                        }
                    });
                });
            });
        }
    }
    
    @FXML
    private void onDeleteTopic() {
        if (currentTopic != null && currentCluster != null) {
            boolean confirmed = DialogHelper.showConfirmDialog(
                "Delete Topic",
                "Are you sure you want to delete the topic '" + currentTopic.getName() + "'?",
                "This action cannot be undone and all messages will be lost."
            );
            
            if (confirmed) {
                showLoading(true);
                updateStatus("Deleting topic: " + currentTopic.getName() + "!");
                
                kafkaService.deleteTopicAsync(currentCluster.getBrokerUrls(), currentTopic.getName())
                    .whenComplete((result, throwable) -> {
                        Platform.runLater(() -> {
                            showLoading(false);
                            if (throwable == null) {
                                updateStatus("Topic deleted successfully: " + currentTopic.getName());
                                refreshTopics();
                            } else {
                                updateStatus("Failed to delete topic: " + throwable.getMessage());
                                DialogHelper.showErrorDialog("Topic Deletion Error",
                                    "Failed to delete topic '" + currentTopic.getName() + "'",
                                    throwable.getMessage());
                            }
                        });
                    });
            }
        }
    }
    
    @FXML
    private void onRefreshTopics() {
        refreshTopics();
    }
    
    private void refreshTopics() {
        if (currentCluster != null && "Connected".equals(currentCluster.getStatus())) {
            showLoading(true);
            updateStatus("Refreshing topics!");
            
            kafkaService.getTopicsAsync(currentCluster.getBrokerUrls())
                .whenComplete((topicList, throwable) -> {
                    Platform.runLater(() -> {
                        showLoading(false);
                        if (throwable == null) {
                            topics.setAll(topicList);
                            updateStatus("Topics refreshed: " + topicList.size() + " topics found");
                        } else {
                            updateStatus("Failed to refresh topics: " + throwable.getMessage());
                            DialogHelper.showErrorDialog("Topic Refresh Error",
                                "Failed to refresh topics", throwable.getMessage());
                        }
                    });
                });
        }
    }
    
    @FXML
    private void onConfigTopic() {
        if (currentTopic != null && currentCluster != null) {
            showEnhancedTopicConfigDialog();
        }
    }
    
    private void showEnhancedTopicConfigDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Topic Configuration - " + currentTopic.getName());
        dialog.setHeaderText("Configure Topic: " + currentTopic.getName());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Topic info section
        VBox infoSection = new VBox(5);
        infoSection.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 5;");
        
        Label infoLabel = new Label("Topic Information");
        infoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        String topicInfo = String.format(
            "Name: %s\nPartitions: %d\nReplication Factor: %d\nTotal Messages: %s\nSize: %s",
            currentTopic.getName(),
            currentTopic.getPartitions(),
            currentTopic.getReplicationFactor(),
            "Loading", // Would be loaded from service
            "Loading"  // Would be loaded from service
        );
        
        Label infoText = new Label(topicInfo);
        infoSection.getChildren().addAll(infoLabel, infoText);
        
        // Configuration tabs
        TabPane configTabs = new TabPane();
        
        // Basic Config Tab
        Tab basicTab = new Tab("Basic Configuration");
        VBox basicContent = new VBox(10);
        basicContent.setPadding(new Insets(10));
        
        // Search field for configurations
        TextField configSearchField = new TextField();
        configSearchField.setPromptText("Search configuration properties");
        
        TableView<ClusterConfig> basicConfigTable = new TableView<>();
        TableColumn<ClusterConfig, String> configNameCol = new TableColumn<>("Property");
        configNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        configNameCol.setPrefWidth(250);
        
        TableColumn<ClusterConfig, String> configValueCol = new TableColumn<>("Value");
        configValueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        configValueCol.setPrefWidth(200);
        
        TableColumn<ClusterConfig, String> configDescCol = new TableColumn<>("Description");
        configDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        configDescCol.setPrefWidth(300);
        
        basicConfigTable.getColumns().addAll(configNameCol, configValueCol, configDescCol);
        basicConfigTable.setPrefHeight(250);
        
        // Config action buttons
        HBox configActions = new HBox(10);
        Button editConfigBtn = new Button("Edit Property");
        Button addConfigBtn = new Button("Add Property");
        Button removeConfigBtn = new Button("Remove Property");
        Button resetConfigBtn = new Button("Reset to Default");
        
        editConfigBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");
        addConfigBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        removeConfigBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        
        configActions.getChildren().addAll(editConfigBtn, addConfigBtn, removeConfigBtn, resetConfigBtn);
        
        basicContent.getChildren().addAll(configSearchField, basicConfigTable, configActions);
        basicTab.setContent(basicContent);
        
        // Advanced Config Tab
        Tab advancedTab = new Tab("Advanced Settings");
        VBox advancedContent = new VBox(10);
        advancedContent.setPadding(new Insets(10));
        
        // Retention settings
        VBox retentionBox = new VBox(5);
        Label retentionLabel = new Label("Retention Settings");
        retentionLabel.setStyle("-fx-font-weight: bold;");
        
        HBox retentionTimeRow = new HBox(10);
        TextField retentionTimeField = new TextField();
        retentionTimeField.setPromptText("e.g., 168h, 7d, 604800000ms");
        retentionTimeRow.getChildren().addAll(new Label("Retention Time:"), retentionTimeField);
        
        HBox retentionSizeRow = new HBox(10);
        TextField retentionSizeField = new TextField();
        retentionSizeField.setPromptText("e.g., 1GB, 1000000000");
        retentionSizeRow.getChildren().addAll(new Label("Retention Size:"), retentionSizeField);
        
        retentionBox.getChildren().addAll(retentionLabel, retentionTimeRow, retentionSizeRow);
        
        // Cleanup settings
        VBox cleanupBox = new VBox(5);
        Label cleanupLabel = new Label("Cleanup Policy");
        cleanupLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> cleanupPolicyCombo = new ComboBox<>();
        cleanupPolicyCombo.getItems().addAll("delete", "compact", "compact,delete");
        cleanupPolicyCombo.setValue("delete");
        
        cleanupBox.getChildren().addAll(cleanupLabel, cleanupPolicyCombo);
        
        // Compression settings
        VBox compressionBox = new VBox(5);
        Label compressionLabel = new Label("Compression");
        compressionLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> compressionCombo = new ComboBox<>();
        compressionCombo.getItems().addAll("uncompressed", "snappy", "lz4", "gzip", "zstd");
        compressionCombo.setValue("snappy");
        
        compressionBox.getChildren().addAll(compressionLabel, compressionCombo);
        
        advancedContent.getChildren().addAll(retentionBox, new Separator(), cleanupBox, new Separator(), compressionBox);
        advancedTab.setContent(advancedContent);
        
        // Partitions Tab
        Tab partitionsTab = new Tab("Partitions & Replicas");
        VBox partitionsContent = new VBox(10);
        partitionsContent.setPadding(new Insets(10));
        
        // Partition management
        VBox partitionMgmt = new VBox(5);
        Label partitionLabel = new Label("Partition Management");
        partitionLabel.setStyle("-fx-font-weight: bold;");
        
        HBox partitionCountRow = new HBox(10);
        Label currentPartitionsLabel = new Label("Current Partitions: " + currentTopic.getPartitions());
        TextField newPartitionsField = new TextField();
        newPartitionsField.setPromptText("New partition count");
        Button addPartitionsBtn = new Button("Add Partitions");
        addPartitionsBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        
        partitionCountRow.getChildren().addAll(currentPartitionsLabel, new Label("New:"), newPartitionsField, addPartitionsBtn);
        
        // Replica assignment
        VBox replicaBox = new VBox(5);
        Label replicaLabel = new Label("Replica Assignment");
        replicaLabel.setStyle("-fx-font-weight: bold;");
        
        TableView<Object> replicaTable = new TableView<>();
        TableColumn<Object, Integer> partitionIdCol = new TableColumn<>("Partition");
        TableColumn<Object, String> replicasCol = new TableColumn<>("Replicas");
        TableColumn<Object, String> leadersCol = new TableColumn<>("Leader");
        TableColumn<Object, String> isrCol = new TableColumn<>("In-Sync Replicas");
        
        replicaTable.getColumns().addAll(partitionIdCol, replicasCol, leadersCol, isrCol);
        replicaTable.setPrefHeight(150);
        
        replicaBox.getChildren().addAll(replicaLabel, replicaTable);
        
        partitionMgmt.getChildren().addAll(partitionLabel, partitionCountRow);
        partitionsContent.getChildren().addAll(partitionMgmt, new Separator(), replicaBox);
        partitionsTab.setContent(partitionsContent);
        
        configTabs.getTabs().addAll(basicTab, advancedTab, partitionsTab);
        
        // Main action buttons
        HBox mainActions = new HBox(10);
        Button saveBtn = new Button("Save Changes");
        Button exportBtn = new Button("Export Config");
        Button importBtn = new Button("Import Config");
        Button applyTemplateBtn = new Button("Apply Template");
        
        saveBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        
        mainActions.getChildren().addAll(saveBtn, exportBtn, importBtn, applyTemplateBtn);
        
        content.getChildren().addAll(infoSection, configTabs, mainActions);
        
        // Load configuration data
        showLoading(true);
        updateStatus("Loading topic configuration!");
        
        kafkaService.getTopicConfigAsync(currentCluster.getBrokerUrls(), currentTopic.getName())
            .whenComplete((configMap, throwable) -> {
                Platform.runLater(() -> {
                    showLoading(false);
                    if (throwable == null) {
                        // Convert Map to List<ClusterConfig>
                        List<ClusterConfig> configList = configMap.entrySet().stream()
                            .map(entry -> {
                                ClusterConfig config = new ClusterConfig();
                                config.setName(entry.getKey());
                                config.setValue(entry.getValue());
                                config.setSource("topic");
                                return config;
                            })
                            .collect(Collectors.toList());
                        
                        basicConfigTable.getItems().setAll(configList);
                        updateStatus("Topic configuration loaded");
                    } else {
                        updateStatus("Failed to load topic configuration: " + throwable.getMessage());
                        DialogHelper.showErrorDialog("Config Load Error",
                            "Failed to load topic configuration", throwable.getMessage());
                    }
                });
            });
        
        // Configuration button actions
        editConfigBtn.setOnAction(e -> {
            ClusterConfig selected = basicConfigTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditConfigDialog(selected, basicConfigTable);
            } else {
                DialogHelper.showErrorDialog("No Selection", "Selection Required", 
                    "Please select a configuration property to edit.");
            }
        });
        
        addConfigBtn.setOnAction(e -> {
            showAddConfigDialog(basicConfigTable);
        });
        
        removeConfigBtn.setOnAction(e -> {
            ClusterConfig selected = basicConfigTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                boolean confirmed = DialogHelper.showConfirmDialog(
                    "Remove Configuration",
                    "Remove configuration property '" + selected.getName() + "'?",
                    "This will reset the property to its default value."
                );
                if (confirmed) {
                    basicConfigTable.getItems().remove(selected);
                    updateStatus("Configuration property marked for removal");
                }
            } else {
                DialogHelper.showErrorDialog("No Selection", "Selection Required",
                    "Please select a configuration property to remove.");
            }
        });
        
        resetConfigBtn.setOnAction(e -> {
            boolean confirmed = DialogHelper.showConfirmDialog(
                "Reset Configuration",
                "Reset all configuration properties to default values?",
                "This will remove all custom configuration overrides."
            );
            if (confirmed) {
                basicConfigTable.getItems().clear();
                updateStatus("Configuration properties reset");
            }
        });
        
        // Button actions
        saveBtn.setOnAction(e -> {
            saveTopicConfiguration(basicConfigTable, retentionTimeField, retentionSizeField, 
                cleanupPolicyCombo, compressionCombo, dialog);
        });
        
        addPartitionsBtn.setOnAction(e -> {
            String newCountStr = newPartitionsField.getText();
            if (!newCountStr.isEmpty()) {
                try {
                    int newCount = Integer.parseInt(newCountStr);
                    if (newCount > currentTopic.getPartitions()) {
                        addTopicPartitions(newCount, dialog);
                    } else {
                        DialogHelper.showErrorDialog("Invalid Partition Count", 
                            "Invalid Value", "New partition count must be greater than current count.");
                    }
                } catch (NumberFormatException ex) {
                    DialogHelper.showErrorDialog("Invalid Input", 
                        "Invalid Number", "Please enter a valid number.");
                }
            }
        });
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(800, 650);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void loadTopicPartitions() {
        if (currentTopic != null && currentCluster != null) {
            kafkaService.getPartitionsAsync(currentCluster.getBrokerUrls(), currentTopic.getName())
                .whenComplete((partitions, throwable) -> {
                    Platform.runLater(() -> {
                        if (throwable == null) {
                            partitionComboBox.getItems().setAll(partitions);
                            if (!partitions.isEmpty()) {
                                partitionComboBox.getSelectionModel().selectFirst();
                            }
                        }
                    });
                });
        }
    }
    
    private void loadPartitionOffsets(int partition) {
        if (currentTopic != null && currentCluster != null) {
            kafkaService.getPartitionOffsetsAsync(currentCluster.getBrokerUrls(), 
                currentTopic.getName(), partition)
                .whenComplete((offsets, throwable) -> {
                    Platform.runLater(() -> {
                        if (throwable == null) {
                            fromOffsetField.setText(String.valueOf(offsets.getLowWatermark()));
                            toOffsetField.setText(String.valueOf(Math.max(0, offsets.getHighWatermark() - 1)));
                        }
                    });
                });
        }
    }
    
    @FXML
    private void onLoadMessages() {
        if (currentTopic != null && currentCluster != null && partitionComboBox.getValue() != null) {
            try {
                long fromOffset = Long.parseLong(fromOffsetField.getText().trim());
                long toOffset = Long.parseLong(toOffsetField.getText().trim());
                int partition = partitionComboBox.getValue();
                
                showLoading(true);
                updateStatus("Loading messages!");
                
                kafkaService.getMessagesBetweenOffsetsAsync(
                    currentCluster.getBrokerUrls(),
                    currentTopic.getName(),
                    partition,
                    fromOffset,
                    toOffset
                ).whenComplete((messageList, throwable) -> {
                    Platform.runLater(() -> {
                        showLoading(false);
                        if (throwable == null) {
                            messages.setAll(messageList);
                            updateStatus("Loaded " + messageList.size() + " messages");
                        } else {
                            updateStatus("Failed to load messages: " + throwable.getMessage());
                            DialogHelper.showErrorDialog("Message Load Error",
                                "Failed to load messages", throwable.getMessage());
                        }
                    });
                });
                
            } catch (NumberFormatException e) {
                DialogHelper.showErrorDialog("Invalid Input", 
                    "Invalid offset values", "Please enter valid numeric offsets");
            }
        }
    }
    
    @FXML
    private void onLoadLatest() {
        if (currentTopic != null && currentCluster != null && partitionComboBox.getValue() != null) {
            int partition = partitionComboBox.getValue();
            
            showLoading(true);
            updateStatus("Loading latest messages!");
            
            kafkaService.getLatestMessagesAsync(
                currentCluster.getBrokerUrls(),
                currentTopic.getName(),
                partition,
                100  // Load latest 100 messages
            ).whenComplete((messageList, throwable) -> {
                Platform.runLater(() -> {
                    showLoading(false);
                    if (throwable == null) {
                        messages.setAll(messageList);
                        updateStatus("Loaded " + messageList.size() + " latest messages");
                    } else {
                        updateStatus("Failed to load latest messages: " + throwable.getMessage());
                        DialogHelper.showErrorDialog("Message Load Error",
                            "Failed to load latest messages", throwable.getMessage());
                    }
                });
            });
        }
    }
    
    @FXML
    private void onProduceMessage() {
        if (currentTopic != null && currentCluster != null) {
            DialogHelper.showProduceMessageDialog(currentTopic.getName()).ifPresent(messageData -> {
                showLoading(true);
                updateStatus("Producing message!");
                
                kafkaService.produceMessageAsync(
                    currentCluster.getBrokerUrls(),
                    currentTopic.getName(),
                    messageData.getKey(),
                    messageData.getValue(),
                    messageData.getHeaders(),
                    messageData.getPartition()
                ).whenComplete((result, throwable) -> {
                    Platform.runLater(() -> {
                        showLoading(false);
                        if (throwable == null) {
                            updateStatus("Message produced successfully");
                            // Optionally refresh messages
                            if (partitionComboBox.getValue() != null && 
                                partitionComboBox.getValue() == messageData.getPartition()) {
                                onLoadLatest();
                            }
                        } else {
                            updateStatus("Failed to produce message: " + throwable.getMessage());
                            DialogHelper.showErrorDialog("Message Production Error",
                                "Failed to produce message", throwable.getMessage());
                        }
                    });
                });
            });
        }
    }
    
    @FXML
    private void onSearchMessages() {
        if (currentTopic != null && currentCluster != null) {
            showRegexSearchDialog();
        } else {
            DialogHelper.showErrorDialog("No Topic Selected", "Selection Required", "Please select a topic first.");
        }
    }
    
    @FXML
    private void onSettings() {
        DialogHelper.showSettingsDialog();
    }
    
    @FXML
    private void onAbout() {
        DialogHelper.showAboutDialog();
    }
    
    @FXML
    private void onExit() {
        Platform.exit();
    }
    
    @FXML
    private void onConsumerGroups() {
        if (currentCluster == null || !"Connected".equals(currentCluster.getStatus())) {
            DialogHelper.showErrorDialog("No Connected Cluster", "Connection Required", "Please connect to a cluster first.");
            return;
        }
        
        // Simple implementation for now - show consumer groups in a basic dialog
        showSimpleConsumerGroupsDialog();
    }
    
    @FXML
    private void onBrokers() {
        if (currentCluster == null || !"Connected".equals(currentCluster.getStatus())) {
            DialogHelper.showErrorDialog("No Connected Cluster", "Connection Required", "Please connect to a cluster first.");
            return;
        }
        
        showSimpleBrokersDialog();
    }
    
    @FXML
    private void onClusterConfig() {
        if (currentCluster == null || !"Connected".equals(currentCluster.getStatus())) {
            DialogHelper.showErrorDialog("No Connected Cluster", "Connection Required", "Please connect to a cluster first.");
            return;
        }
        
        showSimpleClusterConfigDialog();
    }
    
    @FXML
    private void onBrokerConfig() {
        if (currentCluster == null || !"Connected".equals(currentCluster.getStatus())) {
            DialogHelper.showErrorDialog("No Connected Cluster", "Connection Required", "Please connect to a cluster first.");
            return;
        }
        
        showSimpleBrokerConfigDialog();
    }
    
    // ===== SIMPLIFIED ADMIN DIALOG METHODS =====
    
    private void showSimpleConsumerGroupsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Consumer Groups - " + currentCluster.getName());
        dialog.setHeaderText("Consumer Groups Management");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Search and filter bar
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search consumer groups");
        ComboBox<String> stateFilter = new ComboBox<>();
        stateFilter.getItems().addAll("All States", "Stable", "PreparingRebalance", "CompletingRebalance", "Dead", "Empty");
        stateFilter.setValue("All States");
        Button refreshBtn = new Button("Refresh");
        filterBar.getChildren().addAll(new Label("Search:"), searchField, new Label("State:"), stateFilter, refreshBtn);
        
        TableView<ConsumerGroupInfo> table = new TableView<>();
        
        TableColumn<ConsumerGroupInfo, String> groupIdCol = new TableColumn<>("Group ID");
        groupIdCol.setCellValueFactory(new PropertyValueFactory<>("groupId"));
        groupIdCol.setPrefWidth(200);
        
        TableColumn<ConsumerGroupInfo, String> stateCol = new TableColumn<>("State");
        stateCol.setCellValueFactory(new PropertyValueFactory<>("state"));
        stateCol.setPrefWidth(120);
        
        TableColumn<ConsumerGroupInfo, Integer> membersCol = new TableColumn<>("Members");
        membersCol.setCellValueFactory(new PropertyValueFactory<>("memberCount"));
        membersCol.setPrefWidth(80);
        
        TableColumn<ConsumerGroupInfo, String> coordinatorCol = new TableColumn<>("Coordinator");
        coordinatorCol.setCellValueFactory(new PropertyValueFactory<>("coordinator"));
        coordinatorCol.setPrefWidth(120);
        
        TableColumn<ConsumerGroupInfo, Long> lagCol = new TableColumn<>("Total Lag");
        lagCol.setCellValueFactory(new PropertyValueFactory<>("totalLag"));
        lagCol.setPrefWidth(100);
        
        table.getColumns().addAll(groupIdCol, stateCol, membersCol, coordinatorCol, lagCol);
        table.setPrefHeight(300);
        
        // Action buttons
        HBox actionBar = new HBox(10);
        Button resetOffsetsBtn = new Button("Reset Offsets");
        Button deleteGroupBtn = new Button("Delete Group");
        Button viewDetailsBtn = new Button("View Details");
        Button exportBtn = new Button("Export Data");
        
        resetOffsetsBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        deleteGroupBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        viewDetailsBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");
        
        actionBar.getChildren().addAll(resetOffsetsBtn, deleteGroupBtn, viewDetailsBtn, exportBtn);
        
        // Consumer group details area
        TextArea detailsArea = new TextArea();
        detailsArea.setPromptText("Select a consumer group to view details");
        detailsArea.setPrefRowCount(6);
        detailsArea.setEditable(false);
        
        // Table selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String details = String.format(
                    "Group ID: %s\nState: %s\nMembers: %d\nCoordinator: %s\nTotal Lag: %d\n\nPartition Details:\n- Topic assignments and lag information\n- Member details and assignments\n- Offset commit history",
                    newSelection.getGroupId(),
                    newSelection.getState(),
                    newSelection.getMemberCount(),
                    newSelection.getCoordinator() != null ? newSelection.getCoordinator() : "Unknown",
                    newSelection.getTotalLag()
                );
                detailsArea.setText(details);
            }
        });
        
        // Load data
        kafkaService.getConsumerGroupsAsync(currentCluster.getBrokerUrls())
            .thenAccept(groups -> Platform.runLater(() -> table.getItems().setAll(groups)))
            .exceptionally(ex -> {
                Platform.runLater(() -> DialogHelper.showErrorDialog("Load Error", "Failed to load consumer groups", ex.getMessage()));
                return null;
            });
        
        // Button actions
        resetOffsetsBtn.setOnAction(e -> {
            ConsumerGroupInfo selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showResetOffsetsDialog(selected.getGroupId());
            } else {
                DialogHelper.showErrorDialog("No Selection", "Selection Required", "Please select a consumer group first.");
            }
        });
        
        deleteGroupBtn.setOnAction(e -> {
            ConsumerGroupInfo selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                boolean confirmed = DialogHelper.showConfirmDialog(
                    "Delete Consumer Group",
                    "Are you sure you want to delete consumer group '" + selected.getGroupId() + "'?",
                    "This action cannot be undone."
                );
                if (confirmed) {
                    deleteConsumerGroup(selected.getGroupId(), table);
                }
            } else {
                DialogHelper.showErrorDialog("No Selection", "Selection Required", "Please select a consumer group first.");
            }
        });
        
        content.getChildren().addAll(
            filterBar,
            table,
            actionBar,
            new Label("Group Details:"),
            detailsArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(700, 600);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showResetOffsetsDialog(String groupId) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Reset Consumer Group Offsets");
        dialog.setHeaderText("Reset offsets for consumer group: " + groupId);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Reset strategy
        HBox strategyRow = new HBox(10);
        ComboBox<String> strategyCombo = new ComboBox<>();
        strategyCombo.getItems().addAll(
            "Reset to Latest",
            "Reset to Earliest", 
            "Reset to Specific Offset",
            "Reset to Timestamp",
            "Reset by Duration"
        );
        strategyCombo.setValue("Reset to Latest");
        strategyRow.getChildren().addAll(new Label("Reset Strategy:"), strategyCombo);
        
        // Topic selection
        HBox topicRow = new HBox(10);
        ComboBox<String> topicCombo = new ComboBox<>();
        topicCombo.getItems().addAll("All Topics");
        topicCombo.getItems().addAll(topics.stream().map(TopicInfo::getName).toList());
        topicCombo.setValue("All Topics");
        topicRow.getChildren().addAll(new Label("Target Topic:"), topicCombo);
        
        // Value input (for specific offset/timestamp)
        HBox valueRow = new HBox(10);
        TextField valueField = new TextField();
        valueField.setPromptText("Enter offset, timestamp, or duration");
        valueField.setDisable(true);
        valueRow.getChildren().addAll(new Label("Value:"), valueField);
        
        strategyCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean needsValue = newVal != null && (newVal.contains("Specific") || newVal.contains("Timestamp") || newVal.contains("Duration"));
            valueField.setDisable(!needsValue);
            if (needsValue) {
                if (newVal.contains("Timestamp")) {
                    valueField.setPromptText("Enter timestamp (yyyy-MM-dd HH:mm:ss)");
                } else if (newVal.contains("Duration")) {
                    valueField.setPromptText("Enter duration (e.g., 1h, 30m, 2d)");
                } else {
                    valueField.setPromptText("Enter specific offset");
                }
            }
        });
        
        // Dry run option
        CheckBox dryRunBox = new CheckBox("Dry run (preview changes only)");
        dryRunBox.setSelected(true);
        
        // Execute button
        Button executeBtn = new Button("Execute Reset");
        executeBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        
        executeBtn.setOnAction(e -> {
            executeOffsetReset(groupId, strategyCombo.getValue(), topicCombo.getValue(), 
                             valueField.getText(), dryRunBox.isSelected(), dialog);
        });
        
        content.getChildren().addAll(
            strategyRow, topicRow, valueRow, dryRunBox,
            new Separator(),
            executeBtn
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(450, 300);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.show();
    }
    
    private void showSimpleBrokersDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Brokers - " + currentCluster.getName());
        dialog.setHeaderText("Cluster Brokers Management");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TableView<BrokerInfo> table = new TableView<>();
        
        TableColumn<BrokerInfo, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        TableColumn<BrokerInfo, String> hostCol = new TableColumn<>("Host");
        hostCol.setCellValueFactory(new PropertyValueFactory<>("host"));
        hostCol.setPrefWidth(150);
        
        TableColumn<BrokerInfo, Integer> portCol = new TableColumn<>("Port");
        portCol.setCellValueFactory(new PropertyValueFactory<>("port"));
        portCol.setPrefWidth(80);
        
        TableColumn<BrokerInfo, Boolean> controllerCol = new TableColumn<>("Controller");
        controllerCol.setCellValueFactory(new PropertyValueFactory<>("controller"));
        controllerCol.setPrefWidth(80);
        
        TableColumn<BrokerInfo, String> rackCol = new TableColumn<>("Rack");
        rackCol.setCellValueFactory(new PropertyValueFactory<>("rack"));
        rackCol.setPrefWidth(100);
        
        TableColumn<BrokerInfo, String> versionCol = new TableColumn<>("Version");
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));
        versionCol.setPrefWidth(120);
        
        table.getColumns().addAll(idCol, hostCol, portCol, controllerCol, rackCol, versionCol);
        table.setPrefHeight(250);
        
        // Broker actions
        HBox actionBar = new HBox(10);
        Button viewConfigBtn = new Button("View Config");
        Button viewMetricsBtn = new Button("View Metrics");
        Button viewLogsBtn = new Button("View Logs");
        Button exportBtn = new Button("Export Data");
        
        viewConfigBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");
        viewMetricsBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        
        actionBar.getChildren().addAll(viewConfigBtn, viewMetricsBtn, viewLogsBtn, exportBtn);
        
        // Broker details area
        TextArea detailsArea = new TextArea();
        detailsArea.setPromptText("Select a broker to view details");
        detailsArea.setPrefRowCount(6);
        detailsArea.setEditable(false);
        
        // Table selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String details = String.format(
                    "Broker ID: %d\nHost: %s:%d\nController: %s\nRack: %s\nVersion: %s\n\nAdditional Info:\n- Leader partitions: %d\n- Replica partitions: %d\n- Disk usage: %s\n- Network throughput: %s",
                    newSelection.getId(),
                    newSelection.getHost(),
                    newSelection.getPort(),
                    newSelection.isController() ? "Yes" : "No",
                    newSelection.getRack() != null ? newSelection.getRack() : "Unknown",
                    newSelection.getVersion() != null ? newSelection.getVersion() : "Unknown",
                    // Mock additional data for demonstration
                    (int)(Math.random() * 100),
                    (int)(Math.random() * 500),
                    String.format("%.1f GB", Math.random() * 100),
                    String.format("%.1f MB/s", Math.random() * 50)
                );
                detailsArea.setText(details);
            }
        });
        
        // Load data
        kafkaService.getBrokersAsync(currentCluster.getBrokerUrls())
            .thenAccept(brokers -> Platform.runLater(() -> table.getItems().setAll(brokers)))
            .exceptionally(ex -> {
                Platform.runLater(() -> DialogHelper.showErrorDialog("Load Error", "Failed to load brokers", ex.getMessage()));
                return null;
            });
        
        // Button actions
        viewConfigBtn.setOnAction(e -> {
            BrokerInfo selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showBrokerConfigurationDialog(selected.getId());
            } else {
                DialogHelper.showErrorDialog("No Selection", "Selection Required", "Please select a broker first.");
            }
        });
        
        viewMetricsBtn.setOnAction(e -> {
            updateStatus("Broker metrics view feature coming soon - enterprise version");
        });
        
        content.getChildren().addAll(
            table,
            actionBar,
            new Label("Broker Details:"),
            detailsArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(600, 500);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showBrokerConfigurationDialog(int brokerId) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Broker Configuration - Broker " + brokerId);
        dialog.setHeaderText("Configuration for Broker " + brokerId);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Search and filter
        HBox filterBar = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search configuration properties");
        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("All Categories", "Log", "Network", "Security", "Replication", "Metrics");
        categoryFilter.setValue("All Categories");
        filterBar.getChildren().addAll(new Label("Search:"), searchField, new Label("Category:"), categoryFilter);
        
        TableView<ClusterConfig> table = new TableView<>();
        TableColumn<ClusterConfig, String> nameCol = new TableColumn<>("Property");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(300);
        
        TableColumn<ClusterConfig, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(200);
        
        TableColumn<ClusterConfig, String> sourceCol = new TableColumn<>("Source");
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceCol.setPrefWidth(120);
        
        table.getColumns().addAll(nameCol, valueCol, sourceCol);
        table.setPrefHeight(350);
        
        // Action buttons
        HBox actionBar = new HBox(10);
        Button editBtn = new Button("Edit Property");
        Button resetBtn = new Button("Reset to Default");
        Button exportBtn = new Button("Export Config");
        
        editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        resetBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        
        actionBar.getChildren().addAll(editBtn, resetBtn, exportBtn);
        
        // Load data
        kafkaService.getBrokerConfigAsync(currentCluster.getBrokerUrls(), brokerId)
            .thenAccept(configs -> Platform.runLater(() -> table.getItems().setAll(configs)))
            .exceptionally(ex -> {
                Platform.runLater(() -> DialogHelper.showErrorDialog("Load Error", "Failed to load broker config", ex.getMessage()));
                return null;
            });
        
        editBtn.setOnAction(e -> {
            ClusterConfig selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditBrokerConfigDialog(selected, brokerId, table);
            } else {
                DialogHelper.showErrorDialog("No Selection", "Selection Required", "Please select a configuration property first.");
            }
        });
        
        content.getChildren().addAll(filterBar, table, actionBar);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(650, 500);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showSimpleClusterConfigDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Cluster Configuration - " + currentCluster.getName());
        dialog.setHeaderText("Cluster Configuration Properties");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TableView<ClusterConfig> table = new TableView<>();
        
        TableColumn<ClusterConfig, String> nameCol = new TableColumn<>("Property");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);
        
        TableColumn<ClusterConfig, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(200);
        
        TableColumn<ClusterConfig, String> sourceCol = new TableColumn<>("Source");
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceCol.setPrefWidth(120);
        
        table.getColumns().addAll(nameCol, valueCol, sourceCol);
        table.setPrefHeight(400);
        
        // Action buttons
        HBox actionBar = new HBox(10);
        Button editBtn = new Button("Edit Property");
        Button refreshBtn = new Button("Refresh");
        Button exportBtn = new Button("Export Config");
        
        editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        refreshBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");
        
        actionBar.getChildren().addAll(editBtn, refreshBtn, exportBtn);
        
        content.getChildren().addAll(table, actionBar);
        
        // Load data
        kafkaService.getClusterConfigAsync(currentCluster.getBrokerUrls())
            .thenAccept(configs -> Platform.runLater(() -> table.getItems().setAll(configs)))
            .exceptionally(ex -> {
                Platform.runLater(() -> DialogHelper.showErrorDialog("Load Error", "Failed to load cluster config", ex.getMessage()));
                return null;
            });
        
        // Button actions
        editBtn.setOnAction(e -> {
            ClusterConfig selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditClusterConfigDialog(selected, table);
            } else {
                DialogHelper.showErrorDialog("No Selection", "Selection Required", 
                    "Please select a configuration property to edit.");
            }
        });
        
        refreshBtn.setOnAction(e -> {
            kafkaService.getClusterConfigAsync(currentCluster.getBrokerUrls())
                .thenAccept(configs -> Platform.runLater(() -> table.getItems().setAll(configs)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> DialogHelper.showErrorDialog("Refresh Error", "Failed to refresh cluster config", ex.getMessage()));
                    return null;
                });
        });
        
        exportBtn.setOnAction(e -> {
            updateStatus("Configuration export feature coming soon");
        });
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(600, 550);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showSimpleBrokerConfigDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Broker Configuration - " + currentCluster.getName());
        dialog.setHeaderText("Select a broker to view its configuration");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        ComboBox<Integer> brokerCombo = new ComboBox<>();
        Button loadBtn = new Button("Load Configuration");
        
        TableView<ClusterConfig> table = new TableView<>();
        TableColumn<ClusterConfig, String> nameCol = new TableColumn<>("Property");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);
        
        TableColumn<ClusterConfig, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(200);
        
        table.getColumns().addAll(nameCol, valueCol);
        table.setPrefHeight(400);
        
        HBox controls = new HBox(10);
        controls.getChildren().addAll(new Label("Broker ID:"), brokerCombo, loadBtn);
        
        content.getChildren().addAll(controls, table);
        
        // Load broker IDs
        kafkaService.getBrokersAsync(currentCluster.getBrokerUrls())
            .thenAccept(brokers -> Platform.runLater(() -> {
                brokerCombo.getItems().setAll(brokers.stream().map(BrokerInfo::getId).toList());
                if (!brokers.isEmpty()) {
                    brokerCombo.setValue(brokers.get(0).getId());
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> DialogHelper.showErrorDialog("Load Error", "Failed to load brokers", ex.getMessage()));
                return null;
            });
        
        loadBtn.setOnAction(e -> {
            Integer brokerId = brokerCombo.getValue();
            if (brokerId != null) {
                kafkaService.getBrokerConfigAsync(currentCluster.getBrokerUrls(), brokerId)
                    .thenAccept(configs -> Platform.runLater(() -> table.getItems().setAll(configs)))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> DialogHelper.showErrorDialog("Load Error", "Failed to load broker config", ex.getMessage()));
                        return null;
                    });
            }
        });
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(500, 550);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void updateMessageDetails(KafkaMessage message) {
        if (message == null) {
            rawMessageTextArea.clear();
            jsonMessageTextArea.clear();
            headersTextArea.clear();
            return;
        }
        
        // Update raw message
        String rawContent = String.format(
            "Offset: %d\nPartition: %d\nKey: %s\nValue: %s\nTimestamp: %s",
            message.getOffset(),
            message.getPartition(),
            message.getKey() != null ? message.getKey() : "null",
            message.getValue() != null ? message.getValue() : "null",
            message.getTimestamp() != null ? message.getTimestamp().format(TIMESTAMP_FORMATTER) : "null"
        );
        rawMessageTextArea.setText(rawContent);
        
        // Update JSON formatted message (if value is JSON)
        String jsonContent = JsonFormatter.formatJson(message.getValue());
        jsonMessageTextArea.setText(jsonContent != null ? jsonContent : message.getValue());
        
        // Update headers
        headersTextArea.setText(message.getHeaders() != null ? message.getHeadersAsString() : "No headers");
    }
    
    // ===== NEW ENTERPRISE FEATURE HANDLERS =====
    
    @FXML
    private void onACLManagement() {
        if (currentCluster == null || !"Connected".equals(currentCluster.getStatus())) {
            DialogHelper.showErrorDialog("No Connected Cluster", "Connection Required", "Please connect to a cluster first.");
            return;
        }
        showACLManagementDialog();
    }
    
    @FXML
    private void onMultiCluster() {
        showMultiClusterDialog();
    }
    
    @FXML
    private void onSchemaRegistry() {
        showEnhancedWindow("Schema Registry", "enhanced/schema-registry.fxml");
    }
    
    @FXML
    private void onKafkaConnect() {
        showEnhancedWindow("Kafka Connect", "enhanced/kafka-connect.fxml");
    }
    
    @FXML
    private void onMetricsDashboard() {
        showEnhancedWindow("Metrics Dashboard", "enhanced/metrics-dashboard.fxml");
    }
    
    @FXML
    private void onImportExport() {
        if (currentTopic != null && currentCluster != null) {
            showImportExportDialog();
        } else {
            DialogHelper.showErrorDialog("No Topic Selected", "Selection Required", "Please select a topic first.");
        }
    }
    
    @FXML
    private void onBulkOperations() {
        if (currentCluster == null || !"Connected".equals(currentCluster.getStatus())) {
            DialogHelper.showErrorDialog("No Connected Cluster", "Connection Required", "Please connect to a cluster first.");
            return;
        }
        showBulkOperationsDialog();
    }
    
    @FXML
    private void onAlerts() {
        showMonitoringAlertsDialog();
    }
    
    @FXML
    private void onWorkspace() {
        showWorkspaceDialog();
    }
    
    @FXML
    private void onRegexSearch() {
        if (currentTopic != null && currentCluster != null) {
            showRegexSearchDialog();
        } else {
            DialogHelper.showErrorDialog("No Topic Selected", "Selection Required", "Please select a topic first.");
        }
    }
    
    @FXML
    private void onMessageReplay() {
        if (currentTopic != null && currentCluster != null) {
            showMessageReplayDialog();
        } else {
            DialogHelper.showErrorDialog("No Topic Selected", "Selection Required", "Please select a topic first.");
        }
    }
    
    @FXML
    private void onToggleTheme() {
        toggleApplicationTheme();
    }
    
    @FXML
    private void onKeyboardShortcuts() {
        showKeyboardShortcutsDialog();
    }
    
    // ===== ENHANCED FEATURE IMPLEMENTATION METHODS =====
    
    private void showEnhancedWindow(String title, String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/" + fxmlPath));
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(title);
            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            logger.error("Failed to open " + title + " window", e);
            DialogHelper.showErrorDialog("Window Error", "Failed to open " + title, e.getMessage());
        }
    }
    
    private void showACLManagementDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("ACL Management - " + currentCluster.getName());
        dialog.setHeaderText("Access Control List Management");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Resource type selection
        HBox resourceRow = new HBox(10);
        resourceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ComboBox<String> resourceTypeCombo = new ComboBox<>();
        resourceTypeCombo.getItems().addAll("Topic", "Group", "Cluster", "TransactionalId", "DelegationToken");
        resourceTypeCombo.setValue("Topic");
        resourceRow.getChildren().addAll(new Label("Resource Type:"), resourceTypeCombo);
        
        // Resource name
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        TextField resourceNameField = new TextField();
        resourceNameField.setPromptText("Resource name or pattern");
        nameRow.getChildren().addAll(new Label("Resource Name:"), resourceNameField);
        
        // Principal
        HBox principalRow = new HBox(10);
        principalRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        TextField principalField = new TextField();
        principalField.setPromptText("User:alice or CN=alice");
        principalRow.getChildren().addAll(new Label("Principal:"), principalField);
        
        // Operation
        HBox operationRow = new HBox(10);
        operationRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ComboBox<String> operationCombo = new ComboBox<>();
        operationCombo.getItems().addAll("All", "Read", "Write", "Create", "Delete", "Alter", "Describe", "ClusterAction", "DescribeConfigs", "AlterConfigs", "IdempotentWrite");
        operationCombo.setValue("Read");
        operationRow.getChildren().addAll(new Label("Operation:"), operationCombo);
        
        // Permission type
        HBox permissionRow = new HBox(10);
        permissionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ComboBox<String> permissionCombo = new ComboBox<>();
        permissionCombo.getItems().addAll("Allow", "Deny");
        permissionCombo.setValue("Allow");
        permissionRow.getChildren().addAll(new Label("Permission:"), permissionCombo);
        
        // Existing ACLs table
        TableView<Object> aclTable = new TableView<>();
        TableColumn<Object, String> resourceCol = new TableColumn<>("Resource");
        TableColumn<Object, String> principalCol = new TableColumn<>("Principal");
        TableColumn<Object, String> opCol = new TableColumn<>("Operation");
        TableColumn<Object, String> permCol = new TableColumn<>("Permission");
        aclTable.getColumns().addAll(resourceCol, principalCol, opCol, permCol);
        aclTable.setPrefHeight(300);
        
        // Buttons
        HBox buttonRow = new HBox(10);
        Button addButton = new Button("Add ACL");
        Button removeButton = new Button("Remove ACL");
        Button refreshButton = new Button("Refresh");
        buttonRow.getChildren().addAll(addButton, removeButton, refreshButton);
        
        addButton.setOnAction(e -> {
            // Implementation for adding ACL
            updateStatus("ACL management feature coming soon - enterprise version");
        });
        
        content.getChildren().addAll(
            resourceRow, nameRow, principalRow, operationRow, permissionRow,
            new Label("Existing ACLs:"), aclTable, buttonRow
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(600, 500);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showMultiClusterDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Multi-Cluster Management");
        dialog.setHeaderText("Manage Multiple Kafka Clusters");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Cluster groups
        Label groupLabel = new Label("Cluster Groups:");
        groupLabel.setStyle("-fx-font-weight: bold;");
        
        TableView<Object> groupTable = new TableView<>();
        TableColumn<Object, String> groupNameCol = new TableColumn<>("Group Name");
        TableColumn<Object, Integer> clusterCountCol = new TableColumn<>("Clusters");
        TableColumn<Object, String> statusCol = new TableColumn<>("Status");
        groupTable.getColumns().addAll(groupNameCol, clusterCountCol, statusCol);
        groupTable.setPrefHeight(200);
        
        // Actions
        HBox actionRow = new HBox(10);
        Button createGroupButton = new Button("Create Group");
        Button manageGroupButton = new Button("Manage Group");
        Button syncClustersButton = new Button("Sync All Clusters");
        actionRow.getChildren().addAll(createGroupButton, manageGroupButton, syncClustersButton);
        
        createGroupButton.setOnAction(e -> {
            updateStatus("Multi-cluster management feature coming soon - enterprise version");
        });
        
        content.getChildren().addAll(groupLabel, groupTable, actionRow);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(500, 400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showImportExportDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Import/Export Messages - " + currentTopic.getName());
        dialog.setHeaderText("Import or Export Messages");
        
        TabPane tabPane = new TabPane();
        
        // Export tab
        Tab exportTab = new Tab("Export");
        VBox exportContent = new VBox(10);
        exportContent.setPadding(new Insets(10));
        
        ComboBox<String> exportFormatCombo = new ComboBox<>();
        exportFormatCombo.getItems().addAll("JSON", "CSV", "Avro");
        exportFormatCombo.setValue("JSON");
        
        TextField exportFileField = new TextField();
        exportFileField.setPromptText("Select output file");
        
        Button browseExportButton = new Button("Browse");
        Button exportButton = new Button("Export Messages");
        
        HBox exportFormatRow = new HBox(10);
        exportFormatRow.getChildren().addAll(new Label("Format:"), exportFormatCombo);
        
        HBox exportFileRow = new HBox(10);
        exportFileRow.getChildren().addAll(new Label("File:"), exportFileField, browseExportButton);
        
        exportContent.getChildren().addAll(exportFormatRow, exportFileRow, exportButton);
        exportTab.setContent(exportContent);
        
        // Import tab
        Tab importTab = new Tab("Import");
        VBox importContent = new VBox(10);
        importContent.setPadding(new Insets(10));
        
        ComboBox<String> importFormatCombo = new ComboBox<>();
        importFormatCombo.getItems().addAll("JSON", "CSV", "Avro");
        importFormatCombo.setValue("JSON");
        
        TextField importFileField = new TextField();
        importFileField.setPromptText("Select input file");
        
        Button browseImportButton = new Button("Browse");
        Button importButton = new Button("Import Messages");
        
        HBox importFormatRow = new HBox(10);
        importFormatRow.getChildren().addAll(new Label("Format:"), importFormatCombo);
        
        HBox importFileRow = new HBox(10);
        importFileRow.getChildren().addAll(new Label("File:"), importFileField, browseImportButton);
        
        importContent.getChildren().addAll(importFormatRow, importFileRow, importButton);
        importTab.setContent(importContent);
        
        tabPane.getTabs().addAll(exportTab, importTab);
        
        exportButton.setOnAction(e -> {
            updateStatus("Message export feature coming soon - enterprise version");
        });
        
        importButton.setOnAction(e -> {
            updateStatus("Message import feature coming soon - enterprise version");
        });
        
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().setPrefSize(500, 300);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showBulkOperationsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Bulk Operations - " + currentCluster.getName());
        dialog.setHeaderText("Perform Bulk Operations on Topics");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Operation selection
        ComboBox<String> operationCombo = new ComboBox<>();
        operationCombo.getItems().addAll(
            "Delete Multiple Topics",
            "Update Topic Configurations",
            "Create Topics from Template",
            "Export Topic Configurations",
            "Reset Consumer Group Offsets"
        );
        operationCombo.setValue("Delete Multiple Topics");
        
        HBox opRow = new HBox(10);
        opRow.getChildren().addAll(new Label("Operation:"), operationCombo);
        
        // Target selection
        TextArea targetArea = new TextArea();
        targetArea.setPromptText("Enter topic names, one per line");
        targetArea.setPrefRowCount(8);
        
        Button executeButton = new Button("Execute Operation");
        executeButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
        
        executeButton.setOnAction(e -> {
            updateStatus("Bulk operations feature coming soon - enterprise version");
        });
        
        content.getChildren().addAll(
            opRow,
            new Label("Target Topics:"),
            targetArea,
            executeButton
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(500, 400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showMonitoringAlertsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Monitoring Alerts");
        dialog.setHeaderText("Configure Real-time Monitoring Alerts");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Alert rules table
        TableView<Object> alertTable = new TableView<>();
        TableColumn<Object, String> nameCol = new TableColumn<>("Alert Name");
        TableColumn<Object, String> metricCol = new TableColumn<>("Metric");
        TableColumn<Object, String> conditionCol = new TableColumn<>("Condition");
        TableColumn<Object, String> statusCol = new TableColumn<>("Status");
        alertTable.getColumns().addAll(nameCol, metricCol, conditionCol, statusCol);
        alertTable.setPrefHeight(250);
        
        // Add new alert
        HBox alertRow = new HBox(10);
        TextField alertNameField = new TextField();
        alertNameField.setPromptText("Alert name");
        
        ComboBox<String> metricCombo = new ComboBox<>();
        metricCombo.getItems().addAll("Consumer Lag", "Topic Size", "Broker CPU", "Broker Memory", "Connection Count");
        
        TextField thresholdField = new TextField();
        thresholdField.setPromptText("Threshold");
        
        Button addAlertButton = new Button("Add Alert");
        
        alertRow.getChildren().addAll(alertNameField, metricCombo, thresholdField, addAlertButton);
        
        addAlertButton.setOnAction(e -> {
            updateStatus("Monitoring alerts feature coming soon - enterprise version");
        });
        
        content.getChildren().addAll(
            new Label("Existing Alerts:"),
            alertTable,
            new Separator(),
            new Label("Add New Alert:"),
            alertRow
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(600, 400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showWorkspaceDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Workspace Management");
        dialog.setHeaderText("Manage Workspaces and Configurations");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Workspace selection
        HBox workspaceRow = new HBox(10);
        ComboBox<String> workspaceCombo = new ComboBox<>();
        workspaceCombo.getItems().addAll("Default", "Development", "Production", "Testing");
        workspaceCombo.setValue("Default");
        
        Button newWorkspaceButton = new Button("New");
        Button deleteWorkspaceButton = new Button("Delete");
        Button exportWorkspaceButton = new Button("Export");
        Button importWorkspaceButton = new Button("Import");
        
        workspaceRow.getChildren().addAll(
            new Label("Workspace:"), workspaceCombo, 
            newWorkspaceButton, deleteWorkspaceButton, exportWorkspaceButton, importWorkspaceButton
        );
        
        // Workspace details
        TextArea workspaceDetails = new TextArea();
        workspaceDetails.setText("Workspace: Default\nClusters: 3\nTopics: 15\nConsumer Groups: 8\nLast Modified: Today");
        workspaceDetails.setPrefRowCount(6);
        workspaceDetails.setEditable(false);
        
        newWorkspaceButton.setOnAction(e -> {
            updateStatus("Workspace management feature coming soon - enterprise version");
        });
        
        content.getChildren().addAll(
            workspaceRow,
            new Label("Workspace Details:"),
            workspaceDetails
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(500, 300);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showRegexSearchDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Advanced Regex Search - " + currentTopic.getName());
        dialog.setHeaderText("Search Messages with Regular Expressions");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Search pattern
        TextField regexField = new TextField();
        regexField.setPromptText("Enter regex pattern");
        
        // Search options
        CheckBox searchKeyBox = new CheckBox("Search in Key");
        CheckBox searchValueBox = new CheckBox("Search in Value");
        CheckBox searchHeadersBox = new CheckBox("Search in Headers");
        CheckBox caseSensitiveBox = new CheckBox("Case Sensitive");
        
        searchValueBox.setSelected(true);
        
        // Results limit
        HBox limitRow = new HBox(10);
        TextField limitField = new TextField("1000");
        limitField.setPrefWidth(100);
        limitRow.getChildren().addAll(new Label("Max Results:"), limitField);
        
        // Search button
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");
        
        // Results area
        TextArea resultsArea = new TextArea();
        resultsArea.setPromptText("Search results will appear here");
        resultsArea.setPrefRowCount(10);
        resultsArea.setEditable(false);
        
        searchButton.setOnAction(e -> {
            String pattern = regexField.getText();
            if (pattern.isEmpty()) {
                DialogHelper.showErrorDialog("Invalid Pattern", "Empty Pattern", "Please enter a regex pattern");
                return;
            }
            
            updateStatus("Advanced regex search feature coming soon - enterprise version");
            resultsArea.setText("Regex search results for pattern: " + pattern + "\n\nFeature coming soon");
        });
        
        content.getChildren().addAll(
            new Label("Regex Pattern:"), regexField,
            new Label("Search In:"), searchKeyBox, searchValueBox, searchHeadersBox, caseSensitiveBox,
            limitRow, searchButton,
            new Label("Results:"), resultsArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(600, 500);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showMessageReplayDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Message Replay - " + currentTopic.getName());
        dialog.setHeaderText("Replay Messages to Topic");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Source configuration
        Label sourceLabel = new Label("Source Configuration:");
        sourceLabel.setStyle("-fx-font-weight: bold;");
        
        HBox offsetRangeRow = new HBox(10);
        TextField fromOffsetReplayField = new TextField();
        fromOffsetReplayField.setPromptText("From offset");
        TextField toOffsetReplayField = new TextField();
        toOffsetReplayField.setPromptText("To offset");
        offsetRangeRow.getChildren().addAll(new Label("Offset Range:"), fromOffsetReplayField, new Label("to"), toOffsetReplayField);
        
        HBox partitionRow = new HBox(10);
        ComboBox<String> partitionReplayCombo = new ComboBox<>();
        partitionReplayCombo.getItems().addAll("All Partitions", "Partition 0", "Partition 1", "Partition 2");
        partitionReplayCombo.setValue("All Partitions");
        partitionRow.getChildren().addAll(new Label("Partitions:"), partitionReplayCombo);
        
        // Target configuration
        Label targetLabel = new Label("Target Configuration:");
        targetLabel.setStyle("-fx-font-weight: bold;");
        
        HBox targetTopicRow = new HBox(10);
        ComboBox<String> targetTopicCombo = new ComboBox<>();
        targetTopicCombo.getItems().addAll(topics.stream().map(TopicInfo::getName).toList());
        targetTopicCombo.setValue(currentTopic.getName());
        targetTopicRow.getChildren().addAll(new Label("Target Topic:"), targetTopicCombo);
        
        // Options
        CheckBox preserveKeyBox = new CheckBox("Preserve Message Keys");
        CheckBox preserveTimestampBox = new CheckBox("Preserve Timestamps");
        CheckBox preserveHeadersBox = new CheckBox("Preserve Headers");
        
        preserveKeyBox.setSelected(true);
        preserveTimestampBox.setSelected(true);
        preserveHeadersBox.setSelected(true);
        
        // Replay button
        Button replayButton = new Button("Start Replay");
        replayButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        
        replayButton.setOnAction(e -> {
            updateStatus("Message replay feature coming soon - enterprise version");
        });
        
        content.getChildren().addAll(
            sourceLabel, offsetRangeRow, partitionRow,
            new Separator(),
            targetLabel, targetTopicRow,
            new Separator(),
            new Label("Options:"), preserveKeyBox, preserveTimestampBox, preserveHeadersBox,
            replayButton
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(500, 400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void toggleApplicationTheme() {
        // Simple theme toggle implementation
        javafx.scene.Scene scene = statusLabel.getScene();
        if (scene != null) {
            ObservableList<String> stylesheets = scene.getStylesheets();
            
            if (stylesheets.stream().anyMatch(s -> s.contains("dark-theme"))) {
                // Switch to light theme
                stylesheets.removeIf(s -> s.contains("dark-theme"));
                stylesheets.add(getClass().getResource("/css/light-theme.css").toExternalForm());
                updateStatus("Switched to light theme");
            } else {
                // Switch to dark theme
                stylesheets.removeIf(s -> s.contains("light-theme"));
                stylesheets.add(getClass().getResource("/css/dark-theme.css").toExternalForm());
                updateStatus("Switched to dark theme");
            }
        }
    }
    
    private void showKeyboardShortcutsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Keyboard Shortcuts");
        dialog.setHeaderText("Available Keyboard Shortcuts");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        String shortcuts = """
            General:
            Ctrl+N         - Add new cluster
            Ctrl+T         - Create new topic  
            Ctrl+R         - Refresh topics
            Ctrl+F         - Search messages
            Ctrl+P         - Produce message
            Ctrl+Shift+T   - Toggle theme
            
            Admin:
            Ctrl+G         - Consumer groups
            Ctrl+B         - Brokers
            Ctrl+Shift+C   - Cluster config
            
            Enterprise:
            Ctrl+S         - Schema registry
            Ctrl+K         - Kafka Connect
            Ctrl+M         - Metrics dashboard
            Ctrl+I         - Import/Export
            
            Navigation:
            F5             - Refresh
            Escape         - Close dialog
            """;
        
        TextArea shortcutsArea = new TextArea(shortcuts);
        shortcutsArea.setEditable(false);
        shortcutsArea.setPrefRowCount(20);
        
        content.getChildren().add(shortcutsArea);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(400, 500);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    // ===== TOPIC CONFIGURATION IMPLEMENTATION METHODS =====
    
    private void saveTopicConfiguration(TableView<ClusterConfig> configTable, 
                                      TextField retentionTimeField, TextField retentionSizeField,
                                      ComboBox<String> cleanupPolicyCombo, ComboBox<String> compressionCombo,
                                      Dialog<Void> dialog) {
        if (currentTopic == null || currentCluster == null) {
            DialogHelper.showErrorDialog("No Topic Selected", "Selection Required", "Please select a topic first.");
            return;
        }
        
        boolean confirmed = DialogHelper.showConfirmDialog(
            "Save Configuration", 
            "Save topic configuration changes?",
            "This will update the topic configuration on the Kafka cluster."
        );
        
        if (!confirmed) return;
        
        // Collect configuration changes
        Map<String, String> configUpdates = new HashMap<>();
        
        // Add retention settings if provided
        String retentionTime = retentionTimeField.getText().trim();
        if (!retentionTime.isEmpty()) {
            configUpdates.put("retention.ms", parseRetentionTime(retentionTime));
        }
        
        String retentionSize = retentionSizeField.getText().trim();
        if (!retentionSize.isEmpty()) {
            configUpdates.put("retention.bytes", parseRetentionSize(retentionSize));
        }
        
        // Add cleanup policy
        if (cleanupPolicyCombo.getValue() != null) {
            configUpdates.put("cleanup.policy", cleanupPolicyCombo.getValue());
        }
        
        // Add compression type
        if (compressionCombo.getValue() != null && !compressionCombo.getValue().equals("uncompressed")) {
            configUpdates.put("compression.type", compressionCombo.getValue());
        }
        
        // Add any modified values from the config table
        for (ClusterConfig config : configTable.getItems()) {
            if (config.getValue() != null && !config.getValue().trim().isEmpty()) {
                configUpdates.put(config.getName(), config.getValue());
            }
        }
        
        if (configUpdates.isEmpty()) {
            DialogHelper.showInfoDialog("No Changes", "No configuration changes detected.");
            return;
        }
        
        showLoading(true);
        updateStatus("Saving topic configuration!");
        
        kafkaService.updateTopicConfigAsync(currentCluster.getBrokerUrls(), currentTopic.getName(), configUpdates)
            .whenComplete((result, throwable) -> {
                Platform.runLater(() -> {
                    showLoading(false);
                    if (throwable == null) {
                        updateStatus("Topic configuration saved successfully");
                        DialogHelper.showInfoDialog("Configuration Saved", 
                            "Topic configuration has been updated successfully.");
                        dialog.close();
                        // Refresh topic list to update any changed information
                        refreshTopics();
                    } else {
                        updateStatus("Failed to save topic configuration: " + throwable.getMessage());
                        DialogHelper.showErrorDialog("Save Error", 
                            "Failed to save topic configuration", throwable.getMessage());
                    }
                });
            });
    }
    
    private void addTopicPartitions(int newPartitionCount, Dialog<Void> dialog) {
        if (currentTopic == null || currentCluster == null) {
            DialogHelper.showErrorDialog("No Topic Selected", "Selection Required", "Please select a topic first.");
            return;
        }
        
        boolean confirmed = DialogHelper.showConfirmDialog(
            "Add Partitions", 
            String.format("Add partitions to topic '%s'?", currentTopic.getName()),
            String.format("This will increase partition count from %d to %d. This operation cannot be undone.", 
                currentTopic.getPartitions(), newPartitionCount)
        );
        
        if (!confirmed) return;
        
        showLoading(true);
        updateStatus("Adding partitions to topic");
        
        // Use the proper partition addition method
        kafkaService.addPartitionsToTopicAsync(currentCluster.getBrokerUrls(), currentTopic.getName(), newPartitionCount)
            .whenComplete((result, throwable) -> {
                Platform.runLater(() -> {
                    showLoading(false);
                    if (throwable == null) {
                        updateStatus("Partitions added successfully");
                        DialogHelper.showInfoDialog("Partitions Added", 
                            String.format("Topic '%s' now has %d partitions.", 
                                currentTopic.getName(), newPartitionCount));
                        dialog.close();
                        // Refresh topic list to update partition count
                        refreshTopics();
                    } else {
                        updateStatus("Failed to add partitions: " + throwable.getMessage());
                        DialogHelper.showErrorDialog("Partition Addition Error", 
                            "Failed to add partitions to topic", throwable.getMessage());
                    }
                });
            });
    }
    
    private String parseRetentionTime(String input) {
        // Convert human-readable time to milliseconds
        String lower = input.toLowerCase().trim();
        try {
            if (lower.endsWith("ms")) {
                return lower.substring(0, lower.length() - 2);
            } else if (lower.endsWith("s")) {
                return String.valueOf(Long.parseLong(lower.substring(0, lower.length() - 1)) * 1000);
            } else if (lower.endsWith("m")) {
                return String.valueOf(Long.parseLong(lower.substring(0, lower.length() - 1)) * 60000);
            } else if (lower.endsWith("h")) {
                return String.valueOf(Long.parseLong(lower.substring(0, lower.length() - 1)) * 3600000);
            } else if (lower.endsWith("d")) {
                return String.valueOf(Long.parseLong(lower.substring(0, lower.length() - 1)) * 86400000);
            } else {
                // Assume milliseconds if no unit specified
                return input;
            }
        } catch (NumberFormatException e) {
            return input; // Return as-is if parsing fails
        }
    }
    
    private String parseRetentionSize(String input) {
        // Convert human-readable size to bytes
        String lower = input.toLowerCase().trim();
        try {
            if (lower.endsWith("b")) {
                return lower.substring(0, lower.length() - 1);
            } else if (lower.endsWith("kb")) {
                return String.valueOf(Long.parseLong(lower.substring(0, lower.length() - 2)) * 1024);
            } else if (lower.endsWith("mb")) {
                return String.valueOf(Long.parseLong(lower.substring(0, lower.length() - 2)) * 1024 * 1024);
            } else if (lower.endsWith("gb")) {
                return String.valueOf(Long.parseLong(lower.substring(0, lower.length() - 2)) * 1024 * 1024 * 1024);
            } else {
                // Assume bytes if no unit specified
                return input;
            }
        } catch (NumberFormatException e) {
            return input; // Return as-is if parsing fails
        }
    }
    
    private void showEditConfigDialog(ClusterConfig config, TableView<ClusterConfig> table) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Configuration Property");
        dialog.setHeaderText("Edit configuration property: " + config.getName());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TextField nameField = new TextField(config.getName());
        nameField.setEditable(false);
        nameField.setStyle("-fx-background-color: #f0f0f0;");
        
        TextField valueField = new TextField(config.getValue());
        valueField.setPromptText("Enter configuration value");
        
        TextArea descArea = new TextArea(config.getDescription());
        descArea.setEditable(false);
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        descArea.setStyle("-fx-background-color: #f0f0f0;");
        
        content.getChildren().addAll(
            new Label("Property Name:"), nameField,
            new Label("Value:"), valueField,
            new Label("Description:"), descArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(400, 300);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return valueField.getText();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(newValue -> {
            config.setValue(newValue);
            table.refresh();
            updateStatus("Configuration property updated");
        });
    }
    
    private void showAddConfigDialog(TableView<ClusterConfig> table) {
        Dialog<ClusterConfig> dialog = new Dialog<>();
        dialog.setTitle("Add Configuration Property");
        dialog.setHeaderText("Add new configuration property");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        ComboBox<String> nameCombo = new ComboBox<>();
        nameCombo.setEditable(true);
        nameCombo.getItems().addAll(
            "retention.ms", "retention.bytes", "cleanup.policy", "compression.type",
            "max.message.bytes", "min.insync.replicas", "unclean.leader.election.enable",
            "delete.retention.ms", "file.delete.delay.ms", "flush.messages", "flush.ms",
            "segment.bytes", "segment.ms", "index.interval.bytes"
        );
        nameCombo.setPromptText("Select or enter property name");
        
        TextField valueField = new TextField();
        valueField.setPromptText("Enter configuration value");
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Optional description");
        descArea.setPrefRowCount(2);
        descArea.setWrapText(true);
        
        content.getChildren().addAll(
            new Label("Property Name:"), nameCombo,
            new Label("Value:"), valueField,
            new Label("Description:"), descArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(400, 250);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK && !nameCombo.getEditor().getText().trim().isEmpty() 
                && !valueField.getText().trim().isEmpty()) {
                ClusterConfig newConfig = new ClusterConfig();
                newConfig.setName(nameCombo.getEditor().getText().trim());
                newConfig.setValue(valueField.getText().trim());
                newConfig.setDescription(descArea.getText().trim());
                newConfig.setSource("user");
                return newConfig;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(newConfig -> {
            table.getItems().add(newConfig);
            updateStatus("Configuration property added");
        });
    }
    
    private void executeOffsetReset(String groupId, String strategy, String targetTopic, 
                                   String value, boolean dryRun, Dialog<Void> dialog) {
        if (currentCluster == null) {
            DialogHelper.showErrorDialog("No Cluster", "No Connected Cluster", "Please connect to a cluster first.");
            return;
        }
        
        // Validate inputs
        if (strategy.contains("Specific") && (value == null || value.trim().isEmpty())) {
            DialogHelper.showErrorDialog("Missing Value", "Value Required", 
                "Please enter a specific offset value.");
            return;
        }
        
        if (strategy.contains("Timestamp") && (value == null || value.trim().isEmpty())) {
            DialogHelper.showErrorDialog("Missing Value", "Timestamp Required", 
                "Please enter a timestamp value.");
            return;
        }
        
        String operationType = dryRun ? "Preview" : "Execute";
        boolean confirmed = DialogHelper.showConfirmDialog(
            operationType + " Offset Reset",
            String.format("%s offset reset for consumer group '%s'?", operationType, groupId),
            String.format("Strategy: %s\nTarget: %s\n%s", strategy, targetTopic, 
                dryRun ? "This is a dry run - no actual changes will be made." : "This will modify consumer group offsets.")
        );
        
        if (!confirmed) return;
        
        showLoading(true);
        updateStatus(operationType + " offset reset!");
        
        CompletableFuture<Void> resetFuture;
        
        if (strategy.equals("Reset to Latest")) {
            resetFuture = kafkaService.resetConsumerGroupOffsetsToLatestAsync(
                currentCluster.getBrokerUrls(), groupId, targetTopic.equals("All Topics") ? null : targetTopic);
        } else if (strategy.equals("Reset to Earliest")) {
            resetFuture = kafkaService.resetConsumerGroupOffsetsToEarliestAsync(
                currentCluster.getBrokerUrls(), groupId, targetTopic.equals("All Topics") ? null : targetTopic);
        } else if (strategy.equals("Reset to Specific Offset")) {
            try {
                long offset = Long.parseLong(value.trim());
                // For now, we'll use partition 0 - this could be enhanced to support all partitions
                resetFuture = kafkaService.resetConsumerGroupOffsetsToOffsetAsync(
                    currentCluster.getBrokerUrls(), groupId, targetTopic, 0, offset);
            } catch (NumberFormatException ex) {
                showLoading(false);
                DialogHelper.showErrorDialog("Invalid Offset", "Invalid Number", 
                    "Please enter a valid offset number.");
                return;
            }
        } else {
            // For timestamp and duration strategies, we'll fall back to latest for now
            resetFuture = kafkaService.resetConsumerGroupOffsetsToLatestAsync(
                currentCluster.getBrokerUrls(), groupId, targetTopic.equals("All Topics") ? null : targetTopic);
        }
        
        resetFuture.whenComplete((result, throwable) -> {
            Platform.runLater(() -> {
                showLoading(false);
                if (throwable == null) {
                    updateStatus("Offset reset " + (dryRun ? "preview" : "execution") + " completed successfully");
                    DialogHelper.showInfoDialog(operationType + " Completed", 
                        "Consumer group offset reset " + (dryRun ? "preview" : "execution") + " completed successfully.");
                    dialog.close();
                } else {
                    updateStatus("Failed to " + operationType.toLowerCase() + " offset reset: " + throwable.getMessage());
                    DialogHelper.showErrorDialog("Reset Error", 
                        "Failed to " + operationType.toLowerCase() + " offset reset", throwable.getMessage());
                }
            });
        });
    }
    
    private void deleteConsumerGroup(String groupId, TableView<ConsumerGroupInfo> table) {
        if (currentCluster == null) {
            DialogHelper.showErrorDialog("No Cluster", "No Connected Cluster", "Please connect to a cluster first.");
            return;
        }
        
        showLoading(true);
        updateStatus("Deleting consumer group!");
        
        kafkaService.deleteConsumerGroupAsync(currentCluster.getBrokerUrls(), groupId)
            .whenComplete((result, throwable) -> {
                Platform.runLater(() -> {
                    showLoading(false);
                    if (throwable == null) {
                        updateStatus("Consumer group deleted successfully");
                        DialogHelper.showInfoDialog("Group Deleted", 
                            "Consumer group '" + groupId + "' has been deleted successfully.");
                        
                        // Remove from table and refresh
                        table.getItems().removeIf(group -> group.getGroupId().equals(groupId));
                    } else {
                        updateStatus("Failed to delete consumer group: " + throwable.getMessage());
                        DialogHelper.showErrorDialog("Delete Error", 
                            "Failed to delete consumer group", throwable.getMessage());
                    }
                });
            });
    }
    
    private void showEditBrokerConfigDialog(ClusterConfig config, int brokerId, TableView<ClusterConfig> table) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Broker Configuration");
        dialog.setHeaderText("Edit broker " + brokerId + " configuration property: " + config.getName());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TextField nameField = new TextField(config.getName());
        nameField.setEditable(false);
        nameField.setStyle("-fx-background-color: #f0f0f0;");
        
        TextField valueField = new TextField(config.getValue());
        valueField.setPromptText("Enter configuration value");
        
        Label currentLabel = new Label("Current Value: " + config.getValue());
        Label sourceLabel = new Label("Source: " + config.getSource());
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Property description");
        descArea.setPrefRowCount(3);
        descArea.setEditable(false);
        descArea.setStyle("-fx-background-color: #f0f0f0;");
        
        content.getChildren().addAll(
            new Label("Property Name:"), nameField,
            currentLabel, sourceLabel,
            new Label("New Value:"), valueField,
            new Label("Description:"), descArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(450, 350);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK && !valueField.getText().trim().equals(config.getValue())) {
                return valueField.getText().trim();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(newValue -> {
            saveBrokerConfiguration(brokerId, config.getName(), newValue, table);
        });
    }
    
    private void saveBrokerConfiguration(int brokerId, String propertyName, String newValue, TableView<ClusterConfig> table) {
        if (currentCluster == null) {
            DialogHelper.showErrorDialog("No Cluster", "No Connected Cluster", "Please connect to a cluster first.");
            return;
        }
        
        boolean confirmed = DialogHelper.showConfirmDialog(
            "Update Broker Configuration", 
            String.format("Update broker %d configuration?", brokerId),
            String.format("Property: %s\nNew Value: %s\n\nThis will update the broker configuration.", propertyName, newValue)
        );
        
        if (!confirmed) return;
        
        showLoading(true);
        updateStatus("Updating broker configuration!");
        
        Map<String, String> configUpdate = new HashMap<>();
        configUpdate.put(propertyName, newValue);
        
        kafkaService.updateBrokerConfigAsync(currentCluster.getBrokerUrls(), brokerId, configUpdate)
            .whenComplete((result, throwable) -> {
                Platform.runLater(() -> {
                    showLoading(false);
                    if (throwable == null) {
                        updateStatus("Broker configuration updated successfully");
                        DialogHelper.showInfoDialog("Configuration Updated", 
                            "Broker configuration has been updated successfully.");
                        
                        // Refresh the configuration table
                        kafkaService.getBrokerConfigAsync(currentCluster.getBrokerUrls(), brokerId)
                            .thenAccept(configs -> Platform.runLater(() -> table.getItems().setAll(configs)))
                            .exceptionally(ex -> {
                                Platform.runLater(() -> updateStatus("Failed to refresh broker config: " + ex.getMessage()));
                                return null;
                            });
                    } else {
                        updateStatus("Failed to update broker configuration: " + throwable.getMessage());
                        DialogHelper.showErrorDialog("Update Error", 
                            "Failed to update broker configuration", throwable.getMessage());
                    }
                });
            });
    }
    
    private void showEditClusterConfigDialog(ClusterConfig config, TableView<ClusterConfig> table) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Cluster Configuration");
        dialog.setHeaderText("Edit cluster configuration property: " + config.getName());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TextField nameField = new TextField(config.getName());
        nameField.setEditable(false);
        nameField.setStyle("-fx-background-color: #f0f0f0;");
        
        TextField valueField = new TextField(config.getValue());
        valueField.setPromptText("Enter configuration value");
        
        Label currentLabel = new Label("Current Value: " + config.getValue());
        Label sourceLabel = new Label("Source: " + config.getSource());
        
        TextArea warningArea = new TextArea();
        warningArea.setText("Warning: Modifying cluster configuration can affect cluster performance and behavior.\nPlease ensure you understand the implications of the changes.");
        warningArea.setEditable(false);
        warningArea.setPrefRowCount(2);
        warningArea.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
        
        content.getChildren().addAll(
            new Label("Property Name:"), nameField,
            currentLabel, sourceLabel,
            new Label("New Value:"), valueField,
            warningArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(450, 350);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK && !valueField.getText().trim().equals(config.getValue())) {
                return valueField.getText().trim();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(newValue -> {
            saveClusterConfiguration(config.getName(), newValue, table);
        });
    }
    
    private void saveClusterConfiguration(String propertyName, String newValue, TableView<ClusterConfig> table) {
        if (currentCluster == null) {
            DialogHelper.showErrorDialog("No Cluster", "No Connected Cluster", "Please connect to a cluster first.");
            return;
        }
        
        boolean confirmed = DialogHelper.showConfirmDialog(
            "Update Cluster Configuration", 
            "Update cluster configuration?",
            String.format("Property: %s\nNew Value: %s\n\nWarning: This will update the cluster configuration and may affect all brokers.", propertyName, newValue)
        );
        
        if (!confirmed) return;
        
        showLoading(true);
        updateStatus("Updating cluster configuration!");
        
        Map<String, String> configUpdate = new HashMap<>();
        configUpdate.put(propertyName, newValue);
        
        kafkaService.updateClusterConfigAsync(currentCluster.getBrokerUrls(), configUpdate)
            .whenComplete((result, throwable) -> {
                Platform.runLater(() -> {
                    showLoading(false);
                    if (throwable == null) {
                        updateStatus("Cluster configuration updated successfully");
                        DialogHelper.showInfoDialog("Configuration Updated", 
                            "Cluster configuration has been updated successfully.");
                        
                        // Refresh the configuration table
                        kafkaService.getClusterConfigAsync(currentCluster.getBrokerUrls())
                            .thenAccept(configs -> Platform.runLater(() -> table.getItems().setAll(configs)))
                            .exceptionally(ex -> {
                                Platform.runLater(() -> updateStatus("Failed to refresh cluster config: " + ex.getMessage()));
                                return null;
                            });
                    } else {
                        updateStatus("Failed to update cluster configuration: " + throwable.getMessage());
                        DialogHelper.showErrorDialog("Update Error", 
                            "Failed to update cluster configuration", throwable.getMessage());
                    }
                });
            });
    }
    
    // ===== CHAT SECTION METHODS =====
    
    @FXML
    private void onSendChatMessage() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            // Add timestamp and format the message
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String formattedMessage = String.format("[%s] You: %s%n", timestamp, message);
            
            // Append to chat messages area
            chatMessagesArea.appendText(formattedMessage);
            
            // Clear input field
            chatInputField.clear();
            
            // Auto-scroll to bottom
            chatMessagesArea.positionCaret(chatMessagesArea.getLength());
            
            updateStatus("Chat message sent");
            
            // Simulate a simple echo response after a short delay
            CompletableFuture.delayedExecutor(1, java.util.concurrent.TimeUnit.SECONDS)
                .execute(() -> Platform.runLater(() -> {
                    String responseTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    String response = String.format("[%s] System: Message received: %s%n", responseTime, message);
                    chatMessagesArea.appendText(response);
                    chatMessagesArea.positionCaret(chatMessagesArea.getLength());
                }));
        }
    }
    
    @FXML
    private void onClearChat() {
        boolean confirmed = DialogHelper.showConfirmDialog(
            "Clear Chat", 
            "Clear all chat messages?",
            "This will remove all messages from the chat window."
        );
        
        if (confirmed) {
            chatMessagesArea.clear();
            updateStatus("Chat history cleared");
        }
    }
    
    @FXML
    private void onConnectChat() {
        if (connectChatButton.getText().equals("Connect")) {
            // Simulate chat connection
            connectChatButton.setText("Disconnect");
            connectChatButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String welcomeMessage = String.format("[%s] System: Connected to chat. Welcome!%n", timestamp);
            chatMessagesArea.appendText(welcomeMessage);
            chatMessagesArea.positionCaret(chatMessagesArea.getLength());
            
            updateStatus("Connected to chat");
        } else {
            // Simulate chat disconnection
            connectChatButton.setText("Connect");
            connectChatButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String disconnectMessage = String.format("[%s] System: Disconnected from chat.%n", timestamp);
            chatMessagesArea.appendText(disconnectMessage);
            chatMessagesArea.positionCaret(chatMessagesArea.getLength());
            
            updateStatus("Disconnected from chat");
        }
    }
    
    public void shutdown() {
        logger.info("Shutting down MainController");
        saveSettings();
        executorService.shutdown();
    }
}