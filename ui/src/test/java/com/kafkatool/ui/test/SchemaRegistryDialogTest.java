package com.kafkatool.ui.test;

import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.ui.DialogHelper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Test application to demonstrate the new Schema Registry dialog functionality
 */
public class SchemaRegistryDialogTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Schema Registry Dialog Test");
        
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20;");
        
        Label title = new Label("Schema Registry Dialog Test");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button testNewClusterButton = new Button("Test New Cluster Dialog");
        testNewClusterButton.setOnAction(e -> {
            DialogHelper.showAddClusterDialog().ifPresent(cluster -> {
                showClusterInfo(cluster);
            });
        });
        
        Button testEditClusterButton = new Button("Test Edit Cluster Dialog (with Schema Registry)");
        testEditClusterButton.setOnAction(e -> {
            // Create a sample cluster with Schema Registry enabled for testing
            AuthenticationConfig schemaAuth = new AuthenticationConfig("schema-user", "schema-pass");
            ClusterInfo sampleCluster = new ClusterInfo(
                "Sample Cluster", "localhost:9092", false,
                AuthenticationType.NONE, null,
                true, "http://localhost:8081",
                AuthenticationType.SASL_PLAIN, schemaAuth
            );
            
            DialogHelper.showEditClusterDialog(sampleCluster).ifPresent(cluster -> {
                showClusterInfo(cluster);
            });
        });
        
        root.getChildren().addAll(title, testNewClusterButton, testEditClusterButton);
        
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void showClusterInfo(ClusterInfo cluster) {
        StringBuilder info = new StringBuilder();
        info.append("Cluster: ").append(cluster.getName()).append("\n");
        info.append("Brokers: ").append(cluster.getBrokerUrls()).append("\n");
        info.append("Auth Type: ").append(cluster.getAuthenticationType()).append("\n");
        info.append("Schema Registry Enabled: ").append(cluster.isSchemaRegistryEnabled()).append("\n");
        if (cluster.isSchemaRegistryEnabled()) {
            info.append("Schema Registry URL: ").append(cluster.getSchemaRegistryUrl()).append("\n");
            info.append("Schema Registry Auth Type: ").append(cluster.getSchemaRegistryAuthType()).append("\n");
            info.append("Schema Registry Auth Required: ").append(cluster.requiresSchemaRegistryAuthentication()).append("\n");
        }
        
        DialogHelper.showInfoDialog("Cluster Configuration", info.toString());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}