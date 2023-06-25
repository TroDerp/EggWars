package me.rosillogames.eggwars.arena;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.TeamUtils;

@SuppressWarnings("deprecation")
public class Scoreboards
{
    private final Arena arena;

    public Scoreboards(Arena arenaIn)
    {
        this.arena = arenaIn;
    }

    public void updateScores(boolean updateTeams)
    {
        for (EwPlayer ewplayer : this.arena.getPlayers())
        {
            this.setScore(ewplayer);

            if (updateTeams)
            {
                this.setTeamScores(ewplayer);
            }
        }
    }

    public void setScore(EwPlayer ewplayer)
    {
        switch (this.arena.getStatus())
        {
            case LOBBY:
            case STARTING:
                this.setLobbyScore(ewplayer);
                break;
            case SETTING:
            case STARTING_GAME:
            case IN_GAME:
            case FINISHING:
                this.setIngameScore(ewplayer);
        }
    }

    private void setLobbyScore(EwPlayer ewplayer)
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
        setScoreboard(TranslationUtils.getMessage("gameplay.scoreboard.lobby.data", ewplayer.getPlayer(), new Object[] {this.arena.getPlayers().size(), ewplayer.getPoints(), this.arena.getMaxPlayers(), this.arena.getName()}).split("\\n"), objective);
        ewplayer.getPlayer().setScoreboard(scoreboard);
    }

    private void setIngameScore(EwPlayer ewplayer)
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

        for (Team team : this.arena.getTeams().values())
        {
            if (!team.isEliminated())
            {
                objective.getScore((team.canRespawn() ? "§a✔ " : "§c✘ ") + TeamUtils.translateTeamType(team.getType(), ewplayer.getPlayer(), true)).setScore(team.getPlayersAlive().size());
            }
        }

        setBelowScoreboard(TranslationUtils.getMessage("gameplay.scoreboard.ingame.data", ewplayer.getPlayer(), new Object[] {this.arena.getAlivePlayers().size(), ewplayer.getPoints(), this.arena.getName()}).split("\\n"), objective);
        Objective objective1 = scoreboard.getObjective("kills");

        if (objective1 != null)
        {
            objective1.unregister();
        }

        if (EggWars.config.showKills)
        {
            objective1 = scoreboard.registerNewObjective("kills", "dummy", "kills");
            objective1.setDisplaySlot(DisplaySlot.PLAYER_LIST);

            for (EwPlayer arenaPlayer : this.arena.getPlayers())
            {
                objective1.getScore(arenaPlayer.getPlayer().getName()).setScore(arenaPlayer.getIngameStats().getStat(StatType.KILLS));
            }
        }

        ewplayer.getPlayer().setScoreboard(scoreboard);
    }

    public void setTeamScores(EwPlayer ewplayer)
    {
        Scoreboard scoreboard = ewplayer.getPlayer().getScoreboard();

        for (Map.Entry<TeamType, Team> entry : this.arena.getTeams().entrySet())
        {
            TeamType type = entry.getKey();
            org.bukkit.scoreboard.Team mcTeam = scoreboard.getTeam(type.id());

            if (mcTeam != null)
            {
                mcTeam.unregister();
            }

            if (!this.arena.getStatus().isLobby())
            {
                mcTeam = scoreboard.registerNewTeam(type.id());
                mcTeam.setDisplayName(TeamUtils.translateTeamType(type, ewplayer.getPlayer(), true));
                mcTeam.setColor(type.color());
                mcTeam.setCanSeeFriendlyInvisibles(true);
                mcTeam.setPrefix(TeamUtils.teamPrefix(type, ewplayer.getPlayer()) + " ");

                for (EwPlayer teamplayer : entry.getValue().getPlayers())
                {
                    mcTeam.addEntry(teamplayer.getPlayer().getName());
                }
            }
        }

        ewplayer.getPlayer().setScoreboard(scoreboard);
    }

    public void clearScoreboards()
    {
        for (EwPlayer ewplayer : this.arena.getPlayers())
        {
            this.clearScoreboard(ewplayer.getPlayer());
        }
    }

    public void clearScoreboard(Player player)
    {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    private static void setScoreboard(String sa[], Objective objective)
    {
        int i = sa.length;

        for (String s : sa)
        {
            objective.getScore(s).setScore(i);
            i--;
        }
    }

    private static void setBelowScoreboard(String sa[], Objective objective)
    {
        int i = -1;

        for (String s : sa)
        {
            objective.getScore(s).setScore(i);
            i--;
        }
    }
}
