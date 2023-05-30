package me.rosillogames.eggwars.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.player.EwPlayer;

public class EwPlayerRemoveEggEvent extends Event implements Cancellable
{
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private EwPlayer player;
    private Team eggTeam;

    public EwPlayerRemoveEggEvent(EwPlayer ewplayer, Team team)
    {
        this.cancelled = false;
        this.player = ewplayer;
        this.eggTeam = team;
    }

    public HandlerList getHandlers()
    {
        return HANDLERS;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    public boolean isCancelled()
    {
        return this.cancelled;
    }

    public void setCancelled(boolean flag)
    {
        this.cancelled = flag;
    }

    public EwPlayer getPlayer()
    {
        return this.player;
    }

    public Team getEggTeam()
    {
        return this.eggTeam;
    }
}
