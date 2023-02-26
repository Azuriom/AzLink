package com.azuriom.azlink.bukkit.integrations;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MoneyPlaceholderExpansion extends PlaceholderExpansion {

    private static final DecimalFormat FORMATTER = createDecimalFormat();

    private final AzLinkBukkitPlugin plugin;

    public MoneyPlaceholderExpansion(AzLinkBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return this.plugin.getName();
    }

    @Override
    public String getAuthor() {
        return String.join(", ", this.plugin.getDescription().getAuthors());
    }

    @Override
    public String getIdentifier() {
        return "azlink";
    }

    @Override
    public String getVersion() {
        return this.plugin.getPluginVersion();
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("%azlink_money%");
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("money")) {
            return this.plugin.getPlugin()
                    .getUserManager()
                    .getUserByName(player.getName())
                    .map(user -> FORMATTER.format(user.getMoney()))
                    .orElse("?");
        }

        return null;
    }

    public static void enable(AzLinkBukkitPlugin plugin) {
        if (new MoneyPlaceholderExpansion(plugin).register()) {
            plugin.getLogger().info("PlaceholderAPI expansion enabled");
        }
    }

    private static DecimalFormat createDecimalFormat() {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        return new DecimalFormat("0.##", symbols);
    }
}
