package me.rosillogames.eggwars.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.utils.WorldController;

public class EntitySpawnListener implements Listener
{
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event)
    {
        Arena possibleArena;

        if ((possibleArena = EggWars.getArenaManager().getArenaByWorld(event.getEntity().getWorld())) != null)
        {
            if (event instanceof CreatureSpawnEvent && ((CreatureSpawnEvent)event).getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
            {
                event.setCancelled(true);
            }

            if (!event.isCancelled() && possibleArena.getStatus().isGame())
            {
                WorldController.addPluginChunkTicket(event.getLocation());
            }
        }
    }
}
