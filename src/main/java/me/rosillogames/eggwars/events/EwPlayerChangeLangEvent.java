package me.rosillogames.eggwars.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import me.rosillogames.eggwars.player.EwPlayer;

public class EwPlayerChangeLangEvent extends Event
{
    private static final HandlerList HANDLERS = new HandlerList();
    private EwPlayer player;

    public EwPlayerChangeLangEvent(EwPlayer player)
    {
        this.player = player;
    }

    public EwPlayer getPlayer()
    {
        return this.player;
    }

    public HandlerList getHandlers()
    {
        return HANDLERS;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }
}
