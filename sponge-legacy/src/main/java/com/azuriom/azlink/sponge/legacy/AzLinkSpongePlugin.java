package com.azuriom.azlink.sponge.legacy;

import com.azuriom.azlink.common.AzLinkPlatform;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.data.WorldData;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.azuriom.azlink.common.logger.Slf4jLoggerAdapter;
import com.azuriom.azlink.common.platform.PlatformInfo;
import com.azuriom.azlink.common.platform.PlatformType;
import com.azuriom.azlink.common.scheduler.JavaSchedulerAdapter;
import com.azuriom.azlink.common.scheduler.SchedulerAdapter;
import com.azuriom.azlink.common.tasks.TpsTask;
import com.azuriom.azlink.common.utils.VersionInfo;
import com.azuriom.azlink.sponge.legacy.command.SpongeCommandExecutor;
import com.azuriom.azlink.sponge.legacy.command.SpongeCommandSender;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@Plugin(
        id = "azlink",
        name = "AzLink",
        version = VersionInfo.VERSION,
        description = "The plugin to link your Azuriom website with your server.",
        url = "https://azuriom.com",
        authors = "Azuriom Team",
        dependencies = @Dependency(id = Platform.API_ID)
)
public final class AzLinkSpongePlugin implements AzLinkPlatform {

    private final TpsTask tpsTask = new TpsTask();

    private final Game game;
    private final Path configDirectory;
    private final LoggerAdapter logger;

    private SchedulerAdapter scheduler;
    private AzLinkPlugin plugin;

    @Inject
    public AzLinkSpongePlugin(Game game, @ConfigDir(sharedRoot = false) Path configDirectory, Logger logger) {
        this.game = game;
        this.configDirectory = configDirectory;
        this.logger = new Slf4jLoggerAdapter(logger);
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        this.scheduler = initScheduler();

        this.plugin = new AzLinkPlugin(this);
        this.plugin.init();

        this.game.getCommandManager().register(this, new SpongeCommandExecutor(this.plugin), "azlink", "azuriomlink");

        Task.builder().intervalTicks(1).execute(this.tpsTask).submit(this);
    }

    @Listener
    public void onGameStop(GameStoppedEvent event) {
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
        return this.logger;
    }

    @Override
    public SchedulerAdapter getSchedulerAdapter() {
        return this.scheduler;
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.SPONGE;
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        Platform platform = this.game.getPlatform();
        PluginContainer version = platform.getContainer(Platform.Component.IMPLEMENTATION);

        return new PlatformInfo(version.getName(), version.getVersion().orElse("unknown"));
    }

    @Override
    public String getPluginVersion() {
        return VersionInfo.VERSION;
    }

    @Override
    public Path getDataDirectory() {
        return this.configDirectory;
    }

    @Override
    public Optional<WorldData> getWorldData() {
        int loadedChunks = this.game.getServer().getWorlds().stream()
                .mapToInt(w -> Iterables.size(w.getLoadedChunks()))
                .sum();

        int entities = this.game.getServer().getWorlds().stream()
                .mapToInt(w -> w.getEntities().size())
                .sum();

        return Optional.of(new WorldData(this.tpsTask.getTps(), loadedChunks, entities));
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        return this.game.getServer().getOnlinePlayers().stream().map(SpongeCommandSender::new);
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        this.game.getCommandManager().process(this.game.getServer().getConsole(), command);
    }

    @Override
    public int getMaxPlayers() {
        return this.game.getServer().getMaxPlayers();
    }

    private SchedulerAdapter initScheduler() {
        SpongeExecutorService syncExecutor = this.game.getScheduler().createSyncExecutor(this);
        SpongeExecutorService asyncExecutor = this.game.getScheduler().createAsyncExecutor(this);

        return new JavaSchedulerAdapter(asyncExecutor, syncExecutor, asyncExecutor);
    }
}
