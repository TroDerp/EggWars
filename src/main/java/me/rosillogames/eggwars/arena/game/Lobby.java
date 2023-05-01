package me.rosillogames.eggwars.arena.game;

import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.arena.Scoreboards;
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
        Countdown countdown = new Countdown(arenaIn.getDefCountdown());
        arenaIn.setStatus(ArenaStatus.STARTING);
        Countdown.playCountDownSoundAndSendText(arenaIn, "starting", countdown.getCountdown());
        (new BukkitRunnable()
        {
            private final int fullCountDown = arenaIn.getFullCountdown();

            @Override
            public void run()
            {
        	    countdown.decrease();

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

                if (arenaIn.isFull() && !countdown.isFullCountdown() && this.fullCountDown >= 0)
                {
                    countdown.setFullCountdown(this.fullCountDown);
                    arenaIn.getPlayers().forEach((ewplayer) ->
                    {
                        TranslationUtils.sendMessage("gameplay.lobby.full_countdown", ewplayer.getPlayer(), TranslationUtils.translateTime(ewplayer.getPlayer(), this.fullCountDown, true));
                    });
                }

                int count = countdown.getCountdown();

                for (EwPlayer ewplayer1 : arenaIn.getPlayers())
                {
                    ewplayer1.getPlayer().setLevel(count);
                    ewplayer1.getPlayer().setExp(0.0F);
                }

                arenaIn.setCurrentCountdown(count);

                switch (count)
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
                case 160:
                    Countdown.playCountDownSoundAndSendText(arenaIn, "starting", count);
                    return;
                default:
                    break;
                }

                if (count <= 0)
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
            Scoreboards.setScore(arenaIn);
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
}
