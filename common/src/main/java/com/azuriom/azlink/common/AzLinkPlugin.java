package com.azuriom.azlink.common;

import com.azuriom.azlink.common.command.AzLinkCommand;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.config.PluginConfig;
import com.azuriom.azlink.common.data.PlatformData;
import com.azuriom.azlink.common.data.PlayerData;
import com.azuriom.azlink.common.data.ServerData;
import com.azuriom.azlink.common.data.SystemData;
import com.azuriom.azlink.common.data.WorldData;
import com.azuriom.azlink.common.http.HttpClient;
import com.azuriom.azlink.common.scheduler.ThreadBuilder;
import com.azuriom.azlink.common.tasks.FetcherTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AzLinkPlugin {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> new ThreadBuilder(r).name("azlink-thread").daemon().build());

    private final HttpClient httpClient = new HttpClient(this);

    private final Gson gson = new Gson();
    private final Gson gsonPrettyPrint = new GsonBuilder().setPrettyPrinting().create();

    private final AzLinkCommand command = new AzLinkCommand(this);

    private final FetcherTask fetcherTask = new FetcherTask(this);

    private final AzLinkPlatform platform;

    private Path configFile;
    private PluginConfig config = new PluginConfig(null, null);

    private boolean logCpuError = true;

    public AzLinkPlugin(AzLinkPlatform platform) {
        this.platform = platform;
    }

    public void init() {
        configFile = platform.getDataDirectory().resolve("config.json");

        try (BufferedReader reader = Files.newBufferedReader(configFile)) {
            config = gson.fromJson(reader, PluginConfig.class);
        } catch (NoSuchFileException e) {
            // ignore, not setup yet
        } catch (IOException e) {
            platform.getLoggerAdapter().error("Error while loading configuration", e);
            return;
        }

        LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
        long startDelay = Duration.between(LocalDateTime.now(), start).toMillis();

        scheduler.scheduleAtFixedRate(fetcherTask, startDelay, TimeUnit.MINUTES.toMillis(1), TimeUnit.MILLISECONDS);

        if (!config.isValid()) {
            platform.getLoggerAdapter().warn("Invalid configuration, you can use '/azlink' to setup the plugin.");
            return;
        }

        platform.executeAsync(() -> {
            try {
                httpClient.verifyStatus();
            } catch (IOException e) {
                platform.getLoggerAdapter().warn("Unable to connect", e);
            }
        });
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public void setConfig(PluginConfig config) {
        this.config = config;
    }

    public void saveConfig() throws IOException {
        if (!Files.isDirectory(platform.getDataDirectory())) {
            Files.createDirectories(platform.getDataDirectory());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
            gsonPrettyPrint.toJson(config, writer);
        }
    }

    public AzLinkCommand getCommand() {
        return command;
    }

    public ServerData getServerData(boolean fullData) {
        List<PlayerData> players = platform.getOnlinePlayers()
                .map(CommandSender::toData)
                .collect(Collectors.toList());
        int max = platform.getMaxPlayers();

        PlatformData platformData = platform.getPlatformData();

        SystemData system = fullData ? new SystemData(getMemoryUsage(), getCpuUsage()) : null;
        WorldData world = fullData ? platform.getWorldData().orElse(null) : null;

        return new ServerData(platformData, platform.getPluginVersion(), players, max, system, world, fullData);
    }

    private double getCpuUsage() {
        try {
            if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean) {
                return ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad() * 100.0;
            }
        } catch (Throwable t) {
            if (logCpuError) {
                logCpuError = false;

                platform.getLoggerAdapter().warn("Error while retrieving cpu usage", t);
            }
        }
        return -1;
    }

    public void fetchNow() {
        platform.executeAsync(fetcherTask);
    }

    private double getMemoryUsage() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0;
    }

    public PluginConfig getConfig() {
        return config;
    }

    public AzLinkPlatform getPlatform() {
        return platform;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public Gson getGson() {
        return gson;
    }

    public Gson getGsonPrettyPrint() {
        return gsonPrettyPrint;
    }
}
