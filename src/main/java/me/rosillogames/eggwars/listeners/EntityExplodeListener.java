package me.rosillogames.eggwars.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.ArenaStatus;

public class EntityExplodeListener implements Listener
{
    @EventHandler
    public void explode(EntityExplodeEvent event)
    {
        Arena arena = EggWars.getArenaManager().getArenaByWorld(event.getEntity().getWorld());

        if (arena == null)
        {
            return;
        }

        if (!arena.getStatus().equals(ArenaStatus.IN_GAME))
        {
            event.setCancelled(true);
            return;
        }

        for (Block block : event.blockList())
        {
            if (arena.getReplacedBlocks().containsKey(block.getLocation()) || EggWars.config.breakableBlocks.contains(block.getType()))
            {
                arena.addReplacedBlock(block.getState());
                block.setType(Material.AIR);
            }
        }

        event.blockList().clear();
    }
}
