package com.azuriom.azlink.hytale;

import com.azuriom.azlink.common.AzLinkPlatform;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.data.WorldData;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.azuriom.azlink.common.platform.PlatformInfo;
import com.azuriom.azlink.common.platform.PlatformType;
import com.azuriom.azlink.common.scheduler.JavaSchedulerAdapter;
import com.azuriom.azlink.common.scheduler.SchedulerAdapter;
import com.azuriom.azlink.hytale.command.HytaleCommandExecutor;
import com.azuriom.azlink.hytale.command.HytalePlayerWrapper;
import com.azuriom.azlink.hytale.logger.HytaleLoggerAdapter;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.commands.world.perf.WorldPerfCommand;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public final class AzLinkHytalePlugin extends JavaPlugin implements AzLinkPlatform {

    private final AzLinkPlugin plugin = new AzLinkPlugin(this);
    private final SchedulerAdapter scheduler = new JavaSchedulerAdapter(HytaleServer.SCHEDULED_EXECUTOR::submit);
    private final LoggerAdapter logger;

    public AzLinkHytalePlugin(JavaPluginInit init) {
        super(init);
        this.logger = new HytaleLoggerAdapter(getLogger());
    }

    @Override
    protected void setup() {
        getCommandRegistry().registerCommand(new HytaleCommandExecutor(this));
    }

    @Override
    public void start() {
        this.plugin.init();
    }

    @Override
    public void shutdown() {
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
    public PlatformType getPlatformType() {
        return PlatformType.HYTALE;
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new PlatformInfo("Hytale", ManifestUtil.getImplementationVersion());
    }

    @Override
    public String getPluginVersion() {
        return this.getManifest().getVersion().toString();
    }

    @Override
    public Optional<WorldData> getWorldData() {
        Collection<World> worlds = Universe.get().getWorlds().values();

        int loadedChunks = worlds.stream()
                .mapToInt(w -> w.getChunkStore().getLoadedChunksCount())
                .sum();
        int entities = worlds.stream()
                .mapToInt(w -> w.getEntityStore().getStore().getEntityCount())
                .sum();
        double tps = worlds.stream()
                .mapToDouble(world -> {
                    int step = world.getTickStepNanos();
                    HistoricMetric metrics = world.getBufferedTickLengthMetricSet();

                    return WorldPerfCommand.tpsFromDelta(metrics.getAverage(0), step);
                })
                .average()
                .orElse(20.0);

        return Optional.of(new WorldData(tps, loadedChunks, entities));
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        Universe universe = Universe.get();

        return universe.getPlayers()
                .stream()
                .map(HytalePlayerWrapper::new);
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        HytaleServer.get().getCommandManager().handleCommand(ConsoleSender.INSTANCE, command);
    }

    @Override
    public int getMaxPlayers() {
        return HytaleServer.get().getConfig().getMaxPlayers();
    }
}
