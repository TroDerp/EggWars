package me.rosillogames.eggwars.language;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiConsumer;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.utils.GsonHelper;

public class Language
{
    private final Map<String, String> translations;
    private final String locale;

    public Language(String name, Language other)
    {
        this.locale = name;
        this.translations = ImmutableMap.copyOf(other.translations);
    }

    public Language(String name, Map<String, String> strings)
    {
        this.locale = name;
        this.translations = strings;
    }

    public String getOrDefault(final String string)
    {
        return this.translations.getOrDefault(string, LanguageManager.getDefaultLanguage().translations.getOrDefault(string, string));
    }

    public boolean has(final String string)
    {
        return this.translations.containsKey(string);
    }

    public String getLocale()
    {
        return this.locale;
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = 31 * result + ((this.locale == null) ? 0 : this.locale.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (this.getClass() != obj.getClass())
        {
            return false;
        }

        Language other = (Language)obj;

        if (this.locale == null)
        {
            if (other.locale != null)
            {
                return false;
            }
        }
        else if (!this.locale.equals(other.locale))
        {
            return false;
        }

        return true;
    }

    public static void loadFromJson(InputStream inputStream, BiConsumer<String, String> biConsumer)
    {
        JsonObject jsonobject = EggWars.instance.getGson().fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);

        for (Map.Entry<String, JsonElement> entry : jsonobject.entrySet())
        {
            biConsumer.accept(entry.getKey(), GsonHelper.convertToString(entry.getValue(), entry.getKey()));
        }
    }
}
