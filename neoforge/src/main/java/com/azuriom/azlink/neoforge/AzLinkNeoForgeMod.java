package com.azuriom.azlink.neoforge;

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
import com.azuriom.azlink.neoforge.command.NeoForgeCommandExecutor;
import com.azuriom.azlink.neoforge.command.NeoForgePlayer;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@Mod(AzLinkNeoForgeMod.MODID)
public final class AzLinkNeoForgeMod implements AzLinkPlatform {

    public static final String MODID = "azlink";
    private static final Logger LOGGER = LogUtils.getLogger();

    private final LoggerAdapter logger = new Slf4jLoggerAdapter(LOGGER);
    private final TpsTask tpsTask = new TpsTask();

    private final ModContainer modContainer;
    private final AzLinkPlugin plugin;
    private final Path dataDirectory;

    private MinecraftServer server;
    private SchedulerAdapter scheduler;

    public AzLinkNeoForgeMod(ModContainer modContainer) {
        this.modContainer = modContainer;
        this.plugin = new AzLinkPlugin(this);
        this.dataDirectory = FMLPaths.CONFIGDIR.get().resolve(this.modContainer.getModId());

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStart(ServerAboutToStartEvent event) {
        this.server = event.getServer();

        this.scheduler = this.initScheduler();
        this.plugin.init();
    }

    @SubscribeEvent
    public void onServerStoppingStop(ServerStoppingEvent event) {
        if (this.plugin != null) {
            this.plugin.shutdown();
        }

        this.server = null;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        var command = new NeoForgeCommandExecutor<>(this.plugin);
        command.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onTickStart(ServerTickEvent.Pre event) {
        this.tpsTask.run();
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
        return PlatformType.NEOFORGE;
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        String name = ModList.get().getModContainerById("neoforge")
                .map(ModContainer::getModInfo)
                .map(IModInfo::getDisplayName)
                .orElse("unknown");

        return new PlatformInfo(name, FMLLoader.versionInfo().neoForgeVersion());
    }

    @Override
    public String getPluginVersion() {
        return this.modContainer.getModInfo().getVersion().toString();
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
                .mapToInt(l -> l.getChunkSource().getLoadedChunksCount())
                .sum();
        int entities = Streams.stream(this.server.getAllLevels())
                .mapToInt(l -> Iterables.size(l.getEntities().getAll()))
                .sum();

        return Optional.of(new WorldData(this.tpsTask.getTps(), loadedChunks, entities));
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        if (this.server == null) {
            return Stream.empty();
        }

        return this.server.getPlayerList()
                .getPlayers()
                .stream()
                .map(NeoForgePlayer::new);
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        CommandSourceStack console = getServer().createCommandSourceStack();
        Commands commandManager = console.getServer().getCommands();
        var parsed = commandManager.getDispatcher().parse(command, console);

        commandManager.performCommand(parsed, command);
    }

    @Override
    public int getMaxPlayers() {
        return this.server != null ? this.server.getMaxPlayers() : 0;
    }

    private SchedulerAdapter initScheduler() {
        return new JavaSchedulerAdapter(getServer()::executeIfPossible);
    }

    private MinecraftServer getServer() {
        if (this.server == null) {
            throw new IllegalStateException("Server has not been initialized yet");
        }

        return this.server;
    }
}
