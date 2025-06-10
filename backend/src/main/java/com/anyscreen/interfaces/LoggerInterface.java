package com.anyscreen.interfaces;

public interface LoggerInterface {

    /**
     * Logs a message at the INFO level.
     * @param message The message to log
     */
    void info(String message);

    /**
     * Logs a message at the DEBUG level.
     * @param message The message to log
     */
    void debug(String message);

    /**
     * Logs a message at the ERROR level.
     * @param message The message to log
     */
    void error(String message);

    /**
     * Logs a message at the WARN level.
     * @param message The message to log
     */
    void warn(String message);
}
