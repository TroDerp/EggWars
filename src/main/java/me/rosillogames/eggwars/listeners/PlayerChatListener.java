package me.rosillogames.eggwars.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.reflection.HelpObject;

public class PlayerChatListener implements Listener
{
    /*@EventHandler //Cannot be done because Spigot applies the new locale AFTER the event, so the setLangId hack won't work.
    public void onClientChangeLang(PlayerLocaleChangeEvent event)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        //Don't check if we have to ignore client language, because if it is switched off, there
        //would be no way of detecting if players have changed their client language before.
        if (ewplayer.getLangId().equals(LanguageManager.DEFAULT_NAME))
        {
            ewplayer.setLangId(ewplayer.getLangId());//Hack to create a playerChangeLang event
        }
    }*/

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void chat(AsyncPlayerChatEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());
        event.setCancelled(true);

        //send to players not in arena
        if (!ewplayer.isInArena())
        {
            for (Player player : event.getRecipients())
            {
                if (!PlayerUtils.getEwPlayer(player).isInArena())
                {
                    player.sendMessage(event.getFormat().replace("%1$s", event.getPlayer().getDisplayName()).replace("%2$s", event.getMessage()));
                }
            }

            return;
        }

        //color remover
        if (!event.getPlayer().hasPermission("eggwars.chatcolor"))
        {
            event.setMessage(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getMessage())));
        }

        HelpObject<String> msg = new HelpObject();
        msg.object = event.getMessage();
        Arena arena = ewplayer.getArena();

        if (ewplayer.isEliminated())
        {
            for (EwPlayer ewplayer1 : arena.getPlayers())
            {
                if (ewplayer1.isEliminated() || EggWars.config.publicSpectChat)
                {
                    TranslationUtils.sendMessagePrefix("gameplay.chat.spectator", ewplayer1.getPlayer(), false, new Object[] {ewplayer.getPlayer().getName(), msg.get(), PlayerUtils.getPrefix(ewplayer.getPlayer())});
                }
            }
        }
        else if (arena.getStatus().isGame())
        {
            if (ewplayer.getTeam() == null)
            {
                event.setCancelled(false);
                return;
            }

            Team team = ewplayer.getTeam();

            if (arena.getMaxTeamPlayers() <= 1 || talksGloballier(msg))
            {
                for (EwPlayer ewplayer2 : arena.getPlayers())
                {
                    /* I decided to make bold name appear in both team and solo, to keep them similar */
                    if (/* arena.getMaxTeamPlayers() > 1 && */ewplayer2.getTeam() == team)
                    {
                        TranslationUtils.sendMessagePrefix("gameplay.chat.global", ewplayer2.getPlayer(), false, new Object[] {TeamUtils.teamPrefix(team.getType(), ewplayer2.getPlayer()), team.getType().color() + "§l" + ewplayer.getPlayer().getName(), msg.get(), PlayerUtils.getPrefix(ewplayer.getPlayer())});
                    }
                    else
                    {
                        TranslationUtils.sendMessagePrefix("gameplay.chat.global", ewplayer2.getPlayer(), false, new Object[] {TeamUtils.teamPrefix(team.getType(), ewplayer2.getPlayer()), team.getType().color() + ewplayer.getPlayer().getName(), msg.get(), PlayerUtils.getPrefix(ewplayer.getPlayer())});
                    }
                }
            }
            else
            {
                for (EwPlayer ewplayer4 : team.getPlayers())
                {
                    TranslationUtils.sendMessagePrefix("gameplay.chat.team", ewplayer4.getPlayer(), false, TeamUtils.translateTeamType(team.getType(), ewplayer4.getPlayer(), false), ewplayer.getPlayer().getName(), msg.get(), PlayerUtils.getPrefix(ewplayer.getPlayer()));
                }
            }
        }
        else if (arena.getStatus().isLobby())
        {
            for (EwPlayer ewplayer5 : arena.getPlayers())
            {
                TranslationUtils.sendMessage("gameplay.chat.lobby", ewplayer5.getPlayer(), ewplayer.getPlayer().getName(), msg.get(), PlayerUtils.getPrefix(ewplayer.getPlayer()));
            }
        }
        else if (arena.getStatus().equals(ArenaStatus.FINISHING))
        {
            if (ewplayer.getTeam() == null)
            {
                event.setCancelled(false);
                return;
            }

            for (EwPlayer ewplayer6 : arena.getPlayers())
            {
                TranslationUtils.sendMessage("gameplay.chat.finishing", ewplayer6.getPlayer(), ewplayer.getPlayer().getName(), msg.get(), PlayerUtils.getPrefix(ewplayer.getPlayer()));
            }
        }
        else
        {
            event.setCancelled(false);
        }
    }

    private static boolean talksGloballier(HelpObject<String> msg)
    {
        if (msg.get().startsWith("!"))
        {
            //test if message resets
            msg.object = msg.get().replaceFirst("!", "");
            return true;
        }

        return false;
    }
}
