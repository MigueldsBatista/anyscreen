package com.anyscreen.implementations;

import com.anyscreen.interfaces.LoggerInterface;

public class Log4jAdapter implements LoggerInterface {

    private final org.apache.logging.log4j.Logger logger;

    public Log4jAdapter() {
        this.logger = org.apache.logging.log4j.LogManager.getLogger(Log4jAdapter.class);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void warn(String message) {
        logger.warn(message);
    }

}
