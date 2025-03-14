package me.rosillogames.eggwars.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerChangeEwLangEvent extends PlayerEvent
{
    private static final HandlerList HANDLERS = new HandlerList();
    private final String previousLangId;

    public PlayerChangeEwLangEvent(Player playerIn, String prevLangIn)
    {
        super(playerIn);
        this.previousLangId = prevLangIn;
    }

    public String getPrevoiusLanguage()
    {
        return this.previousLangId;
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
}
