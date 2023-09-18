package me.rosillogames.eggwars.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class PlayerCraftListener implements Listener
{
    @EventHandler
    public void craft(CraftItemEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player))
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)event.getWhoClicked());

        if (ewplayer.isInArena())
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void burn(FurnaceBurnEvent event)
    {
        Arena arena = EggWars.getArenaManager().getArenaByWorld(event.getBlock().getWorld());

        if (arena != null && arena.getStatus() == ArenaStatus.SETTING)
        {
            return;
        }

        event.setCancelled(true);
    }
}
