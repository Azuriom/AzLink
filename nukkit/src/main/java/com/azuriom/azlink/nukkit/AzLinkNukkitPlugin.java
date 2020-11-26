package com.azuriom.azlink.nukkit;

import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.plugin.PluginBase;
import com.azuriom.azlink.common.AzLinkPlatform;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.data.WorldData;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.azuriom.azlink.common.platform.PlatformInfo;
import com.azuriom.azlink.common.platform.PlatformType;
import com.azuriom.azlink.common.scheduler.JavaSchedulerAdapter;
import com.azuriom.azlink.common.scheduler.SchedulerAdapter;
import com.azuriom.azlink.common.tasks.TpsTask;
import com.azuriom.azlink.nukkit.command.NukkitCommandExecutor;
import com.azuriom.azlink.nukkit.command.NukkitCommandSender;
import com.azuriom.azlink.nukkit.utils.NukkitLoggerAdapter;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public final class AzLinkNukkitPlugin extends PluginBase implements AzLinkPlatform {

    private final SchedulerAdapter scheduler = new JavaSchedulerAdapter(
            r -> getServer().getScheduler().scheduleTask(this, r),
            r -> getServer().getScheduler().scheduleTask(this, r, true)
    );

    private final TpsTask tpsTask = new TpsTask();

    private AzLinkPlugin plugin;

    private LoggerAdapter logger;

    @Override
    public void onLoad() {
        this.logger = new NukkitLoggerAdapter(getLogger());
    }

    @Override
    public void onEnable() {
        this.plugin = new AzLinkPlugin(this);
        this.plugin.init();

        PluginIdentifiableCommand command = getCommand("azlink");
        ((PluginCommand<?>) command).setExecutor(new NukkitCommandExecutor(this.plugin));

        getServer().getScheduler().scheduleDelayedRepeatingTask(this, this.tpsTask, 0, 1);
    }

    @Override
    public void onDisable() {
        this.plugin.shutdown();
    }

    @Override
    public AzLinkPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return this.logger;
    }

    @Override
    public SchedulerAdapter getSchedulerAdapter() {
        return this.scheduler;
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new PlatformInfo(getServer().getName(), getServer().getVersion());
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.NUKKIT;
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public Optional<WorldData> getWorldData() {
        int loadedChunks = getServer().getLevels().values().stream()
                .mapToInt(w -> w.getChunks().size())
                .sum();

        int entities = getServer().getLevels().values().stream()
                .mapToInt(w -> w.getEntities().length)
                .sum();

        return Optional.of(new WorldData(this.tpsTask.getTps(), loadedChunks, entities));
    }

    @Override
    public Path getDataDirectory() {
        return getDataFolder().toPath();
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        return getServer().getOnlinePlayers().values().stream().map(NukkitCommandSender::new);
    }

    @Override
    public int getMaxPlayers() {
        return getServer().getMaxPlayers();
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        getServer().dispatchCommand(getServer().getConsoleSender(), command);
    }
}
