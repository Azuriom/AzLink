package com.azuriom.azlink.common.tasks;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.data.UserInfo;
import com.azuriom.azlink.common.data.WebsiteResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FetcherTask implements Runnable {

    private static final Duration MIN_REQUEST_INTERVAL = Duration.ofSeconds(5);

    private final AzLinkPlugin plugin;

    private Instant lastFullDataSent = Instant.MIN;
    private Instant lastRequest = Instant.MIN;
    private CompletableFuture<Void> scheduledFetch;

    public FetcherTask(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (Instant.now().isBefore(nextRequestTime())) {
            return;
        }

        fetch().exceptionally(ex -> {
            this.plugin.getLogger().error("Unable to send data to the website: " + ex.getMessage());

            return null;
        });
    }

    public synchronized CompletableFuture<Void> fetch() {
        Instant now = Instant.now();

        if (!this.plugin.getConfig().isValid()) {
            return CompletableFuture.completedFuture(null);
        }

        Instant nextRequest = nextRequestTime();

        if (now.isBefore(nextRequest)) {
            // The request in progress may already have missed what the caller is waiting for,
            // like commands the website just queued, so send another one as soon as possible
            return scheduleFetch(Duration.between(now, nextRequest).toMillis() + 1);
        }

        this.plugin.getPlatform().prepareDataAsync();
        this.lastRequest = now;

        Executor sync = this.plugin.getScheduler().syncExecutor();
        Executor async = this.plugin.getScheduler().asyncExecutor();

        LocalDateTime currentTime = LocalDateTime.now();
        boolean sendFullData = currentTime.getMinute() % 15 == 0
                && this.lastFullDataSent.isBefore(now.minusSeconds(60));

        return CompletableFuture.supplyAsync(() -> this.plugin.getServerData(sendFullData), sync)
                .thenComposeAsync(this.plugin.getHttpClient()::postData, async)
                .thenAcceptAsync(res -> handleResponse(res, sendFullData), sync);
    }

    private Instant nextRequestTime() {
        return this.lastRequest.plus(MIN_REQUEST_INTERVAL);
    }

    private CompletableFuture<Void> scheduleFetch(long delayMillis) {
        if (this.scheduledFetch != null) {
            return this.scheduledFetch;
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        this.scheduledFetch = future;

        this.plugin.getScheduler().scheduleAsyncLater(() -> {
            synchronized (this) {
                this.scheduledFetch = null;
            }

            fetch().whenComplete((result, ex) -> {
                if (ex != null) {
                    future.completeExceptionally(ex);
                } else {
                    future.complete(null);
                }
            });
        }, delayMillis, TimeUnit.MILLISECONDS);

        return future;
    }

    private void handleResponse(WebsiteResponse response, boolean sendFullData) {
        if (response == null) {
            return;
        }

        if (response.getUsers() != null) {
            for (UserInfo user : response.getUsers()) {
                this.plugin.getUserManager().addUser(user);
            }
        }

        if (response.getCommands().isEmpty()) {
            return;
        }

        dispatchCommands(response.getCommands());

        if (sendFullData) {
            this.lastFullDataSent = Instant.now();
        }
    }

    private void dispatchCommands(Map<String, List<String>> commands) {
        this.plugin.getLogger().info("Dispatching commands to " + commands.size() + " players.");

        Map<String, CommandSender> players = this.plugin.getPlatform()
                .getOnlinePlayers()
                .collect(Collectors.toMap(cs -> cs.getName().toLowerCase(Locale.ROOT), p -> p, (p1, p2) -> {
                    String player1 = p1.getName() + " (" + p1.getUuid() + ')';
                    String player2 = p2.getName() + " (" + p2.getUuid() + ')';
                    this.plugin.getLogger().warn("Duplicate players names: " + player1 + " / " + player2);
                    return p1;
                }));

        for (Map.Entry<String, List<String>> entry : commands.entrySet()) {
            String playerName = entry.getKey();
            CommandSender player = players.get(playerName.toLowerCase(Locale.ROOT));

            if (player != null) {
                playerName = player.getName();
            }

            for (String command : entry.getValue()) {
                command = command.replace("{player}", playerName)
                        .replace("{name}", playerName)
                        .replace("{uuid}", player != null ? player.getUuid().toString() : "?");

                this.plugin.getLogger().info("Dispatching command for player " + playerName + ": " + command);

                this.plugin.getPlatform().dispatchConsoleCommand(command);
            }
        }
    }
}
