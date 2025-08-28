package com.azuriom.azlink.bukkit.placeholders;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import com.azuriom.azlink.common.http.client.HttpClient;
import com.google.common.base.Strings;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopPlaceholderProvider implements PlaceholderProvider {

    private final List<ShopPayment> topCustomers = new ArrayList<>();
    private final List<ShopPayment> recentPayments = new ArrayList<>();
    private double goalProgress = 0.0;
    private double goalTotal = 0.0;
    private BukkitTask refreshTask;

    private final AzLinkBukkitPlugin plugin;

    public ShopPlaceholderProvider(AzLinkBukkitPlugin plugin) {
        this.plugin = plugin;

        this.refreshTask = this.plugin.getServer()
                .getScheduler()
                .runTaskTimer(plugin, this::refreshData, 1, 5 * 60 * 20); // Run every 5 minutes
    }

    @Override
    public List<String> availablePlaceholders() {
        return Arrays.asList(
                "%azlink_shop_goal_progress%",
                "%azlink_shop_goal_total%",
                "%azlink_shop_goal_percentage%",
                "%azlink_shop_top_[position]_name%",
                "%azlink_shop_top_[position]_amount%",
                "%azlink_shop_top_[position]_currency%",
                "%azlink_shop_recent_[position]_name%",
                "%azlink_shop_recent_[position]_amount%",
                "%azlink_shop_recent_[position]_currency%",
                "%azlink_shop_recent_[position]_timestamp%"
        );
    }

    @Override
    public void disable() {
        if (this.refreshTask != null) {
            this.refreshTask.cancel();
            this.refreshTask = null;
        }
    }

    @Override
    public String evaluatePlaceholder(String[] parts, OfflinePlayer player) {
        if (parts.length < 2) {
            return null; // Invalid placeholder format
        }

        try {
            switch (parts[0]) {
                case "goal":
                    return goalPlaceholder(parts[1]);
                case "top":
                    return topAndRecentPlaceholder("top", parts);
                case "recent":
                    return topAndRecentPlaceholder("recent", parts);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String goalPlaceholder(String type) {
        double proportion = this.goalTotal > 0 ? this.goalProgress / this.goalTotal : 0;

        switch (type) {
            case "progress":
                return String.valueOf(this.goalProgress);
            case "total":
                return String.valueOf(this.goalTotal);
            case "percentage":
                return String.format("%.2f%%", proportion * 100);
            case "progressbar":
                int length = 15;
                int filled = (int) Math.round(length * Math.min(proportion, 1));

                return Strings.repeat("■", filled) + ChatColor.GRAY + Strings.repeat("■", length - filled);
            default:
                return null;
        }
    }

    private String topAndRecentPlaceholder(String type, String[] parts) {
        if (parts.length < 3) {
            return null; // Invalid placeholder format
        }

        int position = Integer.parseInt(parts[1]);
        List<ShopPayment> list = type.equals("top") ? this.topCustomers : this.recentPayments;

        if (position < 1 || position > list.size()) {
            return ""; // Position out of bounds
        }

        ShopPayment payment = list.get(position - 1);
        switch (parts[2]) {
            case "name":
                return payment.user.name;
            case "currency":
                return payment.currency;
            case "amount":
                return formatAmount(payment.amount);
            case "timestamp":
                return formatTimestamp(payment.timestamp);
            default:
                return null;
        }
    }

    private void refreshData() {
        if (!this.plugin.getPlugin().isConfigured()) {
            return;
        }

        this.plugin.getPlugin()
                .getHttpClient()
                .request(HttpClient.RequestMethod.GET, "/shop/azlink", null, ShopResponse.class)
                .thenAccept(response -> {
                    this.goalProgress = response.goal.progress;
                    this.goalTotal = response.goal.total;

                    this.topCustomers.clear();
                    this.topCustomers.addAll(response.top);

                    this.recentPayments.clear();
                    this.recentPayments.addAll(response.recent);
                })
                .exceptionally(e -> {
                    this.plugin.getLogger().severe("Failed to refresh shop data: " + e.getMessage());
                    return null;
                });
    }

    public static class ShopResponse {
        public ShopMonthGoal goal = new ShopMonthGoal();
        public List<ShopPayment> top = new ArrayList<>();
        public List<ShopPayment> recent = new ArrayList<>();
    }

    public static class ShopMonthGoal {
        public double progress;
        public double total;
    }

    public static class ShopPayment {
        public double amount;
        public String currency;
        public ShopUser user;
        public Instant timestamp;
    }

    public static class ShopUser {
        public String name;
    }
}
