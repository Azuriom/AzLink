package com.azuriom.azlink.common.tasks;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.data.ServerData;
import com.azuriom.azlink.common.data.WebsiteResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FetcherTask implements Runnable {

    private final AzLinkPlugin plugin;

    private Instant lastFullDataSent = Instant.MIN;

    public FetcherTask(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().isValid()) {
            return;
        }

        plugin.getPlatform().executeSync(() -> {
            LocalDateTime now = LocalDateTime.now();
            boolean sendFullData = now.getMinute() % 15 == 0 && lastFullDataSent.isBefore(Instant.now().minusSeconds(60));

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

            plugin.getLogger().info("Dispatching " + response.getCommands() + " commands.");

            Map<String, CommandSender> players = plugin.getPlatform()
                    .getOnlinePlayers()
                    .collect(Collectors.toMap(CommandSender::getName, Function.identity()));

            for (Map.Entry<String, String> entry : response.getCommands().entrySet()) {
                CommandSender player = players.get(entry.getKey());
                String command = entry.getValue()
                        .replace("{name}", player.getName())
                        .replace("{uuid}", player.getUuid().toString());

                plugin.getLogger().info("Dispatching command for player " + player.getName() + ": " + command);

                plugin.getPlatform().dispatchConsoleCommand(command);
            }

            if (sendFullData) {
                lastFullDataSent = Instant.now();
            }
        } catch (IOException e) {
            plugin.getLogger().error("Unable to send data to website: " + e.getMessage() + " - " + e.getClass().getName());
        }
    }
}
