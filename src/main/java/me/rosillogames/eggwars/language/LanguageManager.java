package me.rosillogames.eggwars.language;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.google.gson.JsonParseException;
import me.rosillogames.eggwars.EggWars;

public class LanguageManager
{
    public static final String DEFAULT_NAME = "default";
    private final Map<String, Language> languages = new HashMap();
    private final Map<DamageCause, List<String>> deathMessages = new EnumMap(DamageCause.class);
    private Language defaultLanguage = null;
    /** Ignores the language of the client player if it hasn't selected any language on the plugin and uses the default language instead */
    public boolean ignoreClientLanguage;

    public void loadConfig()
    {
        FileConfiguration config = EggWars.instance.getConfig();
        this.ignoreClientLanguage = config.getBoolean("plugin.ignore_client_language", false);
        this.deathMessages.clear();
        String optKey = "death_message_keys";

        if (config.isConfigurationSection(optKey))
        {
            ConfigurationSection section = config.getConfigurationSection(optKey);

            for (DamageCause cause : DamageCause.values())
            {
                List<String> list = section.getStringList(cause.name());

                if (list.isEmpty())
                {
                    list.add("generic");
                }

                this.deathMessages.put(cause, list);
            }
        }
    }

    public void loadLangs()
    {
        this.languages.clear();
        EggWars.instance.getLogger().log(Level.INFO, "Loading data for languages...");

        try
        {
            EggWars.instance.saveCustomResource("langs/en_us.json", null, false);
            EggWars.instance.saveCustomResource("langs/es_es.json", null, false);
            File langsFolder = new File(EggWars.instance.getDataFolder(), "langs");

            for (File file : langsFolder.listFiles())
            {
                if (file.isFile())
                {
                    String locale = file.getName().replaceFirst(".json", "");

                    if (locale.equals(DEFAULT_NAME))
                    {
                        EggWars.instance.getLogger().log(Level.WARNING, "A language cannot be called \"" + DEFAULT_NAME + "\"; skipping");
                        continue;
                    }

                    try
                    {
                        this.languages.put(locale, Language.loadFromJsonFile(file, locale));
                    }
                    catch (IOException | JsonParseException ex)
                    {
                        EggWars.instance.getLogger().log(Level.WARNING, "Error loading language data for file \"" + file.getName() + "\": " + ex);
                    }
                }
            }
        }
        catch (Exception ex1)
        {
            EggWars.instance.getLogger().log(Level.SEVERE, "Error loading languages: ", ex1);
        }

        String defLocale = EggWars.instance.getConfig().getString("plugin.language", "en_us");

        if (!this.languages.isEmpty())
        {
            //do NOT add "default" language to lang map to avoid further issues with player loading
            this.defaultLanguage = new Language(DEFAULT_NAME, this.languages.getOrDefault(defLocale, this.languages.values().iterator().next()));
        }
    }

    public Map<String, Language> getLanguages()
    {
        return this.languages;
    }

    public Language getLanguageOrDefault(String locale)
    {
        return this.languages.getOrDefault(locale, getDefaultLanguage());
    }

    public String getDeathMsgKey(DamageCause cause)
    {
        List<String> list = this.deathMessages.get(cause);

        if (list != null)
        {
            return list.get((new Random()).nextInt(list.size()));
        }

        return cause.name().toLowerCase();
    }

    public static Language getDefaultLanguage()
    {
        return EggWars.languageManager().defaultLanguage;
    }

    public static Language getPlayerLanguage(Player player)
    {
        String locale = EggWars.getDB().getPlayerData(player).getLocale();

        if (locale.equals(LanguageManager.DEFAULT_NAME))
        {
            if (EggWars.languageManager().ignoreClientLanguage)
            {
                return LanguageManager.getDefaultLanguage();
            }

            locale = player.getLocale();
        }

        return EggWars.languageManager().getLanguageOrDefault(locale);
    }
}
