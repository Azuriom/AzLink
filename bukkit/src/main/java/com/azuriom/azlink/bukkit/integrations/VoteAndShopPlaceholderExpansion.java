package com.azuriom.azlink.bukkit.integrations;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import com.azuriom.azlink.common.data.UserInfo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

public class VoteAndShopPlaceholderExpansion extends PlaceholderExpansion {

    private final AzLinkBukkitPlugin plugin;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private final Map<UUID, Map<String, Object>> playerDataCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastFetchTime = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = TimeUnit.MINUTES.toMillis(5); 
    

    private String dbHost = "localhost";
    private String dbPort = "3307";
    private String dbName = "test"; 
    private String dbUser = "root"; 
    private String dbPassword = ""; 

    private boolean debug = false;
    
    private List<Map<String, Object>> voteSitesCache = new ArrayList<>();
    private long voteSitesCacheTime = 0;
    private static final long VOTE_SITES_CACHE_DURATION = TimeUnit.MINUTES.toMillis(10);

    private List<Map<String, Object>> topVotersCache = new ArrayList<>();
    private long topVotersCacheTime = 0;

    public VoteAndShopPlaceholderExpansion(AzLinkBukkitPlugin plugin) {
        this.plugin = plugin;
        loadDatabaseConfig();
        loadVoteSites();
        loadTopVoters();
    }

    public static void enable(AzLinkBukkitPlugin plugin) {
        if (new VoteAndShopPlaceholderExpansion(plugin).register()) {
            plugin.getLogger().info("Vote & Shop PlaceholderAPI expansion enabled.");
            
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                    try {
                        plugin.getLogger().info("Preloading data for " + player.getName());
                        Optional<UserInfo> userInfoOpt = plugin.getPlugin().getUserManager().getUserByName(player.getName());
                        if (userInfoOpt.isPresent()) {
                            VoteAndShopPlaceholderExpansion expansion = 
                                (VoteAndShopPlaceholderExpansion) me.clip.placeholderapi.PlaceholderAPIPlugin.getInstance()
                                    .getLocalExpansionManager().getExpansion("azlink");
                            if (expansion != null) {
                                expansion.fetchPlayerDataSync(player.getUniqueId(), player.getName());
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to preload data for " + player.getName() + ": " + e.getMessage());
                    }
                }
                plugin.getLogger().info("Finished preloading player data");
            });
        }
    }
    
    private void loadDatabaseConfig() {
        try {
            dbHost = plugin.getConfig().getString("database.host", dbHost);
            dbPort = plugin.getConfig().getString("database.port", dbPort);
            dbName = plugin.getConfig().getString("database.name", dbName);
            dbUser = plugin.getConfig().getString("database.username", dbUser);
            dbPassword = plugin.getConfig().getString("database.password", "");
            debug = plugin.getConfig().getBoolean("database.debug", false);
            
            plugin.getLogger().info("Database configuration loaded");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load database configuration: " + e.getMessage());
        }
    }
    
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + 
                     "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        
        if (debug) {
            plugin.getLogger().info("Connecting to database at " + dbHost + ":" + dbPort + "/" + dbName);
        }
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                plugin.getLogger().severe("MySQL JDBC driver not found. Please make sure it's properly installed.");
                throw new SQLException("MySQL JDBC driver not found", ex);
            }
        }
        
        Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
        if (debug && connection != null) {
            plugin.getLogger().info("Successfully connected to database");
        }
        return connection;
    }
    
    private void loadVoteSites() {
        if (System.currentTimeMillis() - voteSitesCacheTime < VOTE_SITES_CACHE_DURATION) {
            return;
        }
        
        List<Map<String, Object>> sites = new ArrayList<>();
        try (Connection conn = getConnection()) {
            if (conn == null) {
                plugin.getLogger().warning("Failed to connect to database");
                return;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, name, url, vote_delay, is_enabled FROM vote_sites WHERE is_enabled = 1")) {
                
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Map<String, Object> site = new HashMap<>();
                    site.put("id", rs.getInt("id"));
                    site.put("name", rs.getString("name"));
                    site.put("url", rs.getString("url"));
                    site.put("vote_delay", rs.getInt("vote_delay"));
                    site.put("is_enabled", rs.getBoolean("is_enabled"));
                    sites.add(site);
                }
                
                voteSitesCache = sites;
                voteSitesCacheTime = System.currentTimeMillis();
                plugin.getLogger().info("Loaded " + sites.size() + " vote sites");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error loading vote sites: " + e.getMessage());
            if (e.getMessage().contains("Communications link failure") || 
                e.getMessage().contains("Connection refused")) {
                plugin.getLogger().warning("Database connection failed. Please check your database settings in config.yml");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Unexpected error loading vote sites: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadTopVoters() {
        List<Map<String, Object>> topVoters = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.id, u.name, COUNT(v.id) as vote_count " +
                "FROM users u " +
                "JOIN vote_votes v ON u.id = v.user_id " +
                "WHERE v.created_at >= DATE_FORMAT(NOW() ,'%Y-%m-01') " +
                "GROUP BY u.id, u.name " +
                "ORDER BY vote_count DESC " +
                "LIMIT 10")) {
            
            ResultSet rs = stmt.executeQuery();
            int position = 1;
            while (rs.next()) {
                Map<String, Object> voter = new HashMap<>();
                voter.put("id", rs.getInt("id"));
                voter.put("name", rs.getString("name"));
                voter.put("vote_count", rs.getInt("vote_count"));
                voter.put("position", position++);
                topVoters.add(voter);
            }
            
            topVotersCache = topVoters;
            topVotersCacheTime = System.currentTimeMillis();
            if (debug) {
                plugin.getLogger().info("Loaded " + topVoters.size() + " top voters");
                for (Map<String, Object> voter : topVoters) {
                    plugin.getLogger().info("Top voter: " + voter.get("position") + ". " + 
                                           voter.get("name") + " - " + voter.get("vote_count") + " votes");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error loading top voters: " + e.getMessage());
        }
    }

    @Override
    public String getIdentifier() {
        return "azlink";
    }

    @Override
    public String getAuthor() {
        return "SenaxZzOnYt";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    private void fetchPlayerData(UUID uuid, String playerName) {
        Long lastFetch = lastFetchTime.get(playerName);
        if (lastFetch != null && System.currentTimeMillis() - lastFetch < CACHE_DURATION_MS) {
            return;
        }

        lastFetchTime.put(playerName, System.currentTimeMillis());
        plugin.getLogger().info("Scheduling async data refresh for player " + playerName);

        CompletableFuture.runAsync(() -> {
            try {
                fetchPlayerDataInternal(uuid, playerName);
            } catch (Exception e) {
                plugin.getLogger().warning("Error fetching player data: " + e.getMessage());
                e.printStackTrace();
            }
        }, plugin.getPlugin().getScheduler().asyncExecutor());
    }
    
    private void fetchPlayerDataSync(UUID uuid, String playerName) {
        lastFetchTime.put(playerName, System.currentTimeMillis());
        if (debug) {
            plugin.getLogger().info("Fetching data synchronously for player " + playerName);
        }
        fetchPlayerDataInternal(uuid, playerName);
    }
    
    private void fetchPlayerDataInternal(UUID uuid, String playerName) {
        try {
            Optional<UserInfo> userInfoOpt = plugin.getPlugin().getUserManager().getUserByName(playerName);
            int userId = -1;
            
            if (userInfoOpt.isPresent()) {
                UserInfo userInfo = userInfoOpt.get();
                userId = userInfo.getId();
                
                if (debug) {
                    plugin.getLogger().info("Found UserInfo for " + playerName + " with ID " + userId);
                }
            } else {
                // Try to find the user directly in the database
                userId = findUserIdInDatabase(playerName);
                if (userId > 0) {
                    plugin.getLogger().info("Found user " + playerName + " directly in database with ID " + userId);
                } else {
                    plugin.getLogger().warning("UserInfo not found for player " + playerName + " and couldn't find in database");
                    return;
                }
            }
            
            Map<String, Object> playerData = new HashMap<>();
            
            try {
                loadVoteSites();
                Map<String, Object> voteData = fetchUserVoteData(userId, playerName);
                if (voteData != null) {
                    playerData.put("votes", voteData);
                    if (debug) {
                        plugin.getLogger().info("Loaded vote data for " + playerName + ": " + voteData.size() + " entries");
                        if (voteData.containsKey("sites")) {
                            List<Map<String, Object>> sites = getListOfMaps(voteData, "sites");
                            if (sites != null) {
                                plugin.getLogger().info("Loaded " + sites.size() + " vote sites for player");
                                for (Map<String, Object> site : sites) {
                                    plugin.getLogger().info("Site: id=" + site.get("id") + 
                                                           ", name=" + site.get("name") + 
                                                           ", available=" + site.get("available") + 
                                                           ", cooldown=" + site.get("cooldown"));
                                }
                            }
                        }
                        if (voteData.containsKey("position")) {
                            plugin.getLogger().info("Player vote position: " + voteData.get("position"));
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading vote data: " + e.getMessage());
            }

            try {
                Map<String, Object> shopData = fetchUserShopData(userId);
                if (shopData != null) {
                    playerData.put("shop", shopData);
                    if (debug) {
                        plugin.getLogger().info("Loaded shop data for " + playerName);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading shop data: " + e.getMessage());
            }
            
            playerDataCache.put(uuid, playerData);
            if (debug) {
                plugin.getLogger().info("Successfully loaded data for player " + playerName);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error in fetchPlayerDataInternal: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int findUserIdInDatabase(String playerName) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM users WHERE name = ? LIMIT 1")) {
            
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error finding user in database: " + e.getMessage());
        }
        return -1;
    }
    
    private Map<String, Object> fetchUserVoteData(int userId, String playerName) {
        Map<String, Object> voteData = new HashMap<>();
        
        try {
            voteData.put("user_id", userId);
            
            int totalSites = voteSitesCache.size();
            voteData.put("total_sites", totalSites);
            List<Map<String, Object>> sitesList = new ArrayList<>();
            int availableSites = 0;
            
            for (Map<String, Object> site : voteSitesCache) {
                int siteId = (int) site.get("id");
                String siteName = (String) site.get("name");
                int voteDelay = (int) site.get("vote_delay");

                boolean hasVoted = false;
                LocalDateTime lastVoteTime = null;
                
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                        "SELECT created_at FROM vote_votes " +
                        "WHERE user_id = ? AND site_id = ? " +
                        "ORDER BY created_at DESC LIMIT 1")) {
                    
                    stmt.setInt(1, userId);
                    stmt.setInt(2, siteId);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        hasVoted = true;
                        lastVoteTime = rs.getTimestamp("created_at").toLocalDateTime();
                    }
                }

                boolean available = true;
                String cooldown = "True";
                long remainingSeconds = 0;
                
                if (hasVoted && lastVoteTime != null) {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime nextVoteTime = lastVoteTime.plusHours(voteDelay);
                    
                    if (now.isBefore(nextVoteTime)) {
                        available = false;
                        Duration remainingTime = Duration.between(now, nextVoteTime);
                        remainingSeconds = remainingTime.getSeconds();
                        
                        if (remainingSeconds <= 0) {
                            available = true;
                            cooldown = "True";
                            remainingSeconds = 0;
                        } else {
                            long hours = remainingSeconds / 3600;
                            long minutes = (remainingSeconds % 3600) / 60;
                            long seconds = remainingSeconds % 60;
                            
                            if (hours > 0) {
                                cooldown = hours + "h " + minutes + "m " + seconds + "s";
                            } else if (minutes > 0) {
                                cooldown = minutes + "m " + seconds + "s";
                            } else {
                                cooldown = seconds + "s";
                            }
                        }
                    } else {
                        available = true;
                        cooldown = "True";
                        remainingSeconds = 0;
                    }
                }
                
                if (available) {
                    availableSites++;
                }
                
                Map<String, Object> siteInfo = new HashMap<>();
                siteInfo.put("id", siteId);
                siteInfo.put("name", siteName);
                siteInfo.put("available", available);
                siteInfo.put("cooldown", cooldown);
                siteInfo.put("remaining_seconds", remainingSeconds);
                sitesList.add(siteInfo);
            }
            
            voteData.put("available_sites", availableSites);
            voteData.put("sites", sitesList);

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(DISTINCT higher.total_votes) + 1 AS rank " +
                    "FROM (" +
                    "  SELECT user_id, COUNT(*) AS total_votes " +
                    "  FROM vote_votes " +
                    "  WHERE created_at >= DATE_FORMAT(NOW() ,'%Y-%m-01') " +
                    "  GROUP BY user_id" +
                    ") AS user_votes " +
                    "JOIN (" +
                    "  SELECT user_id, COUNT(*) AS total_votes " +
                    "  FROM vote_votes " +
                    "  WHERE created_at >= DATE_FORMAT(NOW() ,'%Y-%m-01') " +
                    "  GROUP BY user_id" +
                    ") AS higher ON higher.total_votes > user_votes.total_votes " +
                    "WHERE user_votes.user_id = ?")) {
                
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int position = rs.getInt("rank");
                    voteData.put("position", position);
                    if (debug) {
                        plugin.getLogger().info("Player " + playerName + " position: " + position);
                    }
                } else {
                    voteData.put("position", 0);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error getting player position: " + e.getMessage());
                voteData.put("position", 0);
            }

            loadTopVoters();
            voteData.put("top_sites", topVotersCache);
            
            return voteData;
        } catch (Exception e) {
            plugin.getLogger().warning("Error fetching user vote data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private Map<String, Object> fetchUserShopData(int userId) {
        Map<String, Object> shopData = new HashMap<>();
        
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT SUM(pi.price * pi.quantity) as total " +
                    "FROM shop_payments p " +
                    "JOIN shop_payment_items pi ON p.id = pi.payment_id " +
                    "WHERE p.user_id = ? AND p.status = 'completed'")) {
                
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    shopData.put("total_spent", total);
                } else {
                    shopData.put("total_spent", 0.0);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT SUM(pi.price * pi.quantity) as total " +
                    "FROM shop_payments p " +
                    "JOIN shop_payment_items pi ON p.id = pi.payment_id " +
                    "WHERE p.user_id = ? AND p.status = 'completed' " +
                    "AND p.created_at >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)")) {
                
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    shopData.put("week", total != 0 ? total : 0.0);
                } else {
                    shopData.put("week", 0.0);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT SUM(pi.price * pi.quantity) as total " +
                    "FROM shop_payments p " +
                    "JOIN shop_payment_items pi ON p.id = pi.payment_id " +
                    "WHERE p.user_id = ? AND p.status = 'completed' " +
                    "AND p.created_at >= DATE_FORMAT(NOW() ,'%Y-%m-01')")) {
                
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    shopData.put("month", total != 0 ? total : 0.0);
                } else {
                    shopData.put("month", 0.0);
                }
            }
            
            return shopData;
        } catch (Exception e) {
            plugin.getLogger().warning("Error fetching user shop data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null || !player.hasPlayedBefore()) {
            return "";
        }

        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        
        if (debug) {
            plugin.getLogger().info("Processing placeholder: " + identifier + " for player: " + playerName);
        }

        Long lastFetch = lastFetchTime.get(playerName);
        boolean needsSync = lastFetch == null || System.currentTimeMillis() - lastFetch >= CACHE_DURATION_MS;
        
        if (needsSync) {
            try {
                fetchPlayerDataSync(uuid, playerName);
            } catch (Exception e) {
                plugin.getLogger().warning("Error fetching player data synchronously: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            fetchPlayerData(uuid, playerName);
        }

        Map<String, Object> playerData = playerDataCache.get(uuid);
        if (playerData == null) {
            plugin.getLogger().info("Player data cache is empty for " + player.getName());
            return getDefaultValue(identifier);
        }

        Map<String, Object> votes = getSection(playerData, "votes");
        Map<String, Object> shop = getSection(playerData, "shop");
        String id = identifier.toLowerCase(Locale.ROOT);

        String result = null;
        
        if ("vote_total_sites".equals(id)) {
            result = getIntString(votes, "total_sites");
        } else if ("vote_total_available".equals(id)) {
            int available = Integer.parseInt(getIntString(votes, "available_sites"));
            int total = Integer.parseInt(getIntString(votes, "total_sites"));
            
            if (available == 0) {
                result = "False";
            } else {
                result = String.valueOf(available);
            }
        } else if ("shop_total_spent".equals(id)) {
            result = getFormatted(shop, "total_spent");
        } else if ("shop_total_week".equals(id)) {
            result = getFormatted(shop, "week");
        } else if ("shop_total_month".equals(id)) {
            result = getFormatted(shop, "month");
        } else if (id.startsWith("vote_")) {
            if (id.endsWith("_cooldown")) {
                result = getVoteSiteCooldown(votes, id);
            } else if (id.endsWith("_available")) {
                result = getVoteSiteBool(votes, id, "available");
            } else if (id.startsWith("vote_name_")) {
                int position = parseIndex(id, "vote_name_");
                result = getVoteTopName(votes, position);
            } else if (id.equals("vote_position")) {
                if (votes != null && votes.containsKey("position")) {
                    result = String.valueOf(votes.get("position"));
                } else {
                    result = "0";
                }
            } else if (id.startsWith("vote_position_")) {
                int position = parseIndex(id, "vote_position_");
                result = getVoteCountByPosition(position);
            } else if (id.startsWith("vote_user_")) {
                int userId = parseIndex(id, "vote_user_");
                result = getPlayerPositionById(userId);
            } else if (id.startsWith("vote_count_position_")) {
                int position = parseIndex(id, "vote_count_position_");
                result = getVoteCountByPosition(position);
            } else if (id.startsWith("vote_position_user_")) {
                String targetPlayerName = id.substring("vote_position_user_".length());
                result = getPlayerPositionByName(targetPlayerName);
            } else if (id.startsWith("vote_site_")) {
                result = handleVoteSitePlaceholder(votes, id);
            }
        }
        
        if (debug && result != null) {
            plugin.getLogger().info("Placeholder " + identifier + " result: " + result);
        }

        return result;
    }

    private String getDefaultValue(String identifier) {
        String id = identifier.toLowerCase(Locale.ROOT);

        if ("vote_total_sites".equals(id)) {
            return "0";
        } else if ("vote_total_available".equals(id)) {
            return "False";
        } else if ("shop_total_spent".equals(id)) {
            return "0.00";
        } else if ("shop_total_week".equals(id)) {
            return "0.00";
        } else if ("shop_total_month".equals(id)) {
            return "0.00";
        } else if (id.endsWith("_cooldown")) {
            return "True";
        } else if (id.endsWith("_available")) {
            return "False";
        } else if (id.startsWith("vote_name_")) {
            return "N/A";
        } else if (id.equals("vote_position")) {
            return "0";
        } else if (id.startsWith("vote_position_")) {
            return "0";
        }

        return "";
    }

    private String getFormatted(Map<String, Object> map, String key) {
        if (map == null) return "0";
        Object value = map.get(key);
        if (value instanceof Number) {
            return DECIMAL_FORMAT.format(((Number) value).doubleValue());
        }
        return "0";
    }

    private String getIntString(Map<String, Object> map, String key) {
        if (map == null) return "0";
        Object val = map.get(key);
        return val != null ? val.toString() : "0";
    }

    private String getVoteSiteBool(Map<String, Object> votes, String id, String field) {
        if (votes == null) return "False";

        String siteId = extractSiteId(id, "_" + field);
        if (debug) {
            plugin.getLogger().info("Looking for site ID: " + siteId + " for field: " + field);
        }
        
        List<Map<String, Object>> sites = getListOfMaps(votes, "sites");
        if (sites == null) {
            if (debug) plugin.getLogger().info("No sites found in vote data");
            return "False";
        }

        for (Map<String, Object> site : sites) {
            String currentId = String.valueOf(site.get("id"));
            if (siteId.equalsIgnoreCase(currentId)) {
                Object value = site.get(field);
                boolean result = Boolean.TRUE.equals(value);
                if (debug) {
                    plugin.getLogger().info("Found site " + currentId + ", " + field + " = " + value + " (returning " + (result ? "True" : "False") + ")");
                }
                return result ? "True" : "False";
            }
        }
        
        if (debug) plugin.getLogger().info("Site ID " + siteId + " not found");
        return "False";
    }
    
    private String getVoteSiteCooldown(Map<String, Object> votes, String id) {
        if (votes == null) return "True";

        String siteIdStr = extractSiteId(id, "_cooldown");
        if (debug) {
            plugin.getLogger().info("Looking for site ID: " + siteIdStr + " for cooldown");
        }
        
        int userId = -1;
        if (votes.containsKey("user_id")) {
            userId = (int) votes.get("user_id");
        }
        
        if (userId <= 0) {
            if (debug) {
                plugin.getLogger().info("User ID not found in vote data, cannot get cooldown");
            }
            return "True";
        }
        
        int siteId;
        try {
            siteId = Integer.parseInt(siteIdStr);
        } catch (NumberFormatException e) {
            if (debug) {
                plugin.getLogger().warning("Invalid site ID: " + siteIdStr);
            }
            return "True";
        }
        
        try (Connection conn = getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM vote_sites WHERE id = ?")) {
                checkStmt.setInt(1, siteId);
                ResultSet checkRs = checkStmt.executeQuery();
                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    if (debug) {
                        plugin.getLogger().warning("Site ID " + siteId + " does not exist in vote_sites table!");
                    }
                    return "True";
                }
            }
            
            String sql = 
                "SELECT vs.vote_delay, vv.last_vote " +
                "FROM vote_sites vs " +
                "LEFT JOIN ( " +
                "  SELECT site_id, MAX(created_at) AS last_vote " +
                "  FROM vote_votes " +
                "  WHERE user_id = ? " +
                "  GROUP BY site_id " +
                ") vv ON vs.id = vv.site_id " +
                "WHERE vs.id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, siteId);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int voteDelay = rs.getInt("vote_delay");
                    java.sql.Timestamp lastVoteTimestamp = rs.getTimestamp("last_vote");
                    
                    if (debug) {
                        plugin.getLogger().info("Site " + siteId + " - Vote delay: " + voteDelay + "h, Last vote: " + lastVoteTimestamp);
                        plugin.getLogger().info("Expected vote delay for site " + siteId + " should be how many hours? Current DB value: " + voteDelay + "h");
                    }
                    if (lastVoteTimestamp == null) {
                        if (debug) {
                            plugin.getLogger().info("No previous vote found for site " + siteId + ", returning True");
                        }
                        return "True";
                    }

                    LocalDateTime lastVoteTime = lastVoteTimestamp.toLocalDateTime();
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime nextVoteTime = lastVoteTime.plusHours(voteDelay);
                    
                    if (debug) {
                        plugin.getLogger().info("Last vote: " + lastVoteTime + ", Next vote allowed: " + nextVoteTime + ", Now: " + now);
                    }
                    
                    if (now.isAfter(nextVoteTime) || now.isEqual(nextVoteTime)) {
                        if (debug) {
                            plugin.getLogger().info("Cooldown expired, player can vote");
                        }
                        return "True";
                    }
                    
                    Duration remainingTime = Duration.between(now, nextVoteTime);
                    long totalSeconds = remainingTime.getSeconds();
                    
                    if (totalSeconds <= 0) {
                        return "True";
                    }
                    
                    long hours = totalSeconds / 3600;
                    long minutes = (totalSeconds % 3600) / 60;
                    long seconds = totalSeconds % 60;
                    
                    String cooldownFormat;
                    if (hours > 0) {
                        cooldownFormat = hours + "h " + minutes + "m " + seconds + "s";
                    } else if (minutes > 0) {
                        cooldownFormat = minutes + "m " + seconds + "s";
                    } else {
                        cooldownFormat = seconds + "s";
                    }
                    
                    if (debug) {
                        plugin.getLogger().info("Cooldown remaining: " + cooldownFormat + " (" + totalSeconds + " seconds)");
                    }
                    
                    return cooldownFormat;
                } else {
                    if (debug) {
                        plugin.getLogger().warning("No data returned for site " + siteId + " - site may not exist");
                    }
                    return "True";
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error in SQL cooldown query: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting cooldown for site " + siteId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        List<Map<String, Object>> sites = getListOfMaps(votes, "sites");
        if (sites == null) {
            if (debug) plugin.getLogger().info("No sites found in vote data");
            return "True";
        }

        for (Map<String, Object> site : sites) {
            String currentId = String.valueOf(site.get("id"));
            if (siteIdStr.equalsIgnoreCase(currentId)) {
                Object cooldown = site.get("cooldown");
                
                if (debug) {
                    plugin.getLogger().info("Found site " + currentId + ", cooldown = " + cooldown);
                }
                
                return String.valueOf(cooldown);
            }
        }
        
        if (debug) plugin.getLogger().info("Site ID " + siteIdStr + " not found");
        return "True";
    }
    
    private String extractSiteId(String id, String suffix) {
        return id.replace("vote_", "").replace(suffix, "");
    }

    private int parseIndex(String id, String prefix) {
        try {
            return Integer.parseInt(id.substring(prefix.length()));
        } catch (Exception e) {
            return 0;
        }
    }

    private String getVoteTopPosition(Map<String, Object> votes, int index) {
        if (votes == null || index < 1 || index > 10) return "0";
        
        List<Map<String, Object>> topSites = getListOfMaps(votes, "top_sites");
        if (topSites == null || topSites.isEmpty() || index > topSites.size()) return "0";
        
        Map<String, Object> voter = topSites.get(index - 1);
        if (voter.containsKey("vote_count")) {
            return String.valueOf(voter.get("vote_count"));
        }
        
        return "0";
    }

    private String getVoteTopName(Map<String, Object> votes, int index) {
        if (votes == null) return "N/A";

        List<Map<String, Object>> topSites = getListOfMaps(votes, "top_sites");
        if (topSites == null || index < 1 || index > topSites.size()) return "N/A";

        Map<String, Object> site = topSites.get(index - 1);
        return site.getOrDefault("name", "N/A").toString();
    }

    private String getVoteCountByPosition(int position) {
        if (position <= 0) return "0";
        
        try (Connection conn = getConnection()) {
            String sql = 
                "SELECT vote_count FROM (" +
                "  SELECT @rank := @rank + 1 AS position, user_id, vote_count " +
                "  FROM (" +
                "    SELECT user_id, COUNT(*) AS vote_count " +
                "    FROM vote_votes " +
                "    WHERE created_at >= DATE_FORMAT(NOW() ,'%Y-%m-01') " +
                "    GROUP BY user_id " +
                "    ORDER BY vote_count DESC" +
                "  ) ranked, (SELECT @rank := 0) r" +
                ") ranked_with_position " +
                "WHERE position = ?";
            
            try (PreparedStatement initStmt = conn.prepareStatement("SET @rank := 0")) {
                initStmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, position);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int voteCount = rs.getInt("vote_count");
                    if (debug) {
                        plugin.getLogger().info("Found " + voteCount + " votes for position " + position);
                    }
                    return String.valueOf(voteCount);
                } else if (debug) {
                    plugin.getLogger().info("No votes found for position " + position);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting vote count for position " + position + ": " + e.getMessage());
        }
        
        return "0";
    }

    private String getPlayerPositionByName(String playerName) {
        if (playerName == null || playerName.isEmpty()) return "0";
        
        try {
            int userId = -1;
            Optional<UserInfo> userInfoOpt = plugin.getPlugin().getUserManager().getUserByName(playerName);
            if (userInfoOpt.isPresent()) {
                userId = userInfoOpt.get().getId();
            } else {
                userId = findUserIdInDatabase(playerName);
            }
            
            if (userId <= 0) {
                if (debug) {
                    plugin.getLogger().info("Could not find user ID for player " + playerName);
                }
                return "0";
            }
            
            return getPlayerPositionById(userId);
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting position for player " + playerName + ": " + e.getMessage());
            return "0";
        }
    }

    private String getPlayerPositionById(int userId) {
        if (userId <= 0) return "0";
        
        try (Connection conn = getConnection()) {
            try (PreparedStatement initStmt = conn.prepareStatement("SET @rank := 0")) {
                initStmt.executeUpdate();
            }
            
            String sql = 
                "SELECT rank FROM (" +
                "  SELECT user_id, @rank := @rank + 1 AS rank " +
                "  FROM (" +
                "    SELECT user_id, COUNT(*) AS vote_count " +
                "    FROM vote_votes " +
                "    WHERE created_at >= DATE_FORMAT(NOW() ,'%Y-%m-01') " +
                "    GROUP BY user_id " +
                "    ORDER BY vote_count DESC " +
                "  ) AS ranked_votes " +
                ") AS ranked " +
                "WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int position = rs.getInt("rank");
                    if (debug) {
                        plugin.getLogger().info("Found position " + position + " for user ID " + userId);
                    }
                    return String.valueOf(position);
                } else if (debug) {
                    plugin.getLogger().info("No position found for user ID " + userId + " in primary query");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error in position query: " + e.getMessage());
            }
            
            try {
                String checkSql = 
                    "SELECT COUNT(*) AS vote_count " +
                    "FROM vote_votes " +
                    "WHERE user_id = ? " +
                    "AND created_at >= DATE_FORMAT(NOW() ,'%Y-%m-01')";
                
                int voteCount = 0;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, userId);
                    ResultSet checkRs = checkStmt.executeQuery();
                    
                    if (checkRs.next()) {
                        voteCount = checkRs.getInt("vote_count");
                        if (debug) {
                            plugin.getLogger().info("User ID " + userId + " has " + voteCount + " votes this month");
                        }
                    }
                }
                
                if (voteCount > 0) {
                    String altSql = 
                        "SELECT COUNT(*) + 1 AS rank FROM (" +
                        "  SELECT user_id, COUNT(*) AS votes " +
                        "  FROM vote_votes " +
                        "  WHERE created_at >= DATE_FORMAT(NOW() ,'%Y-%m-01') " +
                        "  GROUP BY user_id " +
                        "  HAVING votes > " + voteCount +
                        ") better_voters";
                    
                    try (PreparedStatement altStmt = conn.prepareStatement(altSql)) {
                        ResultSet altRs = altStmt.executeQuery();
                        
                        if (altRs.next()) {
                            int position = altRs.getInt("rank");
                            if (debug) {
                                plugin.getLogger().info("Alternative method: found position " + position + " for user ID " + userId);
                            }
                            return String.valueOf(position);
                        }
                    }
                } else {
                    if (debug) {
                        plugin.getLogger().info("User ID " + userId + " has no votes this month");
                    }
                    return "0";
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error in alternative position calculation: " + e.getMessage());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting position for user ID " + userId + ": " + e.getMessage());
        }
        
        if (debug) {
            plugin.getLogger().warning("All methods failed to get position for user ID " + userId);
        }
        return "0";
    }

    private String getVotePositionByRank(int rank) {
        if (rank <= 0) return "0";
        
        try (Connection conn = getConnection()) {
            try (PreparedStatement initStmt = conn.prepareStatement("SET @rank := 0")) {
                initStmt.executeUpdate();
            }
            
            String sql = 
                "SELECT position FROM (" +
                "  SELECT @rank := @rank + 1 AS position, user_id " +
                "  FROM (" +
                "    SELECT user_id, COUNT(*) AS vote_count " +
                "    FROM vote_votes " +
                "    WHERE created_at >= DATE_FORMAT(NOW() ,'%Y-%m-01') " +
                "    GROUP BY user_id " +
                "    ORDER BY vote_count DESC" +
                "  ) ranked" +
                ") ranked_with_position " +
                "WHERE position = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, rank);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int position = rs.getInt("position");
                    if (debug) {
                        plugin.getLogger().info("Found position " + position + " for rank " + rank);
                    }
                    return String.valueOf(position);
                } else if (debug) {
                    plugin.getLogger().info("No position found for rank " + rank);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting position for rank " + rank + ": " + e.getMessage());
        }
        
        return String.valueOf(rank);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSection(Map<String, Object> data, String key) {
        if (data == null) return null;
        Object section = data.get(key);
        if (section instanceof Map) {
            return (Map<String, Object>) section;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getListOfMaps(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object obj = map.get(key);
        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                return (List<Map<String, Object>>) list;
            }
        }
        return null;
    }

    private String handleVoteSitePlaceholder(Map<String, Object> votes, String id) {
        if (votes == null) return "";
        
        String[] parts = id.split("_");
        if (parts.length < 4) {
            if (debug) plugin.getLogger().info("Invalid site placeholder format: " + id);
            return "";
        }
        
        String siteIdStr = parts[2];
        String property = parts[3];
        
        if (debug) {
            plugin.getLogger().info("Processing site placeholder: siteId=" + siteIdStr + ", property=" + property);
        }
        
        List<Map<String, Object>> sites = getListOfMaps(votes, "sites");
        if (sites == null) {
            if (debug) plugin.getLogger().info("No sites found in vote data");
            return "";
        }
        
        for (Map<String, Object> site : sites) {
            String currentId = String.valueOf(site.get("id"));
            if (siteIdStr.equalsIgnoreCase(currentId)) {
                if ("name".equalsIgnoreCase(property)) {
                    return String.valueOf(site.get("name"));
                } else if ("available".equalsIgnoreCase(property)) {
                    return Boolean.TRUE.equals(site.get("available")) ? "True" : "False";
                } else if ("cooldown".equalsIgnoreCase(property)) {
                    boolean available = Boolean.TRUE.equals(site.get("available"));
                    if (available) {
                        return "True";
                    } else {
                        Object cooldownObj = site.get("cooldown");
                        return cooldownObj != null ? String.valueOf(cooldownObj) : "True";
                    }
                } else if ("url".equalsIgnoreCase(property)) {
                    Object url = site.get("url");
                    return url != null ? String.valueOf(url) : "";
                }
            }
        }
        
        if (debug) plugin.getLogger().info("Site ID " + siteIdStr + " not found");
        return "";
    }
}


