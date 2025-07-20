package com.kafkatool.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Main JavaFX application class for Kafka UI Tool
 */
public class KafkaUIApplication extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaUIApplication.class);
    private static final String APP_TITLE = "Kafka UI Tool";
    private static final String MAIN_FXML = "/fxml/main.fxml";
    private static final String APP_ICON = "/images/kafka-icon.png";
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Kafka UI Tool application");
            
            // Load the main FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_FXML));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Load CSS stylesheet
            scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/css/application.css")).toExternalForm());
            
            // Configure the primary stage
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            
            // Set application icon
            try {
                primaryStage.getIcons().add(new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(APP_ICON))));
            } catch (Exception e) {
                logger.warn("Could not load application icon: {}", e.getMessage());
            }
            
            // Show the stage
            primaryStage.show();
            
            logger.info("Kafka UI Tool application started successfully");
            
        } catch (IOException e) {
            logger.error("Failed to start application", e);
            throw new RuntimeException("Failed to load application UI", e);
        }
    }
    
    @Override
    public void stop() {
        logger.info("Shutting down Kafka UI Tool application");
        // Cleanup resources here if needed
    }
    
    public static void main(String[] args) {
        // Set system properties for better cross-platform support
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        
        logger.info("Launching Kafka UI Tool with args: {}", String.join(" ", args));
        launch(args);
    }
}