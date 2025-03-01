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
import me.rosillogames.eggwars.managers.KitManager;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.ItemUtils;
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
            ItemUtils.setOptionalHUDItem(ewplayer, EggWars.instance.getConfig().getInt("inventory.kit_selection.slot_in_cage"), KitManager.getInvItem(ewplayer.getPlayer()));
        }

        arenaIn.sendBroadcast("gameplay.lobby.teleporting");

        for (Team team : arenaIn.getTeams().values())
        {
            team.placeCages();
            team.prepareForGame();
        }

        for (Generator gen : arenaIn.getGenerators().values())
        {
            gen.prepareForGame();
        }

        arenaIn.getScores().updateScores(true);
        arenaIn.setCurrentCountdown(arenaIn.getReleaseCountdown() + 1);
        (new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!arenaIn.getStatus().equals(ArenaStatus.STARTING_GAME))
                {
                    this.cancel();
                    return;
                }

                if (!arenaIn.beenForced() && (arenaIn.getWinner() != null || arenaIn.getAliveTeams().isEmpty()))
                {
                    this.cancel();
                    Finish.finish(arenaIn, arenaIn.getWinner());
                    return;
                }

                int countDown = arenaIn.getCurrentCountdown() - 1;

                if (countDown != 0 && (countDown % 5 == 0 || countDown <= 5) || countDown == arenaIn.getReleaseCountdown())
                {
                    Lobby.playCountDown(arenaIn, "release", countDown);
                }

                for (EwPlayer ewplayer1 : arenaIn.getPlayers())
                {
                    ewplayer1.getPlayer().setLevel(countDown);
                    ewplayer1.getPlayer().setExp(0.0F);
                }

                if (countDown <= 0)
                {
                    this.cancel();
                    Lobby.playCountDownSound(arenaIn);
                    releasePlayersAndStartGame(arenaIn);
                    return;
                }

                arenaIn.setCurrentCountdown(countDown);
            }

            @Override
            public void cancel()
            {
                super.cancel();
                arenaIn.setCurrentCountdown(0);
            }
        }).runTaskTimer(EggWars.instance, 0L, 20L);
    }

    public static void releasePlayersAndStartGame(Arena arena)
    {
        arena.sendBroadcast("gameplay.lobby.go");
        arena.setStatus(ArenaStatus.IN_GAME);

        if (arena.getReleaseCountdown() != 0 || arena.skipsLobby())//When skipping lobby we don't use releaseCountdown
        {
            arena.getTeams().values().forEach(Team::removeCages);
        }

        arena.getGenerators().values().forEach(Generator::start);

        for (EwPlayer ewplayer : arena.getAlivePlayers())
        {
            ReflectionUtils.sendTitle(ewplayer.getPlayer(), 5, 10, 5, TranslationUtils.getMessage("gameplay.lobby.go", ewplayer.getPlayer()), null);
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
