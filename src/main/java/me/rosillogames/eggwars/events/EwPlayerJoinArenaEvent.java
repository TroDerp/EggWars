package me.rosillogames.eggwars.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.player.EwPlayer;

public class EwPlayerJoinArenaEvent extends Event implements Cancellable
{
    private static final HandlerList HANDLERS = new HandlerList();
    private EwPlayer player;
    private Arena arena;
    private boolean cancelled;

    public EwPlayerJoinArenaEvent(EwPlayer ewplayer, Arena arena1)
    {
        this.player = ewplayer;
        this.arena = arena1;
        this.cancelled = false;
    }

    public HandlerList getHandlers()
    {
        return HANDLERS;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    public EwPlayer getPlayer()
    {
        return this.player;
    }

    public void setPlayer(EwPlayer ewplayer)
    {
        this.player = ewplayer;
    }

    public Arena getArena()
    {
        return this.arena;
    }

    public void setArena(Arena arena1)
    {
        this.arena = arena1;
    }

    public boolean isCancelled()
    {
        return this.cancelled;
    }

    public void setCancelled(boolean flag)
    {
        this.cancelled = flag;
    }
}
