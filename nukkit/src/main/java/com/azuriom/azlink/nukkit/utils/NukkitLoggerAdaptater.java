package com.azuriom.azlink.nukkit.utils;

import cn.nukkit.utils.MainLogger;
import com.azuriom.azlink.common.logger.LoggerAdapter;

public class NukkitLoggerAdaptater implements LoggerAdapter {

    private MainLogger mainLogger;

    public NukkitLoggerAdaptater(MainLogger mainLogger) {
        this.mainLogger = mainLogger;
    }

    @Override
    public void info(String message) {
        mainLogger.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        mainLogger.info(message, throwable);
    }

    @Override
    public void warn(String message) {
        mainLogger.warning(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        mainLogger.warning(message, throwable);
    }

    @Override
    public void error(String message) {
        mainLogger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        mainLogger.error(message, throwable);
    }
}
