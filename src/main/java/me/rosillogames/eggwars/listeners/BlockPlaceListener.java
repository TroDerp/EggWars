package me.rosillogames.eggwars.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.arena.Scoreboards;
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
    public void placeBlock(BlockPlaceEvent eventIn)
    {
        if (eventIn.isCancelled())
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(eventIn.getPlayer());

        if (ewplayer.isInArena())
        {
            Arena arena = ewplayer.getArena();

            if (!arena.getStatus().equals(ArenaStatus.IN_GAME))
            {
            	eventIn.setCancelled(true);
                return;
            }

            if (eventIn.getBlock().getType().equals(Material.TNT))
            {
            	eventIn.getBlock().setType(Material.AIR);
                TNTPrimed tnt = eventIn.getBlock().getWorld().spawn(Locations.toMiddle(eventIn.getBlock().getLocation()), TNTPrimed.class);

                if (!EggWars.config.moveTNTOnIgnite)
                {
                    tnt.setVelocity(new Vector(0.0D, 0.0D, 0.0D));
                }

                ReflectionUtils.setTNTSource(tnt, eventIn.getPlayer());
                tnt.setYield(3.0F);
                return;
            }

            if (eventIn.getBlock().getType().equals(Material.DRAGON_EGG))
            {
                Team team = TeamUtils.getTeamByEggLocation(ewplayer.getArena(), eventIn.getBlock().getLocation());

                if (team != null && !team.isEliminated())
                {
                    Scoreboards.setScore(ewplayer.getArena());
                    return;
                }
                else
                {
                    eventIn.setCancelled(true);
                    return;
                }
            }

            /* for invalidating places, use block location, and not middle location */
            for (Team team1 : arena.getTeams().values())
            {
                if (invalidPlace(team1.getVillager(), eventIn, false))
                {
                    return;
                }

            	if (invalidPlace(team1.getRespawn(), eventIn, false))
            	{
            		return;
            	}
            }

            for (Generator gen : arena.getGenerators().values())
            {
                if (invalidPlace(gen.getBlock(), eventIn, true))
                {
                    return;
                }
            }

            if (!arena.getBounds().canPlaceAt(eventIn.getBlock().getLocation()))
            {
                eventIn.setCancelled(true);
                TranslationUtils.sendMessage("gameplay.ingame.cant_place_outside", ewplayer.getPlayer());
                return;
            }

            if (arena.getWorld().equals(eventIn.getBlock().getWorld()))
            {
                arena.addBrokenBlock(eventIn.getBlockReplacedState());
            	arena.addPlacedBlock(eventIn.getBlock().getLocation());
                ewplayer.getIngameStats().addStat(StatType.BLOCKS_PLACED, 1);
            }
            else
            {
            	eventIn.setCancelled(true);
            }
        }
        else
        {
            Arena arena = EggWars.getArenaManager().getArenaByWorld(eventIn.getBlock().getWorld());

            if (arena != null && !arena.getStatus().equals(ArenaStatus.SETTING))
            {
                eventIn.setCancelled(true);
                return;
            }
        }
    }

    private static boolean invalidPlace(Location blocker, BlockPlaceEvent place, boolean offset)
    {
        for (BlockFace face : (new BlockFace[] {BlockFace.SELF, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.EAST, BlockFace.WEST}))
        {
            for (int i = (offset ? -1 : 0); i < (offset ? 1 : 2); i++)
            {
                if ((int)place.getBlock().getLocation().getX() == ((int)blocker.getX() + face.getModX()) && (int)place.getBlock().getLocation().getY() == ((int)blocker.getY() + i) && (int)place.getBlock().getLocation().getZ() == ((int)blocker.getZ() + face.getModZ()))
                {
                    place.setCancelled(true);
                    TranslationUtils.sendMessage("gameplay.ingame.cant_place_objects_here", place.getPlayer());
                    return true;
                }
            }
        }

        return false;
    }
}
