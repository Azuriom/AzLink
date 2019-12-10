package com.azuriom.azlink.sponge;

import com.azuriom.azlink.common.AzLinkPlatform;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.PlatformType;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.data.WorldData;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.azuriom.azlink.common.logger.Slf4jLoggerAdapter;
import com.azuriom.azlink.common.tasks.TpsTask;
import com.azuriom.azlink.sponge.command.SpongeCommandExecutor;
import com.azuriom.azlink.sponge.command.SpongeCommandSender;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@Plugin(
        id = "azlink",
        name = "AzLink",
        version = "${pluginVersion}",
        description = "The plugin to link your Azuriom website with your server.",
        url = "https://azuriom.com",
        authors = "Azuriom Team"
)
public final class AzLinkSpongePlugin implements AzLinkPlatform {

    private final AzLinkPlugin plugin = new AzLinkPlugin(this);

    private final Game game;

    private final Path configDirectory;

    private final LoggerAdapter logger;

    private TpsTask tpsTask = new TpsTask();

    @Inject
    public AzLinkSpongePlugin(Game game, @ConfigDir(sharedRoot = false) Path configDirectory, Logger logger) {
        this.game = game;
        this.configDirectory = configDirectory;
        this.logger = new Slf4jLoggerAdapter(logger);
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        plugin.init();

        game.getCommandManager().register(this, new SpongeCommandExecutor(plugin), "azlink", "azuriomlink");

        Task.builder().intervalTicks(1).execute(tpsTask).submit(this);
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
        return PlatformType.SPONGE;
    }

    @Override
    public String getPlatformName() {
        return game.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName();
    }

    @Override
    public String getPlatformVersion() {
        return game.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse("unknown");
    }

    @Override
    public String getPluginVersion() {
        return "${pluginVersion}";
    }

    @Override
    public Path getDataDirectory() {
        return configDirectory;
    }

    @Override
    public Optional<WorldData> getWorldData() {
        int loadedChunks = game.getServer().getWorlds().stream()
                .mapToInt(w -> Iterables.size(w.getLoadedChunks()))
                .sum();

        int entities = game.getServer().getWorlds().stream().mapToInt(w -> w.getEntities().size()).sum();

        return Optional.of(new WorldData(tpsTask.getTps(), loadedChunks, entities));
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        return game.getServer().getOnlinePlayers().stream().map(SpongeCommandSender::new);
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        game.getCommandManager().process(game.getServer().getConsole(), command);
    }

    @Override
    public int getMaxPlayers() {
        return game.getServer().getMaxPlayers();
    }

    @Override
    public void executeSync(Runnable runnable) {
        Task.builder().execute(runnable).submit(this);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        Task.builder().execute(runnable).async().submit(this);
    }
}
