package com.azuriom.azlink.common;

import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.data.PlatformData;
import com.azuriom.azlink.common.data.WorldData;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.azuriom.azlink.common.platform.PlatformInfo;
import com.azuriom.azlink.common.platform.PlatformType;
import com.azuriom.azlink.common.scheduler.SchedulerAdapter;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public interface AzLinkPlatform {

    AzLinkPlugin getPlugin();

    LoggerAdapter getLoggerAdapter();

    SchedulerAdapter getSchedulerAdapter();

    PlatformType getPlatformType();

    PlatformInfo getPlatformInfo();

    String getPluginVersion();

    Path getDataDirectory();

    Stream<CommandSender> getOnlinePlayers();

    int getMaxPlayers();

    default Optional<WorldData> getWorldData() {
        return Optional.empty();
    }

    void dispatchConsoleCommand(String command);

    default PlatformData getPlatformData() {
        return new PlatformData(getPlatformType(), getPlatformInfo());
    }

    default void prepareDataAsync() {
    }
}
