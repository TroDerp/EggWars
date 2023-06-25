package me.rosillogames.eggwars.listeners;

import java.util.UUID;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class ItemPickupListener implements Listener
{
    @EventHandler
    public void onPickup(EntityPickupItemEvent pickUpEvent)
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

        if (ewplayer.getArena().getStatus() == ArenaStatus.FINISHING)
        {
            pickUpEvent.setCancelled(true);
            return;
        }

        Item item = pickUpEvent.getItem();
        UUID uuid;

        if (EggWars.config.enableAPSS && (uuid = item.getThrower()) != null)
        {
            for (Generator gen : ewplayer.getArena().getGenerators().values())
            {
                Generator.APSS apss = gen.getAPSS();

                if (apss.uuid.equals(uuid))
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
