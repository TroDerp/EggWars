package me.rosillogames.eggwars.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.ArenaSign;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.LobbySigns;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class SignClickListener implements Listener
{
    @EventHandler
    public void click(PlayerInteractEvent event)
    {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getHand() != EquipmentSlot.HAND)
        {
            return;
        }

        Location clickLoc = event.getClickedBlock().getLocation();
        ArenaSign ewsign = LobbySigns.getSignByLocation(clickLoc, false);

        if (ewsign == null)
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

            if (ewplayer.isEliminated())
            {
                return;
            }

            Arena arena = EggWars.getArenaManager().getArenaByWorld(clickLoc.getWorld());

            if (arena == null || !arena.getStatus().equals(ArenaStatus.IN_GAME))
            {
                return;
            }

            arena.getGenerators().forEach((location, gen) ->
            {
                if (location.equals(clickLoc) || (EggWars.config.useBelowBlock && location.equals(clickLoc.clone().add(0.0D, 1.0D, 0.0D))))
                {
                    if (gen.hasCachedType())
                    {
                        if (event.getPlayer().isSneaking())
                        {
                            gen.tryUpgrade(event.getPlayer());
                        }
                        else
                        {
                            gen.openInventory(event.getPlayer());
                        }
                    }

                    event.setCancelled(true);
                    return;
                }
            });

            return;
        }
        else
        {
            if (event.getPlayer().isSneaking())
            {
                return;
            }

            EwPlayer ewplayer1 = PlayerUtils.getEwPlayer(event.getPlayer());

            if (ewplayer1.isInArena())
            {
                return;
            }

            Arena arena1 = ewsign.getArena();

            if (!arena1.isSetup())
            {
                TranslationUtils.sendMessage("commands.error.arena_not_set_up", event.getPlayer(), arena1.getName());
                return;
            }

            if (arena1.getStatus().equals(ArenaStatus.SETTING))
            {
                TranslationUtils.sendMessage("commands.error.arena_in_edit_mode", event.getPlayer());
                return;
            }

            if (arena1.getStatus().isLobby())
            {
                if (arena1.isFull())
                {
                    TranslationUtils.sendMessage("gameplay.lobby.cant_join.full", ewplayer1.getPlayer());
                }
                else
                {
                    event.setCancelled(true);
                    arena1.joinArena(PlayerUtils.getEwPlayer(event.getPlayer()), false, false);
                }
            }
            else if (arena1.getStatus().equals(ArenaStatus.FINISHING) || !EggWars.config.canSpectJoin)
            {
                TranslationUtils.sendMessage("gameplay.lobby.cant_join.ingame", ewplayer1.getPlayer());
            }
            else
            {
                event.setCancelled(true);
                arena1.joinArena(PlayerUtils.getEwPlayer(event.getPlayer()), true, true);
            }

            return;
        }
    }
}
