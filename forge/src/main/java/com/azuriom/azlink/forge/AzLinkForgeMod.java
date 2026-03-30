package com.azuriom.azlink.forge;

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
import com.azuriom.azlink.forge.command.ForgeCommandExecutor;
import com.azuriom.azlink.forge.command.ForgePlayer;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.versions.forge.ForgeVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@Mod(AzLinkForgeMod.MODID)
public final class AzLinkForgeMod implements AzLinkPlatform {

    public static final String MODID = "azlink";
    private static final Logger LOGGER = LoggerFactory.getLogger("azlink");

    private final LoggerAdapter logger = new Slf4jLoggerAdapter(LOGGER);
    private final TpsTask tpsTask = new TpsTask();

    private final AzLinkPlugin plugin;
    private final Path dataDirectory;

    private MinecraftServer server;
    private SchedulerAdapter scheduler;

    public AzLinkForgeMod() {
        this.plugin = new AzLinkPlugin(this);

        Path defaultDataDirectory = FMLPaths.CONFIGDIR.get().resolve(MODID);
        Path legacyDataDirectory = FMLPaths.CONFIGDIR.get().resolve("AzLink");

        if (!Files.exists(defaultDataDirectory.resolve("config.json"))
                && Files.exists(legacyDataDirectory.resolve("config.json"))) {
            this.dataDirectory = legacyDataDirectory;
            LOGGER.warn("Using legacy AzLink config directory '{}' for compatibility.", this.dataDirectory);
        } else {
            this.dataDirectory = defaultDataDirectory;
        }

        MinecraftForge.EVENT_BUS.register(this);
        TickEvent.ServerTickEvent.Pre.BUS.addListener(e -> this.tpsTask.run());
    }

    @SubscribeEvent
    public void onServerStarting(ServerAboutToStartEvent event) {
        this.server = event.getServer();
        this.scheduler = this.initScheduler();
        this.plugin.init();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (this.plugin != null) {
            this.plugin.shutdown();
        }
        this.server = null;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        var command = new ForgeCommandExecutor<>(this.plugin);
        command.register(event.getDispatcher());
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
        return PlatformType.FORGE;
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        String name = ModList.get().getModContainerById("forge")
                .map(mc -> mc.getModInfo().getDisplayName())
                .orElse("Forge");

        String version = ForgeVersion.getVersion();
        if (version == null || version.isEmpty()) {
            version = FMLLoader.versionInfo().forgeVersion();
        }

        return new PlatformInfo(name, version);
    }

    @Override
    public String getPluginVersion() {
        return ModList.get().getModContainerById(MODID)
                .map(mc -> mc.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public Path getDataDirectory() {
        return this.dataDirectory;
    }

    @Override
    public Optional<WorldData> getWorldData() {
        if (this.server == null) {
            return Optional.empty();
        }

        int loadedChunks = Streams.stream(this.server.getAllLevels())
                .mapToInt(level -> level.getChunkSource().getLoadedChunksCount())
                .sum();
        int entities = Streams.stream(this.server.getAllLevels())
                .mapToInt(level -> Iterables.size(level.getEntities().getAll()))
                .sum();

        return Optional.of(new WorldData(this.tpsTask.getTps(), loadedChunks, entities));
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        if (this.server == null) {
            return Stream.empty();
        }

        return this.server.getPlayerList().getPlayers().stream().map(ForgePlayer::new);
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        CommandSourceStack console = this.getServer().createCommandSourceStack();
        Commands commandManager = console.getServer().getCommands();
        var parsed = commandManager.getDispatcher().parse(command, console);
        commandManager.performCommand(parsed, command);
    }

    @Override
    public int getMaxPlayers() {
        return this.server != null ? this.server.getMaxPlayers() : 0;
    }

    private SchedulerAdapter initScheduler() {
        return new JavaSchedulerAdapter(this.getServer()::executeIfPossible);
    }

    private MinecraftServer getServer() {
        if (this.server == null) {
            throw new IllegalStateException("Server has not been initialized yet");
        }

        return this.server;
    }
}