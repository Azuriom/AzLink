package com.azuriom.azlink.common.tasks;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.data.ServerData;
import com.azuriom.azlink.common.data.WebsiteResponse;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FetcherTask implements Runnable {

    private final AzLinkPlugin plugin;

    private int count = 0;

    public FetcherTask(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    public void runTask() {
        count++;

        run();
    }

    @Override
    public void run() {
        plugin.getPlatform().executeSync(() -> {
            ServerData data = plugin.getServerData(count % 15 == 0);

            plugin.getPlatform().executeAsync(() -> sendData(data));
        });
    }

    private void sendData(ServerData data) {
        try {
            WebsiteResponse response = plugin.getHttpClient().postData(data);

            if (response.getCommands().isEmpty()) {
                return;
            }

            plugin.getPlatform().getLoggerAdapter().info("Dispatching " + response.getCommands() + " commands.");

            Map<String, CommandSender> players = plugin.getPlatform()
                    .getOnlinePlayers()
                    .collect(Collectors.toMap(CommandSender::getName, Function.identity()));

            for (Map.Entry<String, String> entry : response.getCommands().entrySet()) {
                CommandSender player = players.get(entry.getKey());
                String command = entry.getValue()
                        .replace("{name}", player.getName())
                        .replace("{uuid}", player.getUuid().toString());

                plugin.getPlatform().getLoggerAdapter().info("Dispatching command for player " + player.getName() + ": " + command);

                plugin.getPlatform().dispatchConsoleCommand(command);
            }
        } catch (IOException e) {
            plugin.getPlatform().getLoggerAdapter().error("Unable to send data to website", e);
        }
    }
}
