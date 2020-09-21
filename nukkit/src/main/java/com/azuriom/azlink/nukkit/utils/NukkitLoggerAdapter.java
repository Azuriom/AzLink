package com.azuriom.azlink.nukkit.utils;

import cn.nukkit.utils.Logger;
import com.azuriom.azlink.common.logger.LoggerAdapter;

public class NukkitLoggerAdapter implements LoggerAdapter {

    private final Logger logger;

    public NukkitLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.warning(message, throwable);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
