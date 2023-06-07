package com.azuriom.azlink.fabric;

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
import com.azuriom.azlink.fabric.command.FabricCommandExecutor;
import com.azuriom.azlink.fabric.command.FabricPlayer;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public final class AzLinkFabricMod implements AzLinkPlatform, DedicatedServerModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("azlink");

    private final LoggerAdapter logger = new Slf4jLoggerAdapter(LOGGER);
    private final TpsTask tpsTask = new TpsTask();

    private final ModContainer modContainer;
    private final AzLinkPlugin plugin;
    private SchedulerAdapter scheduler;

    public AzLinkFabricMod() {
        this.modContainer = FabricLoader.getInstance()
                .getModContainer("azlink")
                .orElseThrow(() -> new RuntimeException("Unable to get the mod container."));
        this.plugin = new AzLinkPlugin(this);
    }

    @Override
    public void onInitializeServer() {
        var command = new FabricCommandExecutor<>(this.plugin);

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStop);
        ServerTickEvents.START_SERVER_TICK.register(s -> this.tpsTask.run());

        CommandRegistrationCallback.EVENT
                .register((dispatcher, registry, env) -> command.register(dispatcher));
    }

    public void onServerStart(MinecraftServer server) {
        this.scheduler = this.initScheduler();
        this.plugin.init();
    }

    public void onServerStop(MinecraftServer server) {
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
        return PlatformType.FABRIC;
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return FabricLoader.getInstance()
                .getModContainer("fabric")
                .map(ModContainer::getMetadata)
                .map(m -> new PlatformInfo(m.getName(), m.getVersion().getFriendlyString()))
                .orElse(new PlatformInfo("unknown", "unknown"));
    }

    @Override
    public String getPluginVersion() {
        return this.modContainer.getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public Path getDataDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve("AzLink");
    }

    @Override
    public Optional<WorldData> getWorldData() {
        int loadedChunks = Streams.stream(getServer().getWorlds())
                .mapToInt(w -> w.getChunkManager().getLoadedChunkCount())
                .sum();
        int entities = Streams.stream(getServer().getWorlds())
                .mapToInt(w -> Iterables.size(w.iterateEntities()))
                .sum();

        return Optional.of(new WorldData(this.tpsTask.getTps(), loadedChunks, entities));
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        return getServer().getPlayerManager()
                .getPlayerList()
                .stream()
                .map(FabricPlayer::new);
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        ServerCommandSource source = getServer().getCommandSource();
        getServer().getCommandManager().executeWithPrefix(source, command);
    }

    @Override
    public int getMaxPlayers() {
        return getServer().getMaxPlayerCount();
    }

    private SchedulerAdapter initScheduler() {
        return new JavaSchedulerAdapter(getServer()::executeSync);
    }

    @SuppressWarnings("deprecation")
    private MinecraftServer getServer() {
        return (MinecraftServer) FabricLoader.getInstance().getGameInstance();
    }
}
