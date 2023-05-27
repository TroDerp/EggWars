package me.rosillogames.eggwars.listeners;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Generator;
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

        Item item = pickUpEvent.getItem();

        if (EggWars.config.enableAPSS && item.getThrower() != null)
        {
            for (Generator gen : ewplayer.getArena().getGenerators().values())
            {
            	Generator.APSS apss = gen.getAPSS();

            	if (apss.uuid.equals(item.getThrower()))
            	{
            		if (apss.candidates.size() > 1 && apss.candidates.contains(ewplayer) && apss.turn != apss.candidates.indexOf(ewplayer))
            		{
        				pickUpEvent.setCancelled(true);
        				return;
            		}

            		break;
            	}
            }
        }

        pickUpEvent.getItem().setItemStack(ItemUtils.tryColorizeByTeam(ewplayer.getTeam().getType(), item.getItemStack()));
        return;
    }
}
