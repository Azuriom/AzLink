package com.azuriom.azlink.bukkit.placeholders;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import com.azuriom.azlink.common.http.client.HttpClient;
import com.google.gson.annotations.SerializedName;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class VotePlaceholderProvider implements PlaceholderProvider, Runnable, Listener {

    private final Map<Integer, VoteSite> voteSites = new HashMap<>();
    private final Map<String, VoteUser> users = new HashMap<>();
    private final List<TopVoteUser> topVotes = new ArrayList<>();

    private volatile boolean pendingRefresh = true;
    private volatile Instant lastUpdate = Instant.MIN;
    private BukkitTask refreshTask;

    private final AzLinkBukkitPlugin plugin;

    public VotePlaceholderProvider(AzLinkBukkitPlugin plugin) {
        this.plugin = plugin;

        this.refreshTask = this.plugin.getServer()
                .getScheduler()
                .runTaskTimer(plugin, this, 1, 5 * 20);

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        refreshData();
    }

    @Override
    public void run() {
        if (this.pendingRefresh || this.lastUpdate.isBefore(Instant.now().minus(5, ChronoUnit.MINUTES))) {
            refreshData();
        }
    }

    @Override
    public List<String> availablePlaceholders() {
        return Arrays.asList(
                "%azlink_vote_can_total%",
                "%azlink_vote_can_[id]%",
                "%azlink_vote_can_[id]_delay%",
                "%azlink_vote_can_[id]_timestamp%",
                "%azlink_vote_user_votes%",
                "%azlink_vote_user_position%",
                "%azlink_vote_sites_count%",
                "%azlink_vote_sites_[id]_name%",
                "%azlink_vote_sites_[id]_url%",
                "%azlink_vote_top_[position]_name%",
                "%azlink_vote_top_[position]_votes%"
        );
    }

    @Override
    public void disable() {
        if (this.refreshTask != null) {
            this.refreshTask.cancel();
            this.refreshTask = null;
        }

        HandlerList.unregisterAll(this);
    }

    @Override
    public String evaluatePlaceholder(String[] parts, OfflinePlayer player) {
        if (parts.length < 2) {
            return null;
        }

        try {
            switch (parts[0]) {
                case "user":
                    return userPlaceholder(parts[1], player);
                case "can":
                    return userCanPlaceholder(parts, player);
                case "top":
                    return topPlaceholder(parts);
                case "sites":
                    return sitePlaceholder(parts);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String userCanPlaceholder(String[] parts, OfflinePlayer player) throws NumberFormatException {
        if (player == null) {
            return null;
        }

        VoteUser user = getUserFromPlayer(player);

        if (user == null) {
            return ""; // Empty value to avoid showing placeholder if user not loaded
        }

        if (parts[1].equals("total")) {
            return Integer.toString(user.totalAvailableVotes());
        }

        int siteId = Integer.parseInt(parts[1]);

        if (parts.length == 2) {
            return formatBoolean(user.canVote(siteId));
        }

        if (parts.length == 3 && parts[2].equals("delay")) {
            return formatDuration(user.nextVoteDelay(siteId));
        }

        if (parts.length == 3 && parts[2].equals("timestamp")) {
            return formatTimestamp(user.nextVoteTime(siteId));
        }

        return null;
    }

    private String sitePlaceholder(String[] parts) throws NumberFormatException {
        if (parts[1].equals("count")) {
            return Integer.toString(voteSites.size());
        }

        if (parts.length < 3) {
            return null;
        }

        int id = Integer.parseInt(parts[1]);
        VoteSite site = voteSites.get(id);

        if (site == null) {
            return "<unknown>";
        }

        switch (parts[2]) {
            case "name":
                return site.name;
            case "url":
                return site.url;
            default:
                return null;
        }
    }

    private String userPlaceholder(String type, OfflinePlayer player) {
        if (player == null) {
            return null;
        }

        VoteUser user = getUserFromPlayer(player);

        if (user == null) {
            return ""; // Empty value to avoid showing placeholder if user not loaded
        }

        switch (type) {
            case "votes":
                return Integer.toString(user.votes);
            case "position":
                return Integer.toString(user.position);
            default:
                return null;
        }
    }

    private String topPlaceholder(String[] parts) throws NumberFormatException {
        if (parts.length < 3) {
            return null;
        }

        int position = Integer.parseInt(parts[1]);

        if (position < 1 || position > this.topVotes.size()) {
            return "";
        }

        TopVoteUser user = this.topVotes.get(position - 1);

        switch (parts[2]) {
            case "name":
                return user.name;
            case "votes":
                return Integer.toString(user.votes);
            default:
                return null;
        }
    }

    private void refreshData() {
        if (!this.plugin.getPlugin().isConfigured()) {
            return;
        }

        if (this.lastUpdate.isAfter(Instant.now().minusSeconds(5))) {
            this.pendingRefresh = true; // Will be refreshed later by the scheduler
            return;
        }

        this.lastUpdate = Instant.now();

        String names = this.plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.joining(","));

        this.plugin.getPlugin()
                .getHttpClient()
                .request(HttpClient.RequestMethod.GET, "/vote/azlink?usernames=" + names, null, VoteResponse.class)
                .thenAccept(response -> {
                    this.pendingRefresh = false;

                    this.voteSites.clear();
                    this.users.clear();
                    this.topVotes.clear();

                    for (VoteSite site : response.sites) {
                        this.voteSites.put(site.id, site);
                    }

                    for (VoteUser user : response.users) {
                        this.users.put(user.name.toLowerCase(Locale.ROOT), user);
                    }
                    this.topVotes.addAll(response.topVotes);
                })
                .exceptionally(e -> {
                    this.plugin.getLogger().severe("Failed to refresh vote data: " + e.getMessage());
                    return null;
                });
    }

    private VoteUser getUserFromPlayer(OfflinePlayer player) {
        return this.users.get(player.getName().toLowerCase(Locale.ROOT));
    }

    private String formatDuration(Duration duration) {
        String format = this.plugin.getConfig().getString("placeholders.duration-format", "%H:%M:%S");

        return formatDuration(format, duration);
    }

    public static class VoteResponse {
        public List<VoteSite> sites = new ArrayList<>();
        public List<VoteUser> users = new ArrayList<>();
        @SerializedName("top_votes")
        public List<TopVoteUser> topVotes = new ArrayList<>();
    }

    public static class VoteUser {
        public String name;
        public int votes;
        public int position;
        public Map<Integer, Instant> sites = new HashMap<>();

        public int totalAvailableVotes() {
            return (int) this.sites.values().stream()
                    .filter(time -> time == null || time.isBefore(Instant.now()))
                    .count();
        }

        public boolean canVote(int siteId) {
            Instant nextVoteTime = this.sites.get(siteId);
            return nextVoteTime == null || nextVoteTime.isBefore(Instant.now());
        }

        public Instant nextVoteTime(int siteId) {
            Instant nextVoteTime = this.sites.get(siteId);
            if (nextVoteTime == null || nextVoteTime.isBefore(Instant.now())) {
                return null; // No delay if no next vote time
            }
            return nextVoteTime;
        }

        public Duration nextVoteDelay(int siteId) {
            Instant nextVoteTime = this.sites.get(siteId);
            if (nextVoteTime == null || nextVoteTime.isBefore(Instant.now())) {
                return Duration.ZERO; // No delay if no next vote time
            }
            return Duration.between(Instant.now(), nextVoteTime);
        }
    }

    public static class TopVoteUser {
        public String name;
        public int votes;
    }

    public static class VoteSite {
        public int id;
        public String name;
        public String url;
    }
}
