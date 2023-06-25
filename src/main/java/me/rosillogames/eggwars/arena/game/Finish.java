package me.rosillogames.eggwars.arena.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.events.EwArenaFinishEvent;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.Fireworks;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;

public class Finish
{
    public static int games = 0;

    public static void finish(final Arena arena, Team winner)
    {
        if (arena.getStatus() == ArenaStatus.FINISHING)
        {
            return;
        }

        arena.setStatus(ArenaStatus.FINISHING);
        arena.getGenerators().values().forEach(generator -> generator.stop());

        if (winner != null)
        {
            for (EwPlayer ewpl : arena.getPlayers())
            {
                Player player = ewpl.getPlayer();
                String winnerObj = arena.getMode().isTeam() || winner.getPlayers().size() < 1 ? TeamUtils.translateTeamType(winner.getType(), player, false) : TeamUtils.colorizePlayerName(winner.getPlayers().iterator().next());
                TranslationUtils.sendMessage("gameplay.ingame.winner", player, winnerObj);

                if (winner.equals(ewpl.getTeam()))
                {
                    TranslationUtils.sendMessage("gameplay.ingame.you_win", player);
                    ewpl.getIngameStats().addStat(StatType.WINS, 1);
                    PlayerUtils.addPoints(ewpl, EggWars.instance.getConfig().getInt("game.points.on_win"));
                }

                if (!ewpl.isEliminated())
                {
                    player.setLevel(0);
                    player.setExp(0.0F);
                    player.closeInventory();
                    player.getInventory().clear();
                    sendFinishStats(ewpl);
                }
            }

            arena.getScores().updateScores(false);
        }
        else
        {
            arena.getScores().clearScoreboards();
        }

        performFinishCounter(arena, winner);
        Bukkit.getPluginManager().callEvent(new EwArenaFinishEvent(winner, arena));
    }

    private static void performFinishCounter(final Arena arena, Team team)
    {
        (new BukkitRunnable()
        {
            public void run()
            {
                //if it is still finishing (arena wasn't reset yet) to prevent kicking when already reloaded
                if (arena.getStatus() == ArenaStatus.FINISHING)
                {
                    arena.getPlayers().forEach(ewplayer -> arena.leaveArena(ewplayer, true, true));
                }
            }
        }).runTaskLater(EggWars.instance, EggWars.config.finishingTime * 20L);
        //the runnable below makes fireworks and checks if the arena (if all players left because runnable up or because they did leave by themselves) is empty to reset it.
        (new BukkitRunnable()
        {
            public void run()
            {
                if (arena.getPlayers().isEmpty())
                {
                    if (EggWars.bungee.isEnabled() && (EggWars.bungee.shouldRestart() || EggWars.bungee.useRandomArena()))
                    {
                        Finish.games++;

                        if (Finish.games >= EggWars.bungee.gamesUntilRestart())
                        {
                            Bukkit.getLogger().info("[EggWars] The server will procceed to stop itself, make sure that you have a method to autorestart the server on stop.");
                            Bukkit.getServer().shutdown();
                            this.cancel();
                            return;
                        }
                    }

                    arena.reset(false);
                    this.cancel();
                }
                else if (team != null)
                {
                    for (EwPlayer ewplayer : team.getPlayersAlive())
                    {
                        Fireworks.genFirework(ewplayer.getPlayer().getLocation());
                    }
                }
            }
        }).runTaskTimer(EggWars.instance, 20L, 10L);
    }

    public static void sendFinishStats(EwPlayer pl)
    {
        Player ply = pl.getPlayer();
        StringBuilder builder = new StringBuilder();
        addFinishStat(builder, ply, TranslationUtils.getMessage("stats.endOfGame.game_lenght", ply), TranslationUtils.translateTime(ply, pl.getIngameStats().getStat(StatType.TIME_PLAYED), true));
        int kills = pl.getIngameStats().getStat(StatType.KILLS);

        if (kills > 0)
        {
            addFinishStat(builder, ply, TranslationUtils.getMessage("stats.kills", ply), String.valueOf(kills));
        }

        int deaths = pl.getIngameStats().getStat(StatType.DEATHS);

        if (deaths > 0)
        {
            addFinishStat(builder, ply, TranslationUtils.getMessage("stats.deaths", ply), deaths);
        }

        addFinishStat(builder, ply, TranslationUtils.getMessage("stats.endOfGame.kill_death", ply), String.format("%.2f", (double)kills / (double)(deaths <= 0 ? 1 : deaths)));
        int eggsBroken = pl.getIngameStats().getStat(StatType.EGGS_BROKEN);

        if (eggsBroken > 0)
        {
            addFinishStat(builder, ply, TranslationUtils.getMessage("stats.eggs_broken", ply), String.valueOf(eggsBroken));
        }

        int finalKills = pl.getIngameStats().getStat(StatType.ELIMINATIONS);

        if (finalKills > 0)
        {
            addFinishStat(builder, ply, TranslationUtils.getMessage("stats.eliminations", ply), String.valueOf(finalKills));
        }

        TranslationUtils.sendMessage("stats.endOfGame.container", ply, builder.toString());
    }

    private static void addFinishStat(StringBuilder builder, Player pl, Object... args)
    {
        if (builder.length() > 0)
        {
            builder.append("\n");
        }

        builder.append(TranslationUtils.getMessage("stats.endOfGame.stat", pl, args));
    }
}
