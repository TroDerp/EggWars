package me.rosillogames.eggwars.listeners;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
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

        ItemStack stack = pickUpEvent.getItem().getItemStack();
        ItemMeta meta = stack.getItemMeta();

        if (meta.getPersistentDataContainer().has(EggWars.apssId, PersistentDataType.STRING))
        {
            for (Generator gen : ewplayer.getArena().getGenerators().values())
            {
            	Generator.APSS apss = gen.getAPSS();

            	if (apss.uuid.equals(UUID.fromString(meta.getPersistentDataContainer().get(EggWars.apssId, PersistentDataType.STRING))))
            	{
            		if (apss.candidates.size() > 1 && apss.candidates.contains(ewplayer))
            		{
            			if (apss.turn != apss.candidates.indexOf(ewplayer))
            			{
            				pickUpEvent.setCancelled(true);
            				return;
            			}
            		}

            		stack = new ItemStack(stack.getType(), stack.getAmount());
            		break;
            	}
            }
        }

        pickUpEvent.getItem().setItemStack(ItemUtils.tryColorizeByTeam(ewplayer.getTeam().getType(), stack));
        return;
    }
}
