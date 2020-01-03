package com.azuriom.azlink.bungee;

import com.azuriom.azlink.bungee.command.BungeeCommandExecutor;
import com.azuriom.azlink.bungee.command.BungeeCommandSender;
import com.azuriom.azlink.common.AzLinkPlatform;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.PlatformType;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.logger.JulLoggerAdapter;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import net.md_5.bungee.api.plugin.Plugin;

import java.nio.file.Path;
import java.util.stream.Stream;

public final class AzLinkBungeePlugin extends Plugin implements AzLinkPlatform {

    private final AzLinkPlugin plugin = new AzLinkPlugin(this);

    private LoggerAdapter loggerAdapter;

    @Override
    public void onLoad() {
        loggerAdapter = new JulLoggerAdapter(getLogger());
    }

    @Override
    public void onEnable() {
        plugin.init();

        getProxy().getPluginManager().registerCommand(this, new BungeeCommandExecutor(plugin));
    }

    @Override
    public void onDisable() {
        plugin.shutdown();
    }

    @Override
    public AzLinkPlugin getPlugin() {
        return plugin;
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return loggerAdapter;
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.BUNGEE;
    }

    @Override
    public String getPlatformName() {
        return getProxy().getName();
    }

    @Override
    public String getPlatformVersion() {
        return getProxy().getVersion();
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public Path getDataDirectory() {
        return getDataFolder().toPath();
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        return getProxy().getPlayers().stream().map(BungeeCommandSender::new);
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), command);
    }

    @Override
    public int getMaxPlayers() {
        return getProxy().getConfig().getPlayerLimit();
    }

    @Override
    public void executeAsync(Runnable runnable) {
        getProxy().getScheduler().runAsync(this, runnable);
    }
}
