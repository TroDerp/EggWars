package me.rosillogames.eggwars.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class PlayerCraftListener implements Listener
{
    @EventHandler
    public void craft(CraftItemEvent craftitemevent)
    {
        if (!(craftitemevent.getWhoClicked() instanceof Player))
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)craftitemevent.getWhoClicked());

        if (ewplayer.isInArena())
        {
            craftitemevent.setCancelled(true);
        }
    }
}
