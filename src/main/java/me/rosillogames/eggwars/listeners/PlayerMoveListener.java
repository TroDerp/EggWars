package me.rosillogames.eggwars.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

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

        if (ewplayer.isInArena() && !ewplayer.isJoining() && !ewplayer.isEliminated())
        {
            if ((ewplayer.getArena().getStatus().equals(ArenaStatus.LOBBY) || ewplayer.getArena().getStatus().equals(ArenaStatus.STARTING)) && ewplayer.getPlayer().getLocation().getY() < (ewplayer.getArena().getLobby().getY() < 0.0 ? -65.0 : 1.0))
            {
                ewplayer.getPlayer().setFallDistance(0.0f);
                ewplayer.getPlayer().teleport(ewplayer.getArena().getLobby());
            }
            else if (ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME) && ewplayer.getPlayer().getLocation().getY() < -65.0)
            {
                //insta kill
                ewplayer.getPlayer().damage(10000.0f);
            }

            if (!ewplayer.getPlayer().getWorld().equals(ewplayer.getArena().getWorld()))
            {
                ewplayer.getArena().leaveArena(ewplayer, true, false);
            }
        }

        if (ewplayer.isInArena() && (ewplayer.isEliminated() || ewplayer.getArena().getStatus().equals(ArenaStatus.FINISHING)) && ewplayer.getPlayer().getLocation().getY() < (ewplayer.getArena().getCenter().getY() < 0.0 ? -65.0 : 1.0))
        {
            ewplayer.getPlayer().teleport(ewplayer.getArena().getCenter());
        }

        Location f = event.getFrom();
        Location t = event.getTo();

        if (event.getTo() == null || event.getFrom() == null)
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
