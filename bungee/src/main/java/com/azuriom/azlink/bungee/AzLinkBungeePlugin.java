package com.azuriom.azlink.bungee;

import com.azuriom.azlink.bungee.command.BungeeCommandExecutor;
import com.azuriom.azlink.bungee.command.BungeeCommandSender;
import com.azuriom.azlink.common.AzLinkPlatform;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.logger.JavaLoggerAdapter;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.azuriom.azlink.common.platform.PlatformInfo;
import com.azuriom.azlink.common.platform.PlatformType;
import com.azuriom.azlink.common.scheduler.SchedulerAdapter;
import net.md_5.bungee.api.plugin.Plugin;

import java.nio.file.Path;
import java.util.stream.Stream;

public final class AzLinkBungeePlugin extends Plugin implements AzLinkPlatform {

    private final SchedulerAdapter scheduler = new BungeeSchedulerAdapter(this);

    private AzLinkPlugin plugin;
    private LoggerAdapter loggerAdapter;

    @Override
    public void onLoad() {
        this.loggerAdapter = new JavaLoggerAdapter(getLogger());
    }

    @Override
    public void onEnable() {
        this.plugin = new AzLinkPlugin(this);
        this.plugin.init();

        getProxy().getPluginManager().registerCommand(this, new BungeeCommandExecutor(this.plugin));
    }

    @Override
    public void onDisable() {
        if (this.plugin != null) {
            this.plugin.shutdown();
        }
    }

    @Override
    public AzLinkPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return this.loggerAdapter;
    }

    @Override
    public SchedulerAdapter getSchedulerAdapter() {
        return this.scheduler;
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.BUNGEE;
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new PlatformInfo(getProxy().getName(), getProxy().getVersion());
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
}
