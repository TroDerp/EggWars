package me.rosillogames.eggwars.arena.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Scoreboards;
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

        for (EwPlayer pl : arena.getPlayers())
        {
            if (!pl.isEliminated())
            {
                sendFinishStats(pl);
            }
        }

        if (winner != null)
        {
            for (EwPlayer pl1 : arena.getPlayers())
            {
                String teamName = TeamUtils.translateTeamType(winner.getType(), pl1.getPlayer(), false);
                TranslationUtils.sendMessage("gameplay.ingame.team_winned", pl1.getPlayer(), teamName);
                pl1.getPlayer().closeInventory();

                if (winner.equals(pl1.getTeam()))
                {
                    pl1.getIngameStats().addStat(StatType.WINS, 1);
                    PlayerUtils.addPoints(pl1, EggWars.instance.getConfig().getInt("gameplay.points.on_win"));
                }
            }

            Scoreboards.setScore(arena);
        }
        else
        {
            Scoreboards.clearScoreboard(arena);
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
        pl.getPlayer().sendMessage(ChatColor.GRAY + "------------------------------------");
        String prefix = ChatColor.DARK_GRAY + "� " + ChatColor.GRAY;
        pl.getPlayer().sendMessage(prefix + TranslationUtils.getMessage("statistics.game_lenght", pl.getPlayer(), ChatColor.YELLOW + TranslationUtils.translateTime(pl.getPlayer(), pl.getIngameStats().getStat(StatType.TIME_PLAYED), true)));
        pl.getPlayer().sendMessage(prefix + TranslationUtils.getMessage("statistics.kills", pl.getPlayer(), ChatColor.YELLOW + String.valueOf(pl.getIngameStats().getStat(StatType.KILLS))));
        int deaths = pl.getIngameStats().getStat(StatType.DEATHS);
        pl.getPlayer().sendMessage(prefix + TranslationUtils.getMessage("statistics.deaths", pl.getPlayer(), ChatColor.YELLOW + String.valueOf(pl.getIngameStats().getStat(StatType.DEATHS))));
        deaths = deaths <= 0 ? 1 : deaths;
        pl.getPlayer().sendMessage(prefix + TranslationUtils.getMessage("statistics.kill_death", pl.getPlayer(), ChatColor.YELLOW + String.format("%.2f", (double)pl.getIngameStats().getStat(StatType.KILLS) / (double)deaths)));
        pl.getPlayer().sendMessage(prefix + TranslationUtils.getMessage("statistics.eliminations", pl.getPlayer(), ChatColor.YELLOW + String.valueOf(pl.getIngameStats().getStat(StatType.ELIMINATIONS))));
        pl.getPlayer().sendMessage(ChatColor.GRAY + "------------------------------------");
    }
}
