package me.rosillogames.eggwars.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class PlayerMoveListener implements Listener
{
    @EventHandler
    public void positionArena(PlayerMoveEvent event)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (ewplayer == null)
        {
            return;
        }

        if (ewplayer.isInArena() && !ewplayer.isJoining())
        {
            Arena arena = ewplayer.getArena();

            if (!ewplayer.getPlayer().getWorld().equals(arena.getWorld()))
            {
                ewplayer.setJoining(true);
                arena.leaveArena(ewplayer, true, false);
                return;
            }

            double voidY = arena.getVoidHeight() == null ? -65.0 : arena.getVoidHeight().doubleValue();

            if (ewplayer.getPlayer().getLocation().getY() <= voidY)
            {
                if (arena.getStatus().isLobby())
                {
                    ewplayer.getPlayer().setFallDistance(0.0F);
                    ewplayer.getPlayer().teleport(arena.getLobby());
                }
                else if (ewplayer.isEliminated() || arena.getStatus().equals(ArenaStatus.FINISHING) || arena.getStatus().equals(ArenaStatus.STARTING_GAME))
                {
                    ewplayer.getPlayer().setFallDistance(0.0F);
                    ewplayer.getPlayer().teleport(arena.getCenter());
                }
                else if (arena.getStatus().equals(ArenaStatus.IN_GAME))
                {
                    ReflectionUtils.killOutOfWorld(ewplayer.getPlayer());//insta kill
                }
            }
        }

        Location f = event.getFrom();
        Location t = event.getTo();

        if (f == null || t == null)
        {
            return;
        }

        if (t.getBlockX() != f.getBlockX() || t.getBlockY() != f.getBlockY() || t.getBlockZ() != f.getBlockZ())
        {
            if (ewplayer.isInArena() && ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME) && !ewplayer.isEliminated())
            {
                ewplayer.getIngameStats().addStat(StatType.BLOCKS_WALKED, 1);
            }
        }
    }
}
