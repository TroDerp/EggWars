package me.rosillogames.eggwars.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Scoreboards;
import me.rosillogames.eggwars.arena.SetupGUI;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.ReloadType;
import me.rosillogames.eggwars.events.PlayerChangeEwLangEvent;
import me.rosillogames.eggwars.events.EwPluginReloadEvent;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class EggWarsListener implements Listener
{
    @EventHandler
    public void onFeatureReload(EwPluginReloadEvent event)
    {
        if (event.getReloadType() == ReloadType.LANGUAGES || event.getReloadType() == ReloadType.ALL)
        {
            for (EwPlayer player : EggWars.players)
            {
                player.getProfile().loadLangGui();

                if (player.getMenuPlayer().getMenu() != null && player.getMenuPlayer().getMenu().getMenuType() != MenuType.LANGUAGES)
                {
                    player.getMenuPlayer().getMenu().sendMenuUpdate(true);
                }
            }

            for (Arena arena : EggWars.getArenaManager().getArenas())
            {
                arena.getScores().updateScores(true);
            }
        }

        if (event.getReloadType() == ReloadType.GENERATORS || event.getReloadType() == ReloadType.ALL)
        {
            SetupGUI.makeGeneratorsTypesGUI();
            SetupGUI.reloadGeneratorLevelsGUIs();
        }
    }

    @EventHandler
    public void onPlayerChangeLang(PlayerChangeEwLangEvent event)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());
        ewplayer.getProfile().loadLangGui();

        if (ewplayer.getMenuPlayer().getMenu() != null)
        {
            ewplayer.getMenuPlayer().getMenu().sendUpdateTo(ewplayer.getMenuPlayer(), true);
        }

        if (ewplayer.getArena() != null)
        {
            Scoreboards scores = ewplayer.getArena().getScores();
            scores.setScore(ewplayer);
            scores.setTeamScores(ewplayer);
        }
    }
}
