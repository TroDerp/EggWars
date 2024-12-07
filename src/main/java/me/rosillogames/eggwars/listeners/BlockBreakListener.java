package me.rosillogames.eggwars.listeners;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
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
            if (!eventIn.getPlayer().hasPermission("eggwars.arenaSign.break"))
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

        if (arena.getGenerators().containsKey(location.toVector()))
        {
            //Block break has to be cancelled to skip further issues
            if (arena.getStatus() != ArenaStatus.SETTING)
            {
                eventIn.setCancelled(true);
                TranslationUtils.sendMessage("commands.error.arena_needs_edit_mode", eventIn.getPlayer());
                return;
            }
            else if (!eventIn.getPlayer().hasPermission("eggwars.genSign.break"))
            {
                eventIn.setCancelled(true);
                return;
            }

            arena.removeGenerator(location.toVector());
            TranslationUtils.sendMessage("setup.generator.removed", eventIn.getPlayer());
            return;
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

            if (!arena.canBreakOrReplace(eventIn.getBlock().getState()))
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
    public void fillBucket(PlayerBucketFillEvent eventIn)
    {//merge with ingame method like in place listener?
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

            if (!arena.canBreakOrReplace(eventIn.getBlock().getState()))
            {
                eventIn.setCancelled(true);
                TranslationUtils.sendMessage("gameplay.ingame.cant_break_not_placed", player.getPlayer());
            }
            else
            {
                arena.addReplacedBlock(eventIn.getBlock().getState());
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
    public void flowOrTeleport(BlockFromToEvent event)
    {
        Arena arena = EggWars.getArenaManager().getArenaByWorld(event.getBlock().getWorld());

        if (arena != null)
        {
            if (event.getBlock().getType() == Material.DRAGON_EGG)
            {
                event.setCancelled(true);
            }
            else if (!event.isCancelled() && arena.getStatus().isGame() && (event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.LAVA))
            {
                if (!arena.canBreakOrReplace(event.getToBlock().getState()))
                {
                    event.setCancelled(true);
                    return;
                }

                arena.addReplacedBlock(event.getToBlock().getState());
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
        handlePistonBlocks(event, event.getBlocks(), false);
    }

    @EventHandler
    public void retract(BlockPistonRetractEvent event)
    {
        handlePistonBlocks(event, event.getBlocks(), true);
    }

    private static void handlePistonBlocks(BlockPistonEvent event, List<Block> blocks, boolean retract)
    {//Bugs: there are a lot of spigot bugs with pistons, see SPIGOT-822, SPIGOT-2677 or SPIGOT-2672
        Arena arena = EggWars.getArenaManager().getArenaByWorld(event.getBlock().getWorld());

        if (arena == null || arena.getStatus().equals(ArenaStatus.SETTING))
        {
            return;
        }

        if (!arena.canBreakOrReplace(event.getBlock().getState()))
        {
            event.setCancelled(true);
        }

        for (Block block : blocks)
        {
            if (!arena.canBreakOrReplace(block.getState()))
            {
                event.setCancelled(true);
            }
        }

        if (event.isCancelled())
        {
            return;
        }

        if (!retract)
        {//head is in list when retracting, this is the piston block from that event, otherwise is the head
            arena.addReplacedBlock(event.getBlock().getRelative(event.getDirection()).getState());
        }

        for (Block block : blocks)
        {
            arena.addReplacedBlock(block.getState());//where the block was at

            if (retract || arena.canBreakOrReplace(block.getRelative(event.getDirection()).getState()))
            {
                arena.addReplacedBlock(block.getRelative(event.getDirection()).getState());//where it will be at
            }
        }
    }
}
