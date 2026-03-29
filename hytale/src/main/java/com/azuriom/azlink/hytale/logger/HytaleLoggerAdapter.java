package com.azuriom.azlink.hytale.logger;

import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.hypixel.hytale.logger.HytaleLogger;

public class HytaleLoggerAdapter implements LoggerAdapter {

    private final HytaleLogger logger;

    public HytaleLoggerAdapter(HytaleLogger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        this.logger.atInfo().log(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        this.logger.atInfo().withCause(throwable).log(message);
    }

    @Override
    public void warn(String message) {
        this.logger.atWarning().log(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        this.logger.atWarning().withCause(throwable).log(message);
    }

    @Override
    public void error(String message) {
        this.logger.atSevere().log(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        this.logger.atSevere().withCause(throwable).log(message);
    }
}
