package me.rosillogames.eggwars.dependencies;

import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.database.PlayerData;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.Mode;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.language.Language;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class EggWarsExpansionPAPI extends PlaceholderExpansion
{
    @Override
    public String getAuthor()
    {
        return "RosilloGames";
    }

    @Override
    public String getIdentifier()
    {
        return "EggWars";
    }

    @Override
    public String getVersion()
    {
        return EggWars.EGGWARS_VERSION;
    }

    @Override
    public boolean canRegister()
    {
        return true;
    }

    @Override
    public boolean persist()
    {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params)
    {
        if (params.startsWith("arena"))
        {
            String[] s = params.split("_", 3);

            if (s.length < 3)
            {
                return null;
            }

            Arena arena = EggWars.getArenaManager().getArenaById(s[1]);

            if (arena != null)
            {
                if ("display_name".equalsIgnoreCase(s[2]))
                {
                    return arena.getName();
                }

                if ("time_until_start".equalsIgnoreCase(s[2]))
                {
                    return String.valueOf(arena.getCurrentCountdown());
                }

                if ("status_id".equalsIgnoreCase(s[2]))
                {
                    return arena.getStatus().toString();
                }

                if ("current_players".equalsIgnoreCase(s[2]))
                {
                    ArenaStatus status = arena.getStatus();
                    return String.valueOf((status.isGame() || status == ArenaStatus.FINISHING ? arena.getAlivePlayers() : arena.getPlayers()).size());
                }

                if ("min_players".equalsIgnoreCase(s[2]))
                {
                    return String.valueOf(arena.getMinPlayers());
                }

                if ("max_players".equalsIgnoreCase(s[2]))
                {
                    return String.valueOf(arena.getMaxPlayers());
                }
            }

            return "";
        }

        if (params.startsWith("translate"))
        {
            try
            {
                String[] ss = params.split("&");
                Language lang = EggWars.languageManager().getLanguageOrDefault(ss[1].substring(5));
                String key = ss[2];

                if (key.startsWith("time:"))
                {
                    if (ss.length < 4)
                    {
                        return TranslationUtils.translateTime(lang, Integer.parseInt(key.substring(5)), true);
                    }
                    else
                    {
                        return TranslationUtils.translateTime(lang, Integer.parseInt(key.substring(5)), Boolean.valueOf(ss[3].substring(5)));
                    }
                }
                else if (key.startsWith("key:"))
                {
                    if (ss.length < 4)
                    {
                        return TranslationUtils.getMessage(key.substring(4), lang);
                    }
                    else
                    {
                        return TranslationUtils.getMessage(key.substring(4), lang, (Object[])ss[3].substring(5).split("#AND#"));
                    }
                }
            }
            catch (Exception ex)
            {
            }

            return "";
        }

        if (player == null)
        {
            return "";
        }

        if (params.equalsIgnoreCase("id_arena"))
        {
            Arena arena = PlayerUtils.getEwPlayer(player).getArena();
            return arena == null ? "" : arena.getId();
        }
        else if (params.equalsIgnoreCase("id_team"))
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);

            if (ewplayer.getArena() == null)
            {
                return "";
            }
            else
            {
                return ewplayer.getTeam() == null ? "" : ewplayer.getTeam().getType().id();
            }
        }
        else if (params.equalsIgnoreCase("points"))
        {
            return Integer.valueOf(PlayerUtils.getEwPlayer(player).getPoints()).toString();
        }
        else//stat placeholders
        {
            PlayerData ew = EggWars.getDB().getPlayerData(player);

            if (params.startsWith("stat_ingame"))
            {
                String[] s = params.split("_", 3);
                EwPlayer pl = PlayerUtils.getEwPlayer(player);

                if (s.length < 3)
                {
                    return null;
                }

                StatType type;

                try
                {
                    type = StatType.valueOf(s[2].toUpperCase());
                }
                catch (Exception ex)
                {
                    return null;
                }

                if (!pl.isInArena() || pl.isEliminated())
                {
                    return "";
                }

                return String.valueOf(pl.getIngameStats().getStat(type));
            }
            else if (params.startsWith("stat_total"))
            {
                String[] s = params.split("_", 3);

                if (s.length < 3)
                {
                    return null;
                }

                StatType type;

                try
                {
                    type = StatType.valueOf(s[2].toUpperCase());
                }
                catch (Exception ex)
                {
                    return null;
                }

                return String.valueOf(ew.getTotalStat(type));
            }
            else if (params.startsWith("stat"))
            {
                String[] s = params.split("_", 3);

                if (s.length < 3)
                {
                    return null;
                }

                Mode mode;
                StatType type;

                try
                {
                    mode = Mode.valueOf(s[1].toUpperCase());
                    type = StatType.valueOf(s[2].toUpperCase());
                }
                catch (Exception ex)
                {
                    return null;
                }

                return String.valueOf(ew.getStat(type, mode));
            }
        }

        return null;
    }
}
