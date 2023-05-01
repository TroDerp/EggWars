package me.rosillogames.eggwars.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class InventoryListener implements Listener
{
    @EventHandler
    public void updatePlayerInvWhenClosing(InventoryCloseEvent inventorycloseevent)
    {
        if (!(inventorycloseevent.getPlayer() instanceof Player) || inventorycloseevent.getPlayer() == null)
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)inventorycloseevent.getPlayer());

        if (ewplayer != null && ewplayer.getInv() != null)
        {
            if (ewplayer.getInv().getInventory() == inventorycloseevent.getInventory())
            {
                ewplayer.setInv(null);
            }
        }

        return;
    }
}
