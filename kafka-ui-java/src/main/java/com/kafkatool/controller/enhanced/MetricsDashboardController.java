package com.kafkatool.controller.enhanced;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import com.kafkatool.model.*;
import com.kafkatool.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for monitoring and metrics dashboard
 */
public class MetricsDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsDashboardController.class);
    
    @FXML private TextField brokerUrlsField;
    @FXML private Button connectButton;
    @FXML private TabPane metricsTabPane;
    
    // Cluster metrics tab
    @FXML private LineChart<String, Number> clusterThroughputChart;
    @FXML private LineChart<String, Number> clusterLatencyChart;
    @FXML private TableView<MetricsInfo> clusterMetricsTable;
    @FXML private TableColumn<MetricsInfo, String> metricNameColumn;
    @FXML private TableColumn<MetricsInfo, Number> metricValueColumn;
    @FXML private TableColumn<MetricsInfo, String> metricUnitColumn;
    @FXML private TableColumn<MetricsInfo, String> metricCategoryColumn;
    
    // Topic metrics tab
    @FXML private ComboBox<String> topicSelector;
    @FXML private LineChart<String, Number> topicThroughputChart;
    @FXML private LineChart<String, Number> topicSizeChart;
    @FXML private TableView<MetricsInfo> topicMetricsTable;
    
    // Consumer group metrics tab
    @FXML private ComboBox<String> consumerGroupSelector;
    @FXML private LineChart<String, Number> consumerLagChart;
    @FXML private TableView<MetricsInfo> consumerMetricsTable;
    
    // Broker metrics tab
    @FXML private ComboBox<Integer> brokerSelector;
    @FXML private LineChart<String, Number> brokerCpuChart;
    @FXML private LineChart<String, Number> brokerMemoryChart;
    @FXML private TableView<MetricsInfo> brokerMetricsTable;
    
    @FXML private Label statusLabel;
    @FXML private Button refreshButton;
    @FXML private ComboBox<String> refreshIntervalCombo;
    
    private EnhancedKafkaService kafkaService;
    private ObservableList<MetricsInfo> clusterMetrics = FXCollections.observableArrayList();
    private ObservableList<MetricsInfo> topicMetrics = FXCollections.observableArrayList();
    private ObservableList<MetricsInfo> consumerMetrics = FXCollections.observableArrayList();
    private ObservableList<MetricsInfo> brokerMetrics = FXCollections.observableArrayList();
    private ObservableList<String> topics = FXCollections.observableArrayList();
    private ObservableList<String> consumerGroups = FXCollections.observableArrayList();
    private ObservableList<Integer> brokers = FXCollections.observableArrayList();
    
    // Chart data series
    private XYChart.Series<String, Number> throughputSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> latencySeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> topicThroughputSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> topicSizeSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> lagSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> cpuSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> memorySeries = new XYChart.Series<>();
    
    public void initialize() {
        setupUI();
        setupEventHandlers();
        setupCharts();
    }
    
    private void setupUI() {
        // Setup table columns
        setupMetricsTableColumns(clusterMetricsTable, metricNameColumn, metricValueColumn, metricUnitColumn, metricCategoryColumn);
        setupMetricsTableColumns(topicMetricsTable, null, null, null, null);
        setupMetricsTableColumns(consumerMetricsTable, null, null, null, null);
        setupMetricsTableColumns(brokerMetricsTable, null, null, null, null);
        
        // Setup table data
        clusterMetricsTable.setItems(clusterMetrics);
        topicMetricsTable.setItems(topicMetrics);
        consumerMetricsTable.setItems(consumerMetrics);
        brokerMetricsTable.setItems(brokerMetrics);
        
        // Setup combo boxes
        topicSelector.setItems(topics);
        consumerGroupSelector.setItems(consumerGroups);
        brokerSelector.setItems(brokers);
        
        refreshIntervalCombo.setItems(FXCollections.observableArrayList(
            "5 seconds", "10 seconds", "30 seconds", "1 minute", "5 minutes"
        ));
        refreshIntervalCombo.setValue("30 seconds");
        
        statusLabel.setText("Not connected");
        metricsTabPane.setDisable(true);
    }
    
    private void setupMetricsTableColumns(TableView<MetricsInfo> table, 
                                        TableColumn<MetricsInfo, String> nameCol,
                                        TableColumn<MetricsInfo, Number> valueCol,
                                        TableColumn<MetricsInfo, String> unitCol,
                                        TableColumn<MetricsInfo, String> categoryCol) {
        if (table.getColumns().isEmpty()) {
            TableColumn<MetricsInfo, String> name = new TableColumn<>("Metric");
            name.setCellValueFactory(cellData -> cellData.getValue().metricNameProperty());
            
            TableColumn<MetricsInfo, Number> value = new TableColumn<>("Value");
            value.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
            
            TableColumn<MetricsInfo, String> unit = new TableColumn<>("Unit");
            unit.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
            
            TableColumn<MetricsInfo, String> category = new TableColumn<>("Category");
            category.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
            
            table.getColumns().addAll(name, value, unit, category);
        }
    }
    
    private void setupEventHandlers() {
        connectButton.setOnAction(e -> connectToCluster());
        refreshButton.setOnAction(e -> refreshAllMetrics());
        topicSelector.setOnAction(e -> loadTopicMetrics());
        consumerGroupSelector.setOnAction(e -> loadConsumerGroupMetrics());
        brokerSelector.setOnAction(e -> loadBrokerMetrics());
    }
    
    private void setupCharts() {
        // Setup chart series
        throughputSeries.setName("Messages/sec");
        latencySeries.setName("Latency (ms)");
        topicThroughputSeries.setName("Topic Messages/sec");
        topicSizeSeries.setName("Topic Size (MB)");
        lagSeries.setName("Consumer Lag");
        cpuSeries.setName("CPU Usage (%)");
        memorySeries.setName("Memory Usage (MB)");
        
        // Add series to charts
        clusterThroughputChart.getData().add(throughputSeries);
        clusterLatencyChart.getData().add(latencySeries);
        topicThroughputChart.getData().add(topicThroughputSeries);
        topicSizeChart.getData().add(topicSizeSeries);
        consumerLagChart.getData().add(lagSeries);
        brokerCpuChart.getData().add(cpuSeries);
        brokerMemoryChart.getData().add(memorySeries);
        
        // Configure chart appearance
        configureChart(clusterThroughputChart, "Cluster Throughput");
        configureChart(clusterLatencyChart, "Cluster Latency");
        configureChart(topicThroughputChart, "Topic Throughput");
        configureChart(topicSizeChart, "Topic Size");
        configureChart(consumerLagChart, "Consumer Lag");
        configureChart(brokerCpuChart, "Broker CPU");
        configureChart(brokerMemoryChart, "Broker Memory");
    }
    
    private void configureChart(LineChart<String, Number> chart, String title) {
        chart.setTitle(title);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.getXAxis().setAutoRanging(true);
        chart.getYAxis().setAutoRanging(true);
    }
    
    private void connectToCluster() {
        String brokerUrls = brokerUrlsField.getText().trim();
        if (brokerUrls.isEmpty()) {
            showError("Please enter broker URLs");
            return;
        }
        
        statusLabel.setText("Connecting");
        connectButton.setDisable(true);
        
        kafkaService.testConnectionAsync(brokerUrls)
            .thenAccept(connected -> {
                if (connected) {
                    statusLabel.setText("Connected");
                    metricsTabPane.setDisable(false);
                    loadInitialData();
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
    
    private void loadInitialData() {
        String brokerUrls = brokerUrlsField.getText().trim();
        
        // Load topics
        kafkaService.getTopicsAsync(brokerUrls)
            .thenAccept(topicList -> {
                topics.clear();
                topicList.forEach(topic -> topics.add(topic.getName()));
            });
        
        // Load consumer groups
        kafkaService.getConsumerGroupsAsync(brokerUrls)
            .thenAccept(groupList -> {
                consumerGroups.clear();
                groupList.forEach(group -> consumerGroups.add(group.getGroupId()));
            });
        
        // Load brokers
        kafkaService.getBrokersAsync(brokerUrls)
            .thenAccept(brokerList -> {
                brokers.clear();
                brokerList.forEach(broker -> brokers.add(broker.getId()));
            });
        
        // Load initial metrics
        refreshAllMetrics();
    }
    
    private void refreshAllMetrics() {
        loadClusterMetrics();
        if (topicSelector.getValue() != null) {
            loadTopicMetrics();
        }
        if (consumerGroupSelector.getValue() != null) {
            loadConsumerGroupMetrics();
        }
        if (brokerSelector.getValue() != null) {
            loadBrokerMetrics();
        }
    }
    
    private void loadClusterMetrics() {
        String brokerUrls = brokerUrlsField.getText().trim();
        
        kafkaService.getClusterMetricsAsync(brokerUrls)
            .thenAccept(metrics -> {
                clusterMetrics.clear();
                clusterMetrics.addAll(metrics);
                
                // Update charts with latest data
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                
                // Find specific metrics for charts
                metrics.stream()
                    .filter(m -> "messages_per_sec".equals(m.getMetricName()))
                    .findFirst()
                    .ifPresent(m -> {
                        throughputSeries.getData().add(new XYChart.Data<>(timestamp, m.getValue()));
                        // Keep only last 20 data points
                        if (throughputSeries.getData().size() > 20) {
                            throughputSeries.getData().remove(0);
                        }
                    });
                
                metrics.stream()
                    .filter(m -> "avg_latency_ms".equals(m.getMetricName()))
                    .findFirst()
                    .ifPresent(m -> {
                        latencySeries.getData().add(new XYChart.Data<>(timestamp, m.getValue()));
                        if (latencySeries.getData().size() > 20) {
                            latencySeries.getData().remove(0);
                        }
                    });
            })
            .exceptionally(throwable -> {
                logger.error("Failed to load cluster metrics", throwable);
                return null;
            });
    }
    
    private void loadTopicMetrics() {
        String selectedTopic = topicSelector.getValue();
        if (selectedTopic == null) return;
        
        String brokerUrls = brokerUrlsField.getText().trim();
        
        kafkaService.getTopicMetricsAsync(brokerUrls, selectedTopic)
            .thenAccept(metrics -> {
                topicMetrics.clear();
                topicMetrics.addAll(metrics);
                
                // Update topic charts
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                
                metrics.stream()
                    .filter(m -> "topic_messages_per_sec".equals(m.getMetricName()))
                    .findFirst()
                    .ifPresent(m -> {
                        topicThroughputSeries.getData().add(new XYChart.Data<>(timestamp, m.getValue()));
                        if (topicThroughputSeries.getData().size() > 20) {
                            topicThroughputSeries.getData().remove(0);
                        }
                    });
                
                metrics.stream()
                    .filter(m -> "topic_size_mb".equals(m.getMetricName()))
                    .findFirst()
                    .ifPresent(m -> {
                        topicSizeSeries.getData().add(new XYChart.Data<>(timestamp, m.getValue()));
                        if (topicSizeSeries.getData().size() > 20) {
                            topicSizeSeries.getData().remove(0);
                        }
                    });
            })
            .exceptionally(throwable -> {
                logger.error("Failed to load topic metrics", throwable);
                return null;
            });
    }
    
    private void loadConsumerGroupMetrics() {
        String selectedGroup = consumerGroupSelector.getValue();
        if (selectedGroup == null) return;
        
        String brokerUrls = brokerUrlsField.getText().trim();
        
        kafkaService.getConsumerGroupLagAsync(brokerUrls, selectedGroup)
            .thenAccept(metrics -> {
                consumerMetrics.clear();
                consumerMetrics.addAll(metrics);
                
                // Update consumer lag chart
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                
                double totalLag = metrics.stream()
                    .filter(m -> "consumer_lag".equals(m.getMetricName()))
                    .mapToDouble(MetricsInfo::getValue)
                    .sum();
                
                lagSeries.getData().add(new XYChart.Data<>(timestamp, totalLag));
                if (lagSeries.getData().size() > 20) {
                    lagSeries.getData().remove(0);
                }
            })
            .exceptionally(throwable -> {
                logger.error("Failed to load consumer group metrics", throwable);
                return null;
            });
    }
    
    private void loadBrokerMetrics() {
        Integer selectedBroker = brokerSelector.getValue();
        if (selectedBroker == null) return;
        
        String brokerUrls = brokerUrlsField.getText().trim();
        
        kafkaService.getBrokerMetricsAsync(brokerUrls, selectedBroker)
            .thenAccept(metrics -> {
                brokerMetrics.clear();
                brokerMetrics.addAll(metrics);
                
                // Update broker charts
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                
                metrics.stream()
                    .filter(m -> "cpu_usage_percent".equals(m.getMetricName()))
                    .findFirst()
                    .ifPresent(m -> {
                        cpuSeries.getData().add(new XYChart.Data<>(timestamp, m.getValue()));
                        if (cpuSeries.getData().size() > 20) {
                            cpuSeries.getData().remove(0);
                        }
                    });
                
                metrics.stream()
                    .filter(m -> "memory_usage_mb".equals(m.getMetricName()))
                    .findFirst()
                    .ifPresent(m -> {
                        memorySeries.getData().add(new XYChart.Data<>(timestamp, m.getValue()));
                        if (memorySeries.getData().size() > 20) {
                            memorySeries.getData().remove(0);
                        }
                    });
            })
            .exceptionally(throwable -> {
                logger.error("Failed to load broker metrics", throwable);
                return null;
            });
    }
    
    public void setKafkaService(EnhancedKafkaService service) {
        this.kafkaService = service;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}