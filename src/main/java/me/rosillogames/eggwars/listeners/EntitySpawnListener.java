package me.rosillogames.eggwars.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.utils.WorldController;

public class EntitySpawnListener implements Listener
{
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event)
    {
        Arena arena = EggWars.getArenaManager().getArenaByWorld(event.getEntity().getWorld());

        if (arena != null)
        {
            if (event instanceof CreatureSpawnEvent && ((CreatureSpawnEvent)event).getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
            {
                event.setCancelled(true);
            }

            if (event.getEntity() instanceof TNTPrimed)
            {
                FileConfiguration conf = EggWars.instance.getConfig();
                TNTPrimed tnt = (TNTPrimed)event.getEntity();

                if (!conf.getBoolean("game.tnt.move_when_ignited"))
                {
                    tnt.setVelocity(new Vector(0.0D, 0.0D, 0.0D));
                }

                tnt.setYield((float)conf.getDouble("game.tnt.strenght"));
                tnt.setFuseTicks(conf.getInt("game.tnt.fuse_ticks"));
            }

            if (!event.isCancelled() && arena.getStatus().isGame())
            {
                WorldController.addPluginChunkTicket(event.getLocation());
            }
        }
    }
}
