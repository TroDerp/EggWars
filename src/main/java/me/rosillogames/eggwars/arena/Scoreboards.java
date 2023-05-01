package me.rosillogames.eggwars.arena;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.TeamUtils;

@SuppressWarnings("deprecation")
public class Scoreboards
{
    public static void setScore(Arena arena)
    {
        for (EwPlayer ewplayer : arena.getPlayers())
        {
            setScore(ewplayer, arena);
        }
    }

    public static void setScore(EwPlayer ewplayer, Arena arena)
    {
        switch (arena.getStatus())
        {
        case LOBBY:
        case STARTING:
            setLobbyScore(ewplayer, arena);
            break;
        case SETTING:
        case STARTING_GAME:
        case IN_GAME:
        case FINISHING:
            setIngameScore(ewplayer, arena);
        }
    }

    public static void setLobbyScore(EwPlayer ewplayer, Arena arena)
    {
        Scoreboard scoreboard = ewplayer.getPlayer().getScoreboard();
        Objective objective = scoreboard.getObjective("lobby");

        if (objective != null)
        {
            objective.unregister();
        }

        objective = scoreboard.registerNewObjective("lobby", "dummy", "lobby");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(TranslationUtils.getMessage("gameplay.scoreboard.lobby.name", ewplayer.getPlayer()));
        setScoreboard(TranslationUtils.getMessage("gameplay.scoreboard.lobby.data", ewplayer.getPlayer(), new Object[] {arena.getPlayers().size(), ewplayer.getPoints(), arena.getMaxPlayers(), arena.getName()}).split("\\n"), objective);
        ewplayer.getPlayer().setScoreboard(scoreboard);
    }

    public static void setIngameScore(EwPlayer ewplayer, Arena arena)
    {
        Scoreboard scoreboard = ewplayer.getPlayer().getScoreboard();
        Objective objective = scoreboard.getObjective("ingame");

        if (objective != null)
        {
            objective.unregister();
        }

        objective = scoreboard.registerNewObjective("ingame", "dummy", "ingame");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(TranslationUtils.getMessage("gameplay.scoreboard.ingame.name", ewplayer.getPlayer()));
        Map<Team, String> hashmap = new HashMap();

        for (Team team : arena.getTeams().values())
        {
            if (!team.isEliminated())
            {
                hashmap.put(team, (team.canRespawn() ? "§a✔ " : "§c✘ ") + TeamUtils.translateTeamType(team.getType(), ewplayer.getPlayer(), true));
            }
        }

        setScoreboard(hashmap, objective);
        setBelowScoreboard(TranslationUtils.getMessage("gameplay.scoreboard.ingame.data", ewplayer.getPlayer(), new Object[] {arena.getAlivePlayers().size(), ewplayer.getPoints(), arena.getName()}).split("\\n"), objective);
        ewplayer.getPlayer().setScoreboard(scoreboard);
    }

    public static void clearScoreboard(Arena arena)
    {
        for (EwPlayer ewplayer : arena.getPlayers())
        {
            clearScoreboard(ewplayer.getPlayer());
        }
    }

    public static void clearScoreboard(Player player)
    {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public static void setScoreboard(String sa[], Objective objective)
    {
        int i = sa.length;

        for (String s : sa)
        {
            objective.getScore(s).setScore(i);
            i--;
        }
    }

    public static void setBelowScoreboard(String sa[], Objective objective)
    {
        int i = -1;

        for (String s : sa)
        {
            objective.getScore(s).setScore(i);
            i--;
        }
    }

    public static void setScoreboard(Map<Team, String> map, Objective objective)
    {
        for (Team team : map.keySet())
        {
            objective.getScore(map.get(team)).setScore(team.getPlayersAlive().size());
        }
    }
}
