package com.azuriom.azlink.bukkit.placeholders;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Taskable;
import org.bukkit.OfflinePlayer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BasePlaceholderExpansion extends PlaceholderExpansion implements Taskable {

    private static final String[] EMPTY_PARTS = new String[0];

    static final DecimalFormat FORMATTER = createDecimalFormat();
    static final DateTimeFormatter DATE_TIME_FORMATTER = createDateTimeFormatter();

    private final Map<String, PlaceholderProvider> providers = new LinkedHashMap<>();

    private final AzLinkBukkitPlugin plugin;

    public BasePlaceholderExpansion(AzLinkBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return this.plugin.getName();
    }

    @Override
    public String getAuthor() {
        return "Azuriom";
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
    public String getRequiredPlugin() {
        return this.plugin.getName();
    }

    @Override
    public List<String> getPlaceholders() {
        return this.providers.values().stream()
                .flatMap(p -> p.availablePlaceholders().stream())
                .collect(Collectors.toList());
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public void start() {
        this.providers.put("money", new MoneyPlaceholderProvider());

        if (plugin.getConfig().getBoolean("placeholders.shop")) {
            this.providers.put("shop", new ShopPlaceholderProvider(plugin));
            this.plugin.getLogger().info("Shop placeholders successfully enabled.");
        }

        if (plugin.getConfig().getBoolean("placeholders.vote")) {
            this.providers.put("vote", new VotePlaceholderProvider(plugin));
            this.plugin.getLogger().info("Vote placeholders successfully enabled.");
        }
    }

    @Override
    public void stop() {
        this.providers.values().forEach(PlaceholderProvider::disable);
        this.providers.clear();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String[] split = params.split("_", 2);
        PlaceholderProvider provider = this.providers.get(split[0]);
        String[] parts = split.length >= 2 ? split[1].split("_") : EMPTY_PARTS;

        return provider != null ? provider.evaluatePlaceholder(parts, player) : null;
    }

    public static void enable(AzLinkBukkitPlugin plugin) {
        if (new BasePlaceholderExpansion(plugin).register()) {
            plugin.getLogger().info("PlaceholderAPI expansion enabled.");
        } else {
            plugin.getLogger().warning("Unable to register PlaceholderAPI expansion.");
        }
    }

    private static DecimalFormat createDecimalFormat() {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        return new DecimalFormat("0.##", symbols);
    }

    private static DateTimeFormatter createDateTimeFormatter() {
        String pattern = PlaceholderAPIPlugin.getDateFormat().toPattern();

        return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
    }

    class MoneyPlaceholderProvider implements PlaceholderProvider {

        @Override
        public List<String> availablePlaceholders() {
            return Collections.singletonList("%azlink_money%");
        }

        @Override
        public String evaluatePlaceholder(String[] parts, OfflinePlayer player) {
            if (player == null || parts.length > 0) {
                return null;
            }

            return BasePlaceholderExpansion.this.plugin.getPlugin()
                    .getUserManager()
                    .getUserByName(player.getName())
                    .map(user -> formatAmount(user.getMoney()))
                    .orElse("?");
        }
    }
}
