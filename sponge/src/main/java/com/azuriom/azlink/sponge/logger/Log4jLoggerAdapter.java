package com.azuriom.azlink.sponge.logger;

import com.azuriom.azlink.common.logger.LoggerAdapter;
import org.apache.logging.log4j.Logger;

public class Log4jLoggerAdapter implements LoggerAdapter {

    private final Logger logger;

    public Log4jLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        this.logger.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        this.logger.info(message, throwable);
    }

    @Override
    public void warn(String message) {
        this.logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        this.logger.warn(message, throwable);
    }

    @Override
    public void error(String message) {
        this.logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        this.logger.error(message, throwable);
    }
}
