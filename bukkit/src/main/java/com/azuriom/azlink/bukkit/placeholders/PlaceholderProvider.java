package com.azuriom.azlink.bukkit.placeholders;

import com.google.common.base.Strings;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface PlaceholderProvider {

    List<String> availablePlaceholders();

    String evaluatePlaceholder(String[] parts, OfflinePlayer player);

    default void disable() {}

    default String formatBoolean(boolean bool) {
        return bool ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }

    default String formatAmount(double amount) {
        return BasePlaceholderExpansion.FORMATTER.format(amount);
    }

    default String formatTimestamp(Instant instant) {
        if (instant == null) {
            return "0";
        }

        return BasePlaceholderExpansion.DATE_TIME_FORMATTER.format(instant);
    }

    default String formatDuration(String format, Duration duration) {
        if (duration.isZero() || duration.isNegative()) {
            return "0";
        }

        long minutes = (duration.getSeconds() % 3600) / 60;
        long hours = duration.toHours();
        long seconds = duration.getSeconds() % 60;

        return format.replace("%H", String.format("%02d", hours))
                .replace("%M", String.format("%02d", minutes))
                .replace("%S", String.format("%02d", seconds))
                .replace("%h", Long.toString(hours))
                .replace("%m", Long.toString(minutes))
                .replace("%s", Long.toString(seconds));
    }

    default String formatProgressBar(double progress, ConfigurationSection config) {
        int totalLength = config.getInt("progress-bar.length", 15);
        String fill = config.getString("progress-bar.fill", "█");
        String separator = config.getString("progress-bar.separator", "&7");
        int filled = (int) Math.round(totalLength * Math.max(Math.min(progress, 1), 0));
        String sep = ChatColor.translateAlternateColorCodes('&', separator);

        return Strings.repeat(fill, filled) + sep + Strings.repeat(fill, totalLength - filled);
    }
}
