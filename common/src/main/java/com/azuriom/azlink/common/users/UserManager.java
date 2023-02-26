package com.azuriom.azlink.common.users;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.data.UserInfo;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private final Map<String, UserInfo> usersByName = new ConcurrentHashMap<>();
    private final AzLinkPlugin plugin;

    public UserManager(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    public Optional<UserInfo> getUserByName(String name) {
        return Optional.ofNullable(this.usersByName.get(name));
    }

    public void addUser(UserInfo user) {
        this.usersByName.put(user.getName(), user);
    }

    public CompletableFuture<UserInfo> editMoney(UserInfo user, String action, double amount) {
        return this.plugin.getHttpClient().editMoney(user, action, amount)
                .thenApply(result -> {
                    user.setMoney(result.getNewBalance());
                    return user;
                });
    }
}
