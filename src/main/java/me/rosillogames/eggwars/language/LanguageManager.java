package me.rosillogames.eggwars.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import me.rosillogames.eggwars.EggWars;

public class LanguageManager
{
    private final Map<String, Language> languages = new HashMap();
    private Language defaultLanguage = null;
    /** Ignores the language of the client player if it hasn't selected any language on the plugin and uses the default language instead */
    public boolean ignoreClientLanguage;

    public void loadConfig()
    {
        this.ignoreClientLanguage = EggWars.instance.getConfig().getBoolean("plugin.ignore_client_language", false);
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

                    if (locale.equals("default"))
                    {
                        EggWars.instance.getLogger().log(Level.WARNING, "A language cannot be called \"default\"; skipping");
                        continue;
                    }

                    try
                    {
                        this.languages.put(locale, loadLang(file, locale));
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
            this.defaultLanguage = new Language("default", this.languages.getOrDefault(defLocale, this.languages.values().iterator().next()));
        }
    }

    private static Language loadLang(File file, String locale) throws IOException
    {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder();
        BiConsumer<String, String> biconsumer = builder::put;
        FileInputStream fileInputStream = new FileInputStream(file);
        Language.loadFromJson(fileInputStream, biconsumer);
        fileInputStream.close();
        return new Language(locale, builder.build());
    }

    public Map<String, Language> getLanguages()
    {
        return this.languages;
    }

    public Language getLanguageOrDefault(String locale)
    {
        return this.languages.getOrDefault(locale, getDefaultLanguage());
    }

    public static Language getDefaultLanguage()
    {
        return EggWars.languageManager().defaultLanguage;
    }
}
