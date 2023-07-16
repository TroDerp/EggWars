package me.rosillogames.eggwars.arena.game;

import java.util.ArrayList;
import java.util.Collections;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.loaders.KitLoader;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class Starting
{
    public static void doReleasingPhase(final Arena arenaIn)
    {
        arenaIn.setStatus(ArenaStatus.STARTING_GAME);
        setTeams(arenaIn);
        arenaIn.setupVotedResults();
        arenaIn.loadShop();

        for (EwPlayer ewplayer : arenaIn.getPlayers())
        {
            ewplayer.getPlayer().setLevel(0);
            ewplayer.getPlayer().setExp(0.0F);
            ewplayer.getPlayer().getInventory().clear();
            ewplayer.getPlayer().getInventory().setItem(EggWars.instance.getConfig().getInt("inventory.kit_selection.slot_in_cage"), KitLoader.getInvItem(ewplayer.getPlayer()));
        }

        arenaIn.sendBroadcast("gameplay.lobby.teleporting");

        for (Team team : arenaIn.getTeams().values())
        {
            team.placeCages();
            team.tpPlayersToCages();
            team.prepareForGame();
        }

        for (Generator gen : arenaIn.getGenerators().values())
        {
            gen.prepareForGame();
        }

        arenaIn.getScores().updateScores(true);
        Lobby.playCountDown(arenaIn, "release", arenaIn.getReleaseCountdown());
        (new BukkitRunnable()
        {
            private int countDown = arenaIn.getReleaseCountdown();

            @Override
            public void run()
            {
                this.countDown--;

                if (!arenaIn.getStatus().equals(ArenaStatus.STARTING_GAME))
                {
                    this.cancel();
                    return;
                }

                if (!arenaIn.beenForced() && (arenaIn.getWinner() != null || arenaIn.getAlivePlayers() == null))
                {
                    this.cancel();
                    Finish.finish(arenaIn, arenaIn.getWinner());
                    return;
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
                    case 15:
                        Lobby.playCountDown(arenaIn, "release", this.countDown);
                        break;
                    case 0:
                        Lobby.playCountDownSound(arenaIn);
                        releasePlayersAndStartGame(arenaIn);
                        this.cancel();
                        return;
                    default:
                        break;
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

    public static void releasePlayersAndStartGame(Arena arena)
    {
        arena.sendBroadcast("gameplay.lobby.go");
        arena.setStatus(ArenaStatus.IN_GAME);

        if (arena.getReleaseCountdown() != 0 || arena.skipSoloLobby())//Solo doesn't use gameCountdown
        {
            arena.getTeams().values().forEach(team -> team.removeCages());
        }

        arena.getGenerators().values().forEach(gen -> gen.start());

        for (EwPlayer ewplayer : arena.getAlivePlayers())
        {
            ReflectionUtils.sendTitle(ewplayer.getPlayer(), Integer.valueOf(5), Integer.valueOf(10), Integer.valueOf(5), TranslationUtils.getMessage("gameplay.lobby.go", ewplayer.getPlayer()), null);
            arena.setPlayerMaxHealth(ewplayer);
            ewplayer.setInvincible();
            ewplayer.getPlayer().playSound(ewplayer.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 2.0F);
            ewplayer.getPlayer().closeInventory();
            EggWars.getDB().getPlayerData(ewplayer.getPlayer()).addStat(StatType.GAMES_PLAYED, arena.getMode(), 1);

            if (ewplayer.getTeam() == null)
            {
                ewplayer.getPlayer().kickPlayer("Error while loading your configuration!");
            }
            else
            {
                Player player = ewplayer.getPlayer();
                player.setLevel(0);
                player.setExp(0.0F);
                player.getInventory().clear();

                if (ewplayer.getKit() != null)
                {
                    ewplayer.getKit().equip(player);
                }
            }
        }
    }

    private static void setTeams(Arena arenaIn)
    {
        ArrayList<EwPlayer> list = new ArrayList(arenaIn.getPlayers());
        Collections.shuffle(list);

        for (EwPlayer ewplayer : list)
        {
            if (ewplayer.getTeam() == null)
            {
                ArrayList list1 = new ArrayList(arenaIn.getTeams().values());
                Collections.sort(list1, (obj0, obj1) -> ((Team)obj0).getPlayers().size() - ((Team)obj1).getPlayers().size());
                Team team = (Team)list1.get(0);
                team.addPlayer(ewplayer);
                TranslationUtils.sendMessage("gameplay.teams.joined", ewplayer.getPlayer(), TeamUtils.translateTeamType(team.getType(), ewplayer.getPlayer(), false));
            }
        }
    }
}
