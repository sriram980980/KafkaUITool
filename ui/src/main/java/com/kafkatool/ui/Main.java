package com.kafkatool.ui;

/**
 * Main launcher class for JavaFX application
 * This class avoids module-path issues when running from a fat jar
 */
public class Main {
    public static void main(String[] args) {
        // Set system properties for JavaFX to work in fat jar
        System.setProperty("prism.forceGPU", "false");
        System.setProperty("prism.order", "sw");
        System.setProperty("javafx.animation.fullspeed", "true");
        
        // Launch the JavaFX application
        KafkaUIApplication.main(args);
    }
}