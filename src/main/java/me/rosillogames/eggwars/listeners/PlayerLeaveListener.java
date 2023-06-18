package me.rosillogames.eggwars.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class PlayerLeaveListener implements Listener
{
    public PlayerLeaveListener()
    {
    }

    @EventHandler //KickEvent collides with leave event
    public void leave(PlayerQuitEvent quitEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(quitEvent.getPlayer());

        if (ewplayer.isInArena())
        {
            ewplayer.getArena().leaveArena(ewplayer, false, false);
        }

        if (EggWars.bungee.isEnabled())
        {
            quitEvent.setQuitMessage(null);
        }

        EggWars.getDB().savePlayer(quitEvent.getPlayer());
        EggWars.players.remove(ewplayer);
    }
}
