package me.rosillogames.eggwars.listeners;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class BlockPlaceListener implements Listener
{
    @EventHandler
    public void emptyBucket(PlayerBucketEmptyEvent eventIn)
    {
        handlePlace(eventIn);
    }

    @EventHandler
    public void placeBlock(BlockPlaceEvent eventIn)
    {
        handlePlace(eventIn);
    }

    private static void handlePlace(Cancellable eventIn)
    {
        if (eventIn.isCancelled())
        {
            return;
        }

        Player player;
        List<BlockState> allReplaced = new ArrayList();
        Block placing = null;

        if (eventIn instanceof PlayerBucketEmptyEvent)
        {
            PlayerBucketEmptyEvent bucketEvent = (PlayerBucketEmptyEvent)eventIn;
            player = bucketEvent.getPlayer();
            allReplaced.add(bucketEvent.getBlock().getState());
        }
        else if (eventIn instanceof BlockPlaceEvent)
        {
            BlockPlaceEvent placeEvent = (BlockPlaceEvent)eventIn;
            player = placeEvent.getPlayer();

            if (eventIn instanceof BlockMultiPlaceEvent)
            {
                allReplaced.addAll(((BlockMultiPlaceEvent)eventIn).getReplacedBlockStates());
            }
            else
            {
                allReplaced.add(placeEvent.getBlockReplacedState());
            }

            placing = placeEvent.getBlock();
        }
        else
        {
            throw new IllegalStateException("This method should only be called from PlayerBucketEmptyEvent or BlockPlaceEvent listeners.");
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);

        if (ewplayer.isInArena())
        {
            Arena arena = ewplayer.getArena();

            if (!arena.getStatus().equals(ArenaStatus.IN_GAME) || !arena.getWorld().equals(allReplaced.get(0).getLocation().getWorld()))
            {
                eventIn.setCancelled(true);
                return;
            }

            for (int i = 0; i < allReplaced.size() && !eventIn.isCancelled(); ++i)
            {
                BlockState replaced = allReplaced.get(i);

                if (!arena.getBounds().canPlaceAt(replaced.getLocation()))
                {
                    eventIn.setCancelled(true);
                    TranslationUtils.sendMessage("gameplay.ingame.cant_place_outside", player);
                    return;
                }

                if (!arena.canBreakOrReplace(replaced))
                {
                    eventIn.setCancelled(true);
                    TranslationUtils.sendMessage("gameplay.ingame.cant_break_not_placed", player);
                    return;
                }

                if (placing != null && placing.getType().equals(Material.DRAGON_EGG))
                {
                    Team team = TeamUtils.getTeamByEggLocation(arena, replaced.getLocation());

                    if (team == null || team.isEliminated())
                    {
                        eventIn.setCancelled(true);
                        return;
                    }
                }

                /* for invalidating places, use block location, and not middle location */
                for (Team team1 : arena.getTeams().values())
                {
                    if (invalidPlace(team1.getVillager(), replaced.getLocation(), player, false))
                    {
                        eventIn.setCancelled(true);
                        return;
                    }

                    if (invalidPlace(team1.getRespawn(), replaced.getLocation(), player, false))
                    {
                        eventIn.setCancelled(true);
                        return;
                    }
                }

                for (Generator gen : arena.getGenerators().values())
                {
                    if (invalidPlace(gen.getBlock(), replaced.getLocation(), player, true))
                    {
                        eventIn.setCancelled(true);
                        return;
                    }
                }
            }//separate loops, one checks, other applies changes

            for (BlockState state : allReplaced)
            {
                if (placing != null && placing.getType().equals(Material.DRAGON_EGG))
                {
                    arena.getScores().updateScores(false);
                }

                if (placing != null && placing.getType() == Material.TNT && EggWars.instance.getConfig().getBoolean("game.tnt.auto_ignite"))
                {
                    state.getBlock().setType(Material.AIR);
                    TNTPrimed tnt = state.getWorld().spawn(Locations.toMiddle(state.getLocation()), TNTPrimed.class);
                    ReflectionUtils.setTNTSource(tnt, player);
                }

                arena.addReplacedBlock(state);
            }

            ewplayer.getIngameStats().addStat(StatType.BLOCKS_PLACED, 1);
            return;
        }
        else
        {
            Arena arena = EggWars.getArenaManager().getArenaByWorld(allReplaced.get(0).getLocation().getWorld());

            if (arena != null && !arena.getStatus().equals(ArenaStatus.SETTING))
            {
                eventIn.setCancelled(true);
                return;
            }
        }
    }

    private static boolean invalidPlace(Location blocker, Location placing, Player player, boolean offset)
    {
        for (BlockFace face : (new BlockFace[] {BlockFace.SELF, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.EAST, BlockFace.WEST}))
        {
            for (int i = (offset ? -1 : 0); i < (offset ? 1 : 2); i++)
            {
                if ((int)placing.getX() == ((int)blocker.getX() + face.getModX()) && (int)placing.getY() == ((int)blocker.getY() + i) && (int)placing.getZ() == ((int)blocker.getZ() + face.getModZ()))
                {
                    TranslationUtils.sendMessage("gameplay.ingame.cant_place_objects_here", player);
                    return true;
                }
            }
        }

        return false;
    }
}
