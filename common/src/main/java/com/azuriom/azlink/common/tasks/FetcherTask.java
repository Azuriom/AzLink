package com.azuriom.azlink.common.tasks;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.data.ServerData;
import com.azuriom.azlink.common.data.WebsiteResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FetcherTask implements Runnable {

    private final AzLinkPlugin plugin;

    private Instant lastFullDataSent = Instant.MIN;
    private Instant lastRequest = Instant.MIN;

    public FetcherTask(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Instant now = Instant.now();

        if (!this.plugin.getConfig().isValid() || this.lastRequest.isAfter(now.minusSeconds(5))) {
            return;
        }

        this.lastRequest = now;

        this.plugin.getScheduler().executeSync(() -> {
            LocalDateTime currentTime = LocalDateTime.now();
            boolean sendFullData = currentTime.getMinute() % 15 == 0 && this.lastFullDataSent.isBefore(now.minusSeconds(60));

            ServerData data = this.plugin.getServerData(sendFullData);

            this.plugin.getScheduler().executeAsync(() -> sendData(data, sendFullData));
        });
    }

    private void sendData(ServerData data, boolean sendFullData) {
        try {
            WebsiteResponse response = this.plugin.getHttpClient().postData(data);

            if (response == null || response.getCommands().isEmpty()) {
                return;
            }

            this.plugin.getScheduler().executeSync(() -> dispatchCommands(response.getCommands()));

            if (sendFullData) {
                this.lastFullDataSent = Instant.now();
            }
        } catch (IOException e) {
            this.plugin.getLogger().error("Unable to send data to the website: " + e.getMessage() + " - " + e.getClass().getName());
        }
    }

    private void dispatchCommands(Map<String, List<String>> commands) {
        this.plugin.getLogger().info("Dispatching commands to " + commands.size() + " players.");

        Map<String, CommandSender> players = this.plugin.getPlatform()
                .getOnlinePlayers()
                .collect(Collectors.toMap(cs -> cs.getName().toLowerCase(), p -> p, (p1, p2) -> {
                    String player1 = p1.getName() + " (" + p1.getUuid() + ')';
                    String player2 = p2.getName() + " (" + p2.getUuid() + ')';
                    this.plugin.getLogger().warn("Duplicate players names: " + player1 + " / " + player2);
                    return p1;
                }));

        for (Map.Entry<String, List<String>> entry : commands.entrySet()) {
            String playerName = entry.getKey();
            CommandSender player = players.get(playerName.toLowerCase());

            if (player != null) {
                playerName = player.getName();
            }

            for (String command : entry.getValue()) {
                command = command.replace("{player}", playerName)
                        .replace("{uuid}", player != null ? player.getUuid().toString() : "?");

                this.plugin.getLogger().info("Dispatching command for player " + playerName + ": " + command);

                this.plugin.getPlatform().dispatchConsoleCommand(command);
            }
        }
    }
}
