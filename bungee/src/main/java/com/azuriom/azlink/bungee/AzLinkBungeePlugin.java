package com.azuriom.azlink.bungee;

import com.azuriom.azlink.bungee.command.BungeeCommandExecutor;
import com.azuriom.azlink.bungee.command.BungeeCommandSender;
import com.azuriom.azlink.bungee.integrations.NLoginIntegration;
import com.azuriom.azlink.bungee.integrations.SkinsRestorerIntegration;
import com.azuriom.azlink.common.AzLinkPlatform;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.logger.JavaLoggerAdapter;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.azuriom.azlink.common.platform.PlatformInfo;
import com.azuriom.azlink.common.platform.PlatformType;
import com.azuriom.azlink.common.scheduler.SchedulerAdapter;
import com.nickuc.login.api.nLoginAPI;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class AzLinkBungeePlugin extends Plugin implements AzLinkPlatform {

    private final SchedulerAdapter scheduler = new BungeeSchedulerAdapter(this);

    private AzLinkPlugin plugin;
    private LoggerAdapter loggerAdapter;
    private Configuration config;

    @Override
    public void onLoad() {
        this.loggerAdapter = new JavaLoggerAdapter(getLogger());
    }

    @Override
    public void onEnable() {
        this.plugin = new AzLinkPlugin(this);
        this.plugin.init();

        getProxy().getPluginManager().registerCommand(this, new BungeeCommandExecutor(this.plugin));

        loadConfig();

        if (this.config.getBoolean("nlogin-integration")
                && getProxy().getPluginManager().getPlugin("nLogin") != null) {
            if (nLoginAPI.getApi().getApiVersion() >= 5) {
                getProxy().getPluginManager().registerListener(this, new NLoginIntegration(this));
            } else {
                this.plugin.getLogger().warn("nLogin integration requires API version v5 or higher");
            }
        }

        if (this.config.getBoolean("skinsrestorer-integration")
                && getProxy().getPluginManager().getPlugin("SkinsRestorer") != null) {
            getProxy().getPluginManager().registerListener(this, new SkinsRestorerIntegration(this));
        }
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

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        try {
            saveResource(configFile.toPath(), "bungee-config.yml");

            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load configuration", e);
        }
    }
}
