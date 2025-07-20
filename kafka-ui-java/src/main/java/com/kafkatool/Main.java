package com.kafkatool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified main entry point for Kafka UI Tool
 * This class determines whether to launch the GUI application or the REST API server
 * based on command line arguments, decoupling the API server from JavaFX requirements.
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("Kafka UI Tool starting with args: {}", String.join(" ", args));
        
        // Check if this is an API server launch
        if (isApiServerMode(args)) {
            logger.info("Starting in API server mode");
            startApiServer(args);
        } else if (isCliMode(args)) {
            logger.info("Starting in CLI mode");
            startCli(args);
        } else {
            logger.info("Starting in GUI mode");
            startGui(args);
        }
    }
    
    /**
     * Determines if the application should start in API server mode
     */
    private static boolean isApiServerMode(String[] args) {
        for (String arg : args) {
            if ("--api-server".equals(arg)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines if the application should start in CLI mode
     */
    private static boolean isCliMode(String[] args) {
        for (String arg : args) {
            if ("topic".equals(arg) || "cluster".equals(arg) || "consumer".equals(arg) || 
                "producer".equals(arg) || "--help".equals(arg) || "-h".equals(arg)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Starts the REST API server without JavaFX dependencies
     */
    private static void startApiServer(String[] args) {
        try {
            // Extract API server specific arguments
            String[] apiArgs = extractApiServerArgs(args);
            RestApiMain.main(apiArgs);
        } catch (Exception e) {
            logger.error("Failed to start API server", e);
            System.err.println("ERROR: Failed to start API server: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Starts the CLI interface
     */
    private static void startCli(String[] args) {
        try {
            // For now, delegate to RestApiMain for CLI functionality
            // This can be extended with a dedicated CLI class later
            logger.info("CLI mode not fully implemented yet. Starting API server instead.");
            System.out.println("CLI mode is not fully implemented yet.");
            System.out.println("Use --api-server to start the REST API server, or run without arguments for GUI.");
            System.exit(0);
        } catch (Exception e) {
            logger.error("Failed to start CLI", e);
            System.err.println("ERROR: Failed to start CLI: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Starts the JavaFX GUI application
     */
    private static void startGui(String[] args) {
        try {
            // Check if JavaFX is available
            try {
                Class.forName("javafx.application.Application");
            } catch (ClassNotFoundException e) {
                logger.error("JavaFX not found in classpath");
                System.err.println("ERROR: JavaFX is required for GUI mode but not found in classpath.");
                System.err.println("To run in API server mode, use: java -jar <jarfile> --api-server");
                System.err.println("To install JavaFX, visit: https://openjfx.io/");
                System.exit(1);
            }
            
            // Launch JavaFX application
            KafkaUIApplication.main(args);
        } catch (Exception e) {
            logger.error("Failed to start GUI application", e);
            System.err.println("ERROR: Failed to start GUI application: " + e.getMessage());
            System.err.println("To run in API server mode, use: java -jar <jarfile> --api-server");
            System.exit(1);
        }
    }
    
    /**
     * Extracts arguments relevant to the API server
     */
    private static String[] extractApiServerArgs(String[] args) {
        java.util.List<String> apiArgs = new java.util.ArrayList<>();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--port".equals(arg) && i + 1 < args.length) {
                apiArgs.add(arg);
                apiArgs.add(args[++i]);
            } else if (arg.startsWith("--port=")) {
                apiArgs.add("--port");
                apiArgs.add(arg.substring("--port=".length()));
            }
            // Skip --api-server as it's not needed by RestApiMain
        }
        
        return apiArgs.toArray(new String[0]);
    }
}
