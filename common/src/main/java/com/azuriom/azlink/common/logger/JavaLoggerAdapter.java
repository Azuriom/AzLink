package com.azuriom.azlink.common.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaLoggerAdapter implements LoggerAdapter {

    private final Logger logger;

    public JavaLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        this.logger.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        this.logger.log(Level.INFO, message, throwable);
    }

    @Override
    public void warn(String message) {
        this.logger.warning(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        this.logger.log(Level.WARNING, message, throwable);
    }

    @Override
    public void error(String message) {
        this.logger.severe(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        this.logger.log(Level.SEVERE, message, throwable);
    }
}
