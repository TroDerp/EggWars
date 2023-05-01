package me.rosillogames.eggwars.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class ItemPickupListener implements Listener
{
    @EventHandler
    public void colorize(EntityPickupItemEvent pickUpEvent)
    {
        if (!(pickUpEvent.getEntity() instanceof Player))
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)pickUpEvent.getEntity());

        if (!ewplayer.isInArena() || ewplayer.getTeam() == null)
        {
            return;
        }

        pickUpEvent.getItem().setItemStack(ItemUtils.tryColorizeByTeam(ewplayer.getTeam().getType(), pickUpEvent.getItem().getItemStack()));
        return;
    }
}
