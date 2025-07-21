package com.kafkatool.ui;

import java.awt.GraphicsEnvironment;

/**
 * Main launcher class for JavaFX application
 * This class avoids module-path issues when running from a fat jar
 */
public class Main {
    public static void main(String[] args) {
        // Handle command line arguments before launching JavaFX
        if (args.length > 0) {
            String firstArg = args[0].toLowerCase();
            if (firstArg.equals("--help") || firstArg.equals("-h")) {
                printHelp();
                return;
            }
            if (firstArg.equals("--version") || firstArg.equals("-v")) {
                System.out.println("Kafka UI Tool v2.0.0");
                return;
            }
        }
        
        // Check if running in headless mode
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("ERROR: Cannot start GUI application in headless environment");
            System.err.println("This application requires a display to run the JavaFX interface.");
            System.err.println();
            System.err.println("For headless/server environments, use the service JAR instead:");
            System.err.println("  java -jar kafka-ui-service-2.0.0-jar-with-dependencies.jar --api-server");
            System.err.println();
            System.err.println("Use --help for more information.");
            System.exit(1);
        }
        
        // Check if DISPLAY is available on Unix systems
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux") || os.contains("unix")) {
            String display = System.getenv("DISPLAY");
            if (display == null || display.trim().isEmpty()) {
                System.err.println("ERROR: DISPLAY environment variable not set");
                System.err.println("This JavaFX application requires a graphical display.");
                System.err.println();
                System.err.println("Solutions:");
                System.err.println("1. Set DISPLAY environment variable (e.g., export DISPLAY=:0)");
                System.err.println("2. Use the service JAR for headless environments:");
                System.err.println("   java -jar kafka-ui-service-2.0.0-jar-with-dependencies.jar --api-server");
                System.exit(1);
            }
        }
        
        // Set system properties for JavaFX to work in fat jar
        System.setProperty("prism.forceGPU", "false");
        System.setProperty("prism.order", "sw");
        System.setProperty("javafx.animation.fullspeed", "true");
        
        try {
            // Launch the JavaFX application
            KafkaUIApplication.main(args);
        } catch (UnsupportedOperationException e) {
            if (e.getMessage() != null && e.getMessage().contains("DISPLAY")) {
                System.err.println("ERROR: Unable to open display for JavaFX GUI");
                System.err.println("This application requires a graphical environment to run.");
                System.err.println();
                System.err.println("For headless/server mode, use the service JAR instead:");
                System.err.println("  java -jar kafka-ui-service-2.0.0-jar-with-dependencies.jar --api-server");
                System.exit(1);
            } else {
                throw e; // Re-throw if it's a different issue
            }
        }
    }
    
    private static void printHelp() {
        System.out.println("Kafka UI Tool v2.0.0 - JavaFX Desktop Application");
        System.out.println();
        System.out.println("Usage: java -jar kafka-ui-application-2.0.0-jar-with-dependencies.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --help, -h     Show this help message");
        System.out.println("  --version, -v  Show version information");
        System.out.println();
        System.out.println("The application will launch a JavaFX desktop interface for managing Kafka clusters.");
        System.out.println("Make sure you have a display available (not available in headless environments).");
        System.out.println();
        System.out.println("For headless/server mode, use the service JAR instead:");
        System.out.println("  java -jar kafka-ui-service-2.0.0-jar-with-dependencies.jar --api-server");
    }
}