package com.anyscreen.services;

import com.anyscreen.implementations.Log4jAdapter;
import com.anyscreen.interfaces.LoggerInterface;

/**
 * Singleton logger service that provides application-wide logging functionality.
 * Supports dependency injection for different logger implementations while maintaining
 * a singleton pattern for easy access throughout the application.
 */
public class LoggerService {
    
    private static LoggerService instance;
    
    private final LoggerInterface logger;

    /**
     * Private constructor to enforce singleton pattern.
     * @param logger The logger implementation to use
     */
    private LoggerService(LoggerInterface logger) {
        this.logger = logger;
    }
    
    /**
     * Initializes the singleton instance with a specific logger implementation.
     * This method should be called once during application startup.
     * @param logger The logger implementation to use
     * @throws IllegalStateException if the logger service has already been initialized
     */
    public static void initialize(LoggerInterface logger) {
        if (instance != null) {
            throw new IllegalStateException("LoggerService has already been initialized");
        }
        instance = new LoggerService(logger);
    }
    
    /**
     * Initializes the singleton instance with the default Log4j adapter.
     * This method should be called once during application startup.
     * @throws IllegalStateException if the logger service has already been initialized
     */
    public static synchronized void initializeDefault() {
        initialize(new Log4jAdapter());
    }
    
    /**
     * @return LoggerService instance
     * @throws IllegalStateException if the logger service has not been initialized
     */
    /**
     * Gets the singleton instance - fails fast if not initialized
     */
    public static LoggerService getInstance() {
        LoggerService result = instance;
        if (result == null) {
            initializeDefault();
        }
        result = instance;
        return result;
    }
    
    
    /**
     * Gets the logger instance for application logging.
     * @return LoggerInterface instance
     */
    public static LoggerInterface getLogger() {
        return getInstance().logger;
    }

    // Convenience methods for common log levels
    public static void info(String message) {
        getLogger().info(message);
    }

    public static void debug(String message) {
        getLogger().debug(message);
    }

    public static void error(String message) {
        getLogger().error(message);
    }

    public static void warn(String message) {
        getLogger().warn(message);
    }
    
    /**
     * Resets the singleton instance. Useful for testing.
     * Should not be used in production code.
     */
    public static synchronized void reset() {
        instance = null;
    }
}
