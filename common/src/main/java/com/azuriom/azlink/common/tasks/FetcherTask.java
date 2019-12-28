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
import java.util.function.Function;
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

        if (!plugin.getConfig().isValid() || lastRequest.isAfter(now.minusSeconds(5))) {
            return;
        }

        lastRequest = now;

        plugin.getPlatform().executeSync(() -> {
            LocalDateTime currentTime = LocalDateTime.now();
            boolean sendFullData = currentTime.getMinute() % 15 == 0 && lastFullDataSent.isBefore(now.minusSeconds(60));

            ServerData data = plugin.getServerData(sendFullData);

            plugin.getPlatform().executeAsync(() -> sendData(data, sendFullData));
        });
    }

    private void sendData(ServerData data, boolean sendFullData) {
        try {
            WebsiteResponse response = plugin.getHttpClient().postData(data);

            if (response.getCommands().isEmpty()) {
                return;
            }

            plugin.getLogger().info("Dispatching commands to " + response.getCommands().size() + " players.");

            Map<String, CommandSender> players = plugin.getPlatform()
                    .getOnlinePlayers()
                    .collect(Collectors.toMap(cs -> cs.getName().toLowerCase(), Function.identity()));

            for (Map.Entry<String, List<String>> entry : response.getCommands().entrySet()) {
                String playerName = entry.getKey();
                List<String> commands = entry.getValue();
                CommandSender player = players.get(playerName.toLowerCase());

                if (player != null) {
                    playerName = player.getName();
                }

                for (String command : commands) {
                    command = command.replace("{name}", playerName)
                            .replace("{uuid}", player != null ? player.getUuid().toString() : "?");

                    plugin.getLogger().info("Dispatching command for player " + playerName + ": " + command);

                    plugin.getPlatform().dispatchConsoleCommand(command);
                }
            }

            if (sendFullData) {
                lastFullDataSent = Instant.now();
            }
        } catch (IOException e) {
            plugin.getLogger().error("Unable to send data to website: " + e.getMessage() + " - " + e.getClass().getName());
        }
    }
}
