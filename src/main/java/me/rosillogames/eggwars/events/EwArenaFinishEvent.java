package me.rosillogames.eggwars.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;

public class EwArenaFinishEvent extends Event
{
    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;
    private final Team winner;

    public EwArenaFinishEvent(Team team, Arena arena1)
    {
        this.winner = team;
        this.arena = arena1;
    }

    @Override
    public HandlerList getHandlers()
    {
        return HANDLERS;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    public Arena getArena()
    {
        return this.arena;
    }

    public Team getWinner()
    {
        return this.winner;
    }
}
