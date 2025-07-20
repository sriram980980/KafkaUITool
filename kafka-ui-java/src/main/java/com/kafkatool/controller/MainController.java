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
    
    // FXML Controls - Right Panel
    @FXML private ComboBox<Integer> partitionComboBox;
    @FXML private TextField fromOffsetField;
    @FXML private TextField toOffsetField;
    @FXML private Button loadMessagesButton;
    @FXML private Button loadLatestButton;
    
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
                    String displayValue = value.length() > 100 ? value.substring(0, 97) + "..." : value;
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
            } else if ("Connecting...".equals(status)) {
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
        updateConnectionStatus("Connecting...", false);
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
                updateStatus("Creating topic: " + topicConfig.getName());
                
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
                updateStatus("Deleting topic: " + currentTopic.getName());
                
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
            updateStatus("Refreshing topics...");
            
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
            showLoading(true);
            updateStatus("Loading topic configuration...");
            
            kafkaService.getTopicConfigAsync(currentCluster.getBrokerUrls(), currentTopic.getName())
                .whenComplete((config, throwable) -> {
                    Platform.runLater(() -> {
                        showLoading(false);
                        if (throwable == null) {
                            DialogHelper.showTopicConfigDialog(currentTopic.getName(), config);
                            updateStatus("Topic configuration loaded");
                        } else {
                            updateStatus("Failed to load topic configuration: " + throwable.getMessage());
                            DialogHelper.showErrorDialog("Config Load Error",
                                "Failed to load topic configuration", throwable.getMessage());
                        }
                    });
                });
        }
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
                updateStatus("Loading messages...");
                
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
            updateStatus("Loading latest messages...");
            
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
                updateStatus("Producing message...");
                
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
        if (currentTopic != null && currentCluster != null && partitionComboBox.getValue() != null) {
            DialogHelper.showSearchMessagesDialog().ifPresent(searchCriteria -> {
                showLoading(true);
                updateStatus("Searching messages...");
                
                kafkaService.searchMessagesAsync(
                    currentCluster.getBrokerUrls(),
                    currentTopic.getName(),
                    partitionComboBox.getValue(),
                    searchCriteria.getSearchPattern(),
                    searchCriteria.isSearchInKey(),
                    searchCriteria.isSearchInValue(),
                    searchCriteria.getMaxResults()
                ).whenComplete((messageList, throwable) -> {
                    Platform.runLater(() -> {
                        showLoading(false);
                        if (throwable == null) {
                            messages.setAll(messageList);
                            updateStatus("Search completed: " + messageList.size() + " messages found");
                        } else {
                            updateStatus("Search failed: " + throwable.getMessage());
                            DialogHelper.showErrorDialog("Search Error",
                                "Failed to search messages", throwable.getMessage());
                        }
                    });
                });
            });
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
        dialog.setHeaderText("Consumer Groups in Cluster");
        
        TableView<ConsumerGroupInfo> table = new TableView<>();
        
        TableColumn<ConsumerGroupInfo, String> groupIdCol = new TableColumn<>("Group ID");
        groupIdCol.setCellValueFactory(new PropertyValueFactory<>("groupId"));
        groupIdCol.setPrefWidth(200);
        
        TableColumn<ConsumerGroupInfo, String> stateCol = new TableColumn<>("State");
        stateCol.setCellValueFactory(new PropertyValueFactory<>("state"));
        stateCol.setPrefWidth(100);
        
        TableColumn<ConsumerGroupInfo, Integer> membersCol = new TableColumn<>("Members");
        membersCol.setCellValueFactory(new PropertyValueFactory<>("memberCount"));
        membersCol.setPrefWidth(80);
        
        table.getColumns().addAll(groupIdCol, stateCol, membersCol);
        table.setPrefHeight(400);
        
        // Load data
        kafkaService.getConsumerGroupsAsync(currentCluster.getBrokerUrls())
            .thenAccept(groups -> Platform.runLater(() -> table.getItems().setAll(groups)))
            .exceptionally(ex -> {
                Platform.runLater(() -> DialogHelper.showErrorDialog("Load Error", "Failed to load consumer groups", ex.getMessage()));
                return null;
            });
        
        dialog.getDialogPane().setContent(table);
        dialog.getDialogPane().setPrefSize(500, 500);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showSimpleBrokersDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Brokers - " + currentCluster.getName());
        dialog.setHeaderText("Cluster Brokers");
        
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
        
        table.getColumns().addAll(idCol, hostCol, portCol, controllerCol);
        table.setPrefHeight(300);
        
        // Load data
        kafkaService.getBrokersAsync(currentCluster.getBrokerUrls())
            .thenAccept(brokers -> Platform.runLater(() -> table.getItems().setAll(brokers)))
            .exceptionally(ex -> {
                Platform.runLater(() -> DialogHelper.showErrorDialog("Load Error", "Failed to load brokers", ex.getMessage()));
                return null;
            });
        
        dialog.getDialogPane().setContent(table);
        dialog.getDialogPane().setPrefSize(400, 400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }
    
    private void showSimpleClusterConfigDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Cluster Configuration - " + currentCluster.getName());
        dialog.setHeaderText("Cluster Configuration Properties");
        
        TableView<ClusterConfig> table = new TableView<>();
        
        TableColumn<ClusterConfig, String> nameCol = new TableColumn<>("Property");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);
        
        TableColumn<ClusterConfig, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(200);
        
        table.getColumns().addAll(nameCol, valueCol);
        table.setPrefHeight(500);
        
        // Load data
        kafkaService.getClusterConfigAsync(currentCluster.getBrokerUrls())
            .thenAccept(configs -> Platform.runLater(() -> table.getItems().setAll(configs)))
            .exceptionally(ex -> {
                Platform.runLater(() -> DialogHelper.showErrorDialog("Load Error", "Failed to load cluster config", ex.getMessage()));
                return null;
            });
        
        dialog.getDialogPane().setContent(table);
        dialog.getDialogPane().setPrefSize(500, 600);
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
        headersTextArea.setText(message.getHeaders() != null ? message.getHeaders() : "No headers");
    }
    
    public void shutdown() {
        logger.info("Shutting down MainController");
        saveSettings();
        executorService.shutdown();
    }
}