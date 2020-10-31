package com.azuriom.azlink.velocity;

import com.azuriom.azlink.common.AzLinkPlatform;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.azuriom.azlink.common.logger.Slf4jLoggerAdapter;
import com.azuriom.azlink.common.platform.PlatformInfo;
import com.azuriom.azlink.common.platform.PlatformType;
import com.azuriom.azlink.velocity.command.VelocityCommandExecutor;
import com.azuriom.azlink.velocity.command.VelocityCommandSender;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.ProxyVersion;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.stream.Stream;

@Plugin(
        id = "azlink",
        name = "AzLink",
        version = "${pluginVersion}",
        description = "The plugin to link your Azuriom website with your server.",
        url = "https://azuriom.com",
        authors = "Azuriom Team"
)
public final class AzLinkVelocityPlugin implements AzLinkPlatform {

    private final ProxyServer server;

    private final Path dataDirectory;

    private final LoggerAdapter logger;

    private AzLinkPlugin plugin;

    @Inject
    public AzLinkVelocityPlugin(ProxyServer server, @DataDirectory Path dataDirectory, Logger logger) {
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.logger = new Slf4jLoggerAdapter(logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            Class.forName("com.velocitypowered.api.command.SimpleCommand");
        } catch (ClassNotFoundException e) {
            logger.error("AzLink requires Velocity 1.1.0 or higher");
            logger.error("You can download the latest version of Velocity on https://velocitypowered.com/downloads");
            return;
        }

        plugin = new AzLinkPlugin(this);
        plugin.init();

        server.getCommandManager().register("azlink", new VelocityCommandExecutor(plugin), "azuriomlink");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        plugin.shutdown();
    }

    @Override
    public AzLinkPlugin getPlugin() {
        return plugin;
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return logger;
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.VELOCITY;
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        ProxyVersion version = server.getVersion();

        return new PlatformInfo(version.getName(), version.getVersion());
    }

    @Override
    public String getPluginVersion() {
        return "${pluginVersion}";
    }

    @Override
    public Path getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        return server.getAllPlayers().stream().map(VelocityCommandSender::new);
    }

    @Override
    public int getMaxPlayers() {
        return server.getConfiguration().getShowMaxPlayers();
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), command);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        server.getScheduler().buildTask(this, runnable).schedule();
    }
}
