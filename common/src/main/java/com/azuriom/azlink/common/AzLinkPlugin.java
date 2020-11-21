package com.azuriom.azlink.common;

import com.azuriom.azlink.common.command.AzLinkCommand;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.config.PluginConfig;
import com.azuriom.azlink.common.data.PlatformData;
import com.azuriom.azlink.common.data.PlayerData;
import com.azuriom.azlink.common.data.ServerData;
import com.azuriom.azlink.common.data.SystemData;
import com.azuriom.azlink.common.data.WorldData;
import com.azuriom.azlink.common.http.client.HttpClient;
import com.azuriom.azlink.common.http.server.HttpServer;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.azuriom.azlink.common.scheduler.ThreadFactoryBuilder;
import com.azuriom.azlink.common.tasks.FetcherTask;
import com.azuriom.azlink.common.utils.SystemUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().name("azlink-scheduler").daemon());

    private final HttpClient httpClient = new HttpClient(this);
    private HttpServer httpServer = new HttpServer(this);

    private final Gson gson = new Gson();
    private final Gson gsonPrettyPrint = new GsonBuilder().setPrettyPrinting().create();

    private final AzLinkCommand command = new AzLinkCommand(this);

    private final FetcherTask fetcherTask = new FetcherTask(this);

    private final AzLinkPlatform platform;

    private Path configFile;
    private PluginConfig config = new PluginConfig(null, null, true, HttpServer.DEFAULT_PORT);

    private boolean logCpuError = true;

    public AzLinkPlugin(AzLinkPlatform platform) {
        this.platform = platform;
    }

    public void init() {
        this.configFile = this.platform.getDataDirectory().resolve("config.json");

        try (BufferedReader reader = Files.newBufferedReader(this.configFile)) {
            this.config = this.gson.fromJson(reader, PluginConfig.class);
        } catch (NoSuchFileException e) {
            // ignore, not setup yet
        } catch (IOException e) {
            getLogger().error("Error while loading configuration", e);
            return;
        }

        LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
        long startDelay = Duration.between(LocalDateTime.now(), start).toMillis() + 500; // Add 0.5s to ensure we are not in the previous hour

        this.scheduler.scheduleAtFixedRate(this.fetcherTask, startDelay, TimeUnit.MINUTES.toMillis(1), TimeUnit.MILLISECONDS);

        if (!this.config.isValid()) {
            getLogger().warn("Invalid configuration, please use '/azlink' to setup the plugin.");
            return;
        }

        if (this.config.hasInstantCommands()) {
            this.httpServer.start();
        }

        this.platform.executeAsync(() -> {
            try {
                this.httpClient.verifyStatus();

                getLogger().info("Successfully connected to " + this.config.getSiteUrl());
            } catch (IOException e) {
                getLogger().warn("Unable to verify the website connection: " + e.getMessage() + " - " + e.getClass().getName());
            }
        });
    }

    public void restartHttpServer() {
        this.httpServer.stop();

        this.httpServer = new HttpServer(this);

        this.httpServer.start();
    }

    public void shutdown() {
        getLogger().info("Shutting down scheduler");
        this.scheduler.shutdown();
        try {
            this.scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            getLogger().warn("Error while shutting down scheduler", e);
        }

        getLogger().info("Stopping HTTP server");
        this.httpServer.stop();
    }

    public void setConfig(PluginConfig config) {
        this.config = config;
    }

    public void saveConfig() throws IOException {
        if (!Files.isDirectory(this.platform.getDataDirectory())) {
            Files.createDirectories(this.platform.getDataDirectory());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(this.configFile)) {
            this.gsonPrettyPrint.toJson(this.config, writer);
        }
    }

    public AzLinkCommand getCommand() {
        return this.command;
    }

    public ServerData getServerData(boolean fullData) {
        List<PlayerData> players = this.platform.getOnlinePlayers()
                .map(CommandSender::toData)
                .collect(Collectors.toList());
        int max = this.platform.getMaxPlayers();

        PlatformData platformData = this.platform.getPlatformData();

        SystemData system = fullData ? new SystemData(SystemUtils.getMemoryUsage(), getCpuUsage()) : null;
        WorldData world = fullData ? this.platform.getWorldData().orElse(null) : null;

        return new ServerData(platformData, this.platform.getPluginVersion(), players, max, system, world, fullData);
    }

    public void fetchNow() {
        this.platform.executeAsync(this.fetcherTask);
    }

    public LoggerAdapter getLogger() {
        return this.platform.getLoggerAdapter();
    }

    public ScheduledExecutorService getScheduler() {
        return this.scheduler;
    }

    public PluginConfig getConfig() {
        return this.config;
    }

    public AzLinkPlatform getPlatform() {
        return this.platform;
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public HttpServer getHttpServer() {
        return this.httpServer;
    }

    public Gson getGson() {
        return this.gson;
    }

    public Gson getGsonPrettyPrint() {
        return this.gsonPrettyPrint;
    }

    private double getCpuUsage() {
        try {
            return SystemUtils.getCpuUsage();
        } catch (Throwable t) {
            if (this.logCpuError) {
                this.logCpuError = false;

                getLogger().warn("Error while retrieving CPU usage", t);
            }
        }
        return -1;
    }
}
