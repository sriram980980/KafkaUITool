package com.kafkatool.ui;

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
        
        // Set system properties for JavaFX to work in fat jar
        System.setProperty("prism.forceGPU", "false");
        System.setProperty("prism.order", "sw");
        System.setProperty("javafx.animation.fullspeed", "true");
        
        // Launch the JavaFX application
        KafkaUIApplication.main(args);
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