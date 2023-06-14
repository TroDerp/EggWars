package me.rosillogames.eggwars.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Scoreboards;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.ReloadType;
import me.rosillogames.eggwars.events.EwPlayerChangeLangEvent;
import me.rosillogames.eggwars.events.EwPluginReloadEvent;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.InventoryController;

public class EggWarsListener implements Listener
{
    @EventHandler
    public void onFeatureReload(EwPluginReloadEvent event)
    {
        if (event.getReloadType() == ReloadType.LANGUAGES || event.getReloadType() == ReloadType.ALL)
        {
            for (EwPlayer player : EggWars.players)
            {
                player.getMenu().loadLangGui();

                if (player.getInv() != null)
                {
                    if (player.getInv().getInventoryType() == MenuType.LANGUAGES)
                    {
                        InventoryController.closeInventory(player.getPlayer(), true);//add warning?
                    }
                    else
                    {
                        player.getInv().updateHandler(null, true);
                    }
                }
            }

            for (Arena arena : EggWars.getArenaManager().getArenas())
            {
                arena.getScores().updateScores(true);
            }
        }

        if (event.getReloadType() == ReloadType.KITS || event.getReloadType() == ReloadType.ALL)
        {
            for (EwPlayer player : EggWars.players)
            {
                if (player.getInv() != null && player.getInv().getInventoryType() == MenuType.KIT_SELECTION)
                {
                    InventoryController.closeInventory(player.getPlayer(), true);//add warning?
                }
            }
        }

        if (event.getReloadType() == ReloadType.GENERATORS || event.getReloadType() == ReloadType.ALL)
        {
            for (EwPlayer player : EggWars.players)
            {
                if (player.getInv() != null && (player.getInv().getInventoryType() == MenuType.GENERATOR_INFO || player.getInv().getInventoryType() == MenuType.SELECT_GENERATOR_LEVEL || player.getInv().getInventoryType() == MenuType.SELECT_GENERATOR))
                {
                    InventoryController.closeInventory(player.getPlayer(), false);//add warning?
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChangeLang(EwPlayerChangeLangEvent event)
    {
        EwPlayer ewplayer = event.getPlayer();
        ewplayer.getMenu().loadLangGui();

        if (ewplayer.getInv() != null)
        {
            ewplayer.getInv().updateHandler(null, true);
        }

        if (ewplayer.getArena() != null)
        {
            Scoreboards scores = ewplayer.getArena().getScores();
            scores.setScore(ewplayer);
            scores.setTeamScores(ewplayer);
        }
    }
}
