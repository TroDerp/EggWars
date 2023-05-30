package me.rosillogames.eggwars.language;

import java.util.Map;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class LanguageUtils
{
    public static boolean hasLanguage(String name)
    {
        for (Map.Entry<String, Language> lang : EggWars.languageManager().getLanguages().entrySet())
        {
            if (lang.getKey().equalsIgnoreCase(name))
            {
                return true;
            }
        }

        return false;
    }

    public static Language getLanguageByName(String name)
    {
        for (Map.Entry<String, Language> entry : EggWars.languageManager().getLanguages().entrySet())
        {
            if (entry.getKey().equalsIgnoreCase(name))
            {
                return entry.getValue();
            }
        }

        return LanguageManager.getDefaultLanguage();
    }

    public static Language getPlayerLanguage(final Player player)
    {
        EwPlayer pl = PlayerUtils.getEwPlayer(player);

        if (pl == null)
        {
            return LanguageManager.getDefaultLanguage();
        }

        return pl.getLanguage();
    }
}
