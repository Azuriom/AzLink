package com.azuriom.azlink.bukkit.integrations;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.api.v3.AuthMePlayer;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.events.EmailChangedEvent;
import fr.xephi.authme.events.PasswordEncryptionEvent;
import fr.xephi.authme.events.RegisterEvent;
import fr.xephi.authme.security.crypts.EncryptionMethod;
import fr.xephi.authme.security.crypts.HashedPassword;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class AuthMeIntegration implements Listener {

    private final Map<String, String> passwords = new HashMap<>();

    private final AzLinkBukkitPlugin plugin;
    private DataSource dataSource;

    public AuthMeIntegration(AzLinkBukkitPlugin plugin) {
        this.plugin = plugin;

        this.plugin.getLoggerAdapter().info("AuthMe integration enabled.");

        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            try {
                Field field = AuthMeApi.class.getDeclaredField("dataSource");
                field.setAccessible(true);

                this.dataSource = (DataSource) field.get(AuthMeApi.getInstance());
            } catch (ReflectiveOperationException e) {
                throw new UnsupportedOperationException("Unable to register AuthMe integration", e);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPasswordEncryption(PasswordEncryptionEvent event) {
        event.setMethod(new ForwardingEncryptionMethod(event.getMethod()));
    }

    @EventHandler
    public void onEmailChanged(EmailChangedEvent event) {
        Player player = event.getPlayer();

        this.plugin.getPlugin()
                .getHttpClient()
                .updateEmail(player.getUniqueId(), event.getNewEmail())
                .exceptionally(ex -> {
                    this.plugin.getLoggerAdapter().error("Unable to update email for " + player.getName(), ex);

                    return null;
                });
    }

    @EventHandler
    public void onRegister(RegisterEvent event) {
        Player player = event.getPlayer();
        HashedPassword hashedPassword = this.dataSource.getPassword(event.getPlayer().getName());
        String password = this.passwords.remove(hashedPassword.getHash());
        String email = AuthMeApi.getInstance().getPlayerInfo(player.getName())
                .flatMap(AuthMePlayer::getEmail)
                .orElse(null);
        InetSocketAddress address = player.getAddress();
        InetAddress ip = address != null ? address.getAddress() : null;

        if (password == null) {
            this.plugin.getLoggerAdapter().warn("Unable to get password of " + player.getName());
            return;
        }

        this.plugin.getPlugin()
                .getHttpClient()
                .registerUser(player.getName(), email, player.getUniqueId(), password, ip)
                .exceptionally(ex -> {
                    this.plugin.getLoggerAdapter().error("Unable to register " + player.getName(), ex);

                    return null;
                });
    }

    public class ForwardingEncryptionMethod implements EncryptionMethod {

        private final EncryptionMethod method;

        public ForwardingEncryptionMethod(EncryptionMethod method) {
            this.method = method;
        }

        @Override
        public HashedPassword computeHash(String password, String name) {
            HashedPassword hash = this.method.computeHash(password, name);
            AuthMeIntegration.this.passwords.put(hash.getHash(), password);
            return hash;
        }

        @Override
        public String computeHash(String password, String salt, String name) {
            String hash = this.method.computeHash(password, salt, name);
            AuthMeIntegration.this.passwords.put(hash, password);
            return hash;
        }

        @Override
        public boolean comparePassword(String password, HashedPassword hashedPassword, String name) {
            return this.method.comparePassword(password, hashedPassword, name);
        }

        @Override
        public String generateSalt() {
            return this.method.generateSalt();
        }

        @Override
        public boolean hasSeparateSalt() {
            return this.method.hasSeparateSalt();
        }

        @Override
        public boolean equals(Object o) {
            return this.method.equals(o);
        }

        @Override
        public int hashCode() {
            return this.method.hashCode();
        }
    }
}
