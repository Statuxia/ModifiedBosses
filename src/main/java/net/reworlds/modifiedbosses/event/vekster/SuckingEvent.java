package net.reworlds.modifiedbosses.event.vekster;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class SuckingEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<Player> topPlayers;

    public SuckingEvent(List<Player> topPlayers) {
        this.topPlayers = topPlayers;
    }

    public List<Player> getTopPlayers() {
        return topPlayers;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
