package me.rosillogames.eggwars.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class PlayerDropListener implements Listener
{
    @EventHandler
    public void drop(PlayerDropItemEvent playerdropitemevent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(playerdropitemevent.getPlayer());

        if (ewplayer.isInArena() && !ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME))
        {
            playerdropitemevent.setCancelled(true);
            return;
        }
        else
        {
            return;
        }
    }
}
