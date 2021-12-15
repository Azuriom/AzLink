package com.azuriom.azlink.bukkit;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ServerPingEvent extends ServerListPingEvent {

    private final Server server;

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
        return this.players.iterator();
    }

    public Collection<? extends Player> getPlayers() {
        if (this.players == null) {
            return this.server.getOnlinePlayers();
        }
        return this.players;
    }
}
