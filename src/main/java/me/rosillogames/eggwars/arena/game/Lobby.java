package me.rosillogames.eggwars.arena.game;

import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.loaders.ArenaLoader;
import me.rosillogames.eggwars.loaders.KitLoader;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.VoteUtils;

public class Lobby
{
    public static void doStartingPhase(final Arena arenaIn)
    {
        arenaIn.setStatus(ArenaStatus.STARTING);
        playCountDown(arenaIn, "starting", arenaIn.getDefCountdown());
        (new BukkitRunnable()
        {
            private int countDown = arenaIn.getDefCountdown();
            private boolean full = false;

            @Override
            public void run()
            {
                this.countDown--;

                if (!arenaIn.getStatus().equals(ArenaStatus.STARTING))
                {
                    this.cancel();
                    return;
                }

                if (!arenaIn.hasEnoughPlayers())
                {
                    for (EwPlayer ewplayer : arenaIn.getPlayers())
                    {
                        ewplayer.getPlayer().setLevel(0);
                        ewplayer.getPlayer().setExp(0.0F);
                    }

                    arenaIn.sendBroadcast("gameplay.lobby.not_enough_players");
                    arenaIn.setStatus(ArenaStatus.LOBBY);
                    this.cancel();
                    return;
                }

                if (arenaIn.isFull() && !this.full && arenaIn.getFullCountdown() >= 0)
                {
                    this.full = true;
                    int fullCD = arenaIn.getFullCountdown();

                    if (this.countDown > fullCD)
                    {
                        this.countDown = fullCD;
                    }

                    for (EwPlayer ewplayer : arenaIn.getPlayers())
                    {
                        TranslationUtils.sendMessage("gameplay.lobby.full_countdown", ewplayer.getPlayer(), TranslationUtils.translateTime(ewplayer.getPlayer(), this.countDown, true));
                    }
                }

                for (EwPlayer ewplayer1 : arenaIn.getPlayers())
                {
                    ewplayer1.getPlayer().setLevel(this.countDown);
                    ewplayer1.getPlayer().setExp(0.0F);
                }

                arenaIn.setCurrentCountdown(this.countDown);

                switch (this.countDown)
                {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 10:
                    case 20:
                    case 30:
                    case 60:
                    case 90:
                    case 120:
                    case 150:
                    case 180:
                        playCountDown(arenaIn, "starting", this.countDown);
                        return;
                    default:
                        break;
                }

                if (this.countDown <= 0)
                {
                    endStartingPhase(arenaIn);
                    this.cancel();
                }
            }

            @Override
            public void cancel()
            {
                super.cancel();
                arenaIn.setCurrentCountdown(0);
            }
        }).runTaskTimer(EggWars.instance, 20L, 20L);
    }

    public static void endStartingPhase(Arena arenaIn)
    {
        if (arenaIn.skipSoloLobby())
        {
            //"solo" start has to skip ArenaStatus.STARTING_GAME and should use defCountdown
            arenaIn.setupVotedResults();
            arenaIn.loadShop();

            for (Team team : arenaIn.getTeams().values())
            {
                team.prepareForGame();
            }

            for (Generator gen : arenaIn.getGenerators().values())
            {
                gen.prepareForGame();
            }

            Starting.releasePlayersAndStartGame(arenaIn);
            arenaIn.getScores().updateScores(true);
        }
        else
        {
            Starting.doReleasingPhase(arenaIn);
        }
    }

    public static void onEnter(Arena arenaIn, EwPlayer ewplayer)
    {
        if (arenaIn.skipSoloLobby())
        {
            for (Team team : arenaIn.getTeams().values())
            {
                if (team.getPlayers().size() <= 0)
                {
                    team.addPlayer(ewplayer);
                    team.placeCages();
                    team.tpPlayersToCages();
                    break;
                }
            }

            ewplayer.getPlayer().getInventory().setItem(EggWars.instance.getConfig().getInt("inventory.kit_selection.slot_in_cage"), KitLoader.getInvItem(ewplayer.getPlayer()));
            ewplayer.getPlayer().getInventory().setItem(EggWars.instance.getConfig().getInt("inventory.voting.slot_in_cage"), VoteUtils.getInvItem(ewplayer.getPlayer()));
        }
        else
        {
            ewplayer.getPlayer().getInventory().setItem(EggWars.instance.getConfig().getInt("inventory.kit_selection.slot"), KitLoader.getInvItem(ewplayer.getPlayer()));
            ewplayer.getPlayer().getInventory().setItem(EggWars.instance.getConfig().getInt("inventory.voting.slot"), VoteUtils.getInvItem(ewplayer.getPlayer()));
            ewplayer.getPlayer().getInventory().setItem(EggWars.instance.getConfig().getInt("inventory.team_selection.slot"), TeamUtils.getInvItem(ewplayer.getPlayer()));
        }

        (new BukkitRunnable()
        {
            public void run()
            {
                if (ewplayer.isInArena() && !arenaIn.getStatus().isGame())
                {
                    ewplayer.getPlayer().getInventory().setItem(EggWars.instance.getConfig().getInt("inventory.leave.slot"), ArenaLoader.getLeaveItem(ewplayer.getPlayer()));
                }
            }
        }).runTaskLater(EggWars.instance, 30L);
    }

    public static void playCountDownSound(Arena arenaIn)
    {
        arenaIn.broadcastSound(Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
    }

    public static void playCountDown(Arena arenaIn, String type, int countdown)
    {
        arenaIn.getPlayers().forEach((ewplayer) ->
        {
            TranslationUtils.sendMessage("gameplay.lobby." + type + "_countdown", ewplayer.getPlayer(), TranslationUtils.translateTime(ewplayer.getPlayer(), countdown, true));
        });
        playCountDownSound(arenaIn);
    }
}
