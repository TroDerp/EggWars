package me.rosillogames.eggwars.listeners;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.ArenaSign;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.LobbySigns;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class BlockBreakListener implements Listener
{
    @EventHandler
    public void sign(BlockBreakEvent eventIn)
    {
        if (eventIn.isCancelled())
        {
            return;
        }

        Location location = eventIn.getBlock().getLocation();
        ArenaSign ewsign;

        if ((ewsign = LobbySigns.getSignByLocation(location, true)) != null)
        {
            if (!eventIn.getPlayer().hasPermission("eggwars.sign.break"))
            {
                eventIn.setCancelled(true);
                return;
            }
            else
            {
                EggWars.signs.remove(ewsign);
                TranslationUtils.sendMessage("setup.sign.arena.removed", eventIn.getPlayer());
                EggWars.saveSigns();
                return;
            }
        }

        if (!(eventIn.getBlock().getState() instanceof Sign))
        {
            return;
        }

        Arena arena = EggWars.getArenaManager().getArenaByWorld(location.getWorld());

        if (arena == null)
        {
            return;
        }

        //remember that 'return' doesn't work outside forEach
        for (Map.Entry<Vector, Generator> entry : arena.getGenerators().entrySet())
        {
            if (entry.getKey().equals(location.toVector()))
            {
                //Block break has to be cancelled to skip further issues
                if (arena.getStatus() != ArenaStatus.SETTING)
                {
                    eventIn.setCancelled(true);
                    TranslationUtils.sendMessage("commands.error.arena_needs_edit_mode", eventIn.getPlayer());
                    return;
                }
                else if (!eventIn.getPlayer().hasPermission("eggwars.gen.break"))
                {
                    eventIn.setCancelled(true);
                    return;
                }

                arena.removeGenerator(entry.getKey());
                TranslationUtils.sendMessage("setup.generator.removed", eventIn.getPlayer());
                return;
            }
        }

        return;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void ingame(BlockBreakEvent eventIn)
    {
        if (eventIn.isCancelled())
        {
            return;
        }

        EwPlayer player = PlayerUtils.getEwPlayer(eventIn.getPlayer());

        if (player.isInArena())
        {
            Arena arena = player.getArena();

            if (!arena.getStatus().equals(ArenaStatus.IN_GAME))
            {
                eventIn.setCancelled(true);
                return;
            }

            if (!arena.getReplacedBlocks().containsKey(eventIn.getBlock().getLocation()) && !EggWars.config.breakableBlocks.contains(eventIn.getBlock().getType()))
            {
                eventIn.setCancelled(true);
                TranslationUtils.sendMessage("gameplay.ingame.cant_break_not_placed", player.getPlayer());
            }
            else
            {
                for (EwPlayer player1 : player.getTeam().getPlayers())
                {
                    if (!player1.equals(player) && player1.getPlayer().getLocation().getBlock().getLocation().equals(eventIn.getBlock().getLocation().clone().add(0.0D, 1.0D, 0.0D)))
                    {
                        TranslationUtils.sendMessage("gameplay.ingame.cant_break_below_team", player.getPlayer());
                        eventIn.setCancelled(true);
                        return;
                    }
                }

                arena.addReplacedBlock(eventIn.getBlock().getState());

                if (!EggWars.instance.getConfig().getBoolean("game.drop_blocks"))
                {
                    eventIn.setDropItems(false);
                }

                eventIn.setExpToDrop(0);
                player.getIngameStats().addStat(StatType.BLOCKS_BROKEN, 1);
            }
        }
        else
        {
            Arena arena = EggWars.getArenaManager().getArenaByWorld(eventIn.getBlock().getWorld());

            if (arena != null && arena.getStatus() != ArenaStatus.SETTING)
            {
                eventIn.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void soilChange(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.PHYSICAL)
        {
            return;
        }

        if (event.getClickedBlock().getType() != Material.FARMLAND)
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (ewplayer.isInArena())
        {
            Arena arena = ewplayer.getArena();

            if (!arena.getStatus().equals(ArenaStatus.SETTING))
            {
                event.setCancelled(true);
            }

            return;
        }
    }

    @EventHandler
    public void extend(BlockPistonExtendEvent event)
    {
        Arena arena = EggWars.getArenaManager().getArenaByWorld(event.getBlock().getWorld());

        if (arena == null || arena.getStatus() == ArenaStatus.SETTING)
        {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void retract(BlockPistonRetractEvent event)
    {
        Arena arena = EggWars.getArenaManager().getArenaByWorld(event.getBlock().getWorld());

        if (arena == null || arena.getStatus() == ArenaStatus.SETTING)
        {
            return;
        }

        event.setCancelled(true);
    }
}
