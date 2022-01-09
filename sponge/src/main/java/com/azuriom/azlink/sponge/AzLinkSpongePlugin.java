package com.azuriom.azlink.sponge;

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
import com.azuriom.azlink.sponge.command.SpongeCommandExecutor;
import com.azuriom.azlink.sponge.command.SpongeCommandSender;
import com.azuriom.azlink.sponge.logger.Log4jLoggerAdapter;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Platform.Component;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command.Raw;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scheduler.TaskExecutorService;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@Plugin("azlink")
public final class AzLinkSpongePlugin implements AzLinkPlatform {

    private final TpsTask tpsTask = new TpsTask();
    private final PluginContainer pluginContainer;
    private final Game game;
    private final Path configDirectory;
    private final LoggerAdapter logger;
    private final AzLinkPlugin plugin;
    private SchedulerAdapter scheduler;

    @Inject
    public AzLinkSpongePlugin(PluginContainer pluginContainer, Game game, @ConfigDir(sharedRoot = false) Path configDirectory, Logger logger) {
        this.pluginContainer = pluginContainer;
        this.game = game;
        this.configDirectory = configDirectory;
        this.logger = new Log4jLoggerAdapter(logger);
        this.plugin = new AzLinkPlugin(this);
    }

    @Listener
    public void onServerStarted(StartedEngineEvent<Server> event) {
        this.scheduler = this.initScheduler();
        this.plugin.init();

        Task task = Task.builder()
                .interval(Ticks.of(1))
                .execute(this.tpsTask)
                .plugin(this.pluginContainer)
                .build();

        event.engine().scheduler().submit(task);
    }

    @Listener
    public void onServerStop(StoppingEngineEvent<Server> event) {
        if (this.plugin != null) {
            this.plugin.shutdown();
        }
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Raw> event) {
        event.register(this.pluginContainer, new SpongeCommandExecutor(this.plugin), "azlink", "azuriomlink");
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
        Platform platform = this.game.platform();
        PluginMetadata version = platform.container(Component.IMPLEMENTATION).metadata();

        return new PlatformInfo(version.name().orElse("Sponge"), version.version().getQualifier());
    }

    @Override
    public String getPluginVersion() {
        return this.pluginContainer.metadata().version().toString();
    }

    @Override
    public Path getDataDirectory() {
        return this.configDirectory;
    }

    @Override
    public Optional<WorldData> getWorldData() {
        int loadedChunks = this.game.server().worldManager()
                .worlds()
                .stream()
                .mapToInt(w -> Iterables.size(w.loadedChunks()))
                .sum();
        int entities = this.game.server().worldManager()
                .worlds()
                .stream()
                .mapToInt(w -> w.entities().size())
                .sum();

        return Optional.of(new WorldData(this.tpsTask.getTps(), loadedChunks, entities));
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        return this.game.server().onlinePlayers().stream()
                .map(player -> new SpongeCommandSender(player, player));
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        try {
            this.game.server().commandManager().process(this.game.systemSubject(), command);
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getMaxPlayers() {
        return this.game.server().maxPlayers();
    }

    private SchedulerAdapter initScheduler() {
        TaskExecutorService syncExecutor = this.game.server().scheduler().executor(this.pluginContainer);
        TaskExecutorService asyncExecutor = this.game.asyncScheduler().executor(this.pluginContainer);

        return new JavaSchedulerAdapter(asyncExecutor, syncExecutor, asyncExecutor);
    }
}
