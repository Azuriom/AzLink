package com.azuriom.azlink.bukkit;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

public class ServerPingEvent extends ServerListPingEvent {

    private final Server server;

    private final Collection<UUID> removedPlayers = new ArrayList<>();
    private Collection<Player> players;

    public ServerPingEvent(InetAddress address, Server server) {
        super(address, server.getMotd(), server.getMaxPlayers());
        this.server = server;
    }

    @Override
    public void setServerIcon(CachedServerIcon icon) {
        // ignore, we don't need to handle favicon
    }

    @Override
    public int getNumPlayers() {
        if (this.players == null) {
            return this.server.getOnlinePlayers().size();
        }
        return players.size();
    }

    @Override
    public Iterator<Player> iterator() {
        if (this.players == null) {
            this.players = new ArrayList<>(this.server.getOnlinePlayers());
        }

        Iterator<Player> iterator = this.players.iterator();

        return new Iterator<Player>() {
            private Player current;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Player next() {
                this.current = iterator.next();
                return this.current;
            }

            @Override
            public void remove() {
                iterator.remove();
                ServerPingEvent.this.removedPlayers.add(this.current.getUniqueId());
            }
        };
    }

    public Collection<UUID> getRemovedPlayers() {
        return this.removedPlayers;
    }
}
