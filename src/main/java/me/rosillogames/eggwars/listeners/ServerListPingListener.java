package me.rosillogames.eggwars.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.language.TranslationUtils;

public class ServerListPingListener implements Listener
{
    @EventHandler
    public void ping(ServerListPingEvent serverListEvent)
    {
        if (!EggWars.bungee.isEnabled())
        {
            return;
        }

        if (EggWars.bungee.getArena() == null)
        {
            return;
        }

        String s = TranslationUtils.getMessage("status." + EggWars.bungee.getArena().getStatus().toString());

        if (EggWars.bungee.getArena().getStatus().equals(ArenaStatus.STARTING) && EggWars.bungee.getArena().isFull())
        {
            s = TranslationUtils.getMessage("status.full");
        }

        int i = EggWars.bungee.getArena().getStatus().isLobby() ? EggWars.bungee.getArena().getPlayers().size() : EggWars.bungee.getArena().getAlivePlayers().size();
        serverListEvent.setMotd(s.replace("{PLAYERS}", Integer.valueOf(i).toString()).replace("{ARENA}", !EggWars.bungee.getArenaName().isEmpty() ? ChatColor.translateAlternateColorCodes('&', EggWars.bungee.getArenaName()) : EggWars.bungee.getArena().getName()));
        serverListEvent.setMaxPlayers(EggWars.bungee.getArena().getMaxPlayers());
    }
}
