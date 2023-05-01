package me.rosillogames.eggwars.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import me.rosillogames.eggwars.enums.ReloadType;

public class EwPluginReloadEvent extends Event
{
    private static final HandlerList HANDLERS = new HandlerList();
    private ReloadType reloadType;

    public EwPluginReloadEvent(ReloadType enumreloadtype)
    {
        this.reloadType = enumreloadtype;
    }

    public HandlerList getHandlers()
    {
        return HANDLERS;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    public ReloadType getReloadType()
    {
        return this.reloadType;
    }
}
