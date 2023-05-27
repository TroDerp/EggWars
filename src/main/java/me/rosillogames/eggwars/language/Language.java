package me.rosillogames.eggwars.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.utils.GsonHelper;

public class Language
{
    private final Map<String, StringProvider> translations;
    private final String locale;

    public Language(String name, Language other)
    {
    	this(name, ImmutableMap.copyOf(other.translations));
    }

    private Language(String name, Map<String, StringProvider> strings)
    {
        this.locale = name;

        for (StringProvider provider : strings.values())
        {
        	if (provider instanceof StringProvider.Reference)
        	{
        		((StringProvider.Reference)provider).setLang(this);
        	}
        }

        this.translations = strings;
    }

    public StringProvider getOrDefault(final String string)
    {
        return this.translations.getOrDefault(string, LanguageManager.getDefaultLanguage().translations.getOrDefault(string, (new StringProvider.Default(string))));
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

    public static Language loadFromJsonFile(File file, String locale) throws IOException
    {
    	FileInputStream fileinputstream = new FileInputStream(file);
        ImmutableMap.Builder<String, StringProvider> builder = ImmutableMap.<String, StringProvider>builder();
        JsonObject jsonobject = EggWars.instance.getGson().fromJson(new InputStreamReader(fileinputstream, StandardCharsets.UTF_8), JsonObject.class);

        for (Map.Entry<String, JsonElement> entry : jsonobject.entrySet())
        {
        	String key = entry.getKey();
        	JsonElement element = entry.getValue();

        	if (GsonHelper.isStringValue(element))
        	{
        		builder.put(key, new StringProvider.Default(GsonHelper.convertToString(element, key)));
        		continue;
        	}

        	if (element.isJsonArray())
        	{
        		JsonArray array = element.getAsJsonArray();
        		String[] strings = new String[array.size()];

        		for (int i = 0; i < array.size(); i++)
                {
        			if (GsonHelper.isStringValue(array.get(i)))
        			{
        				strings[i] = array.get(i).getAsString();
        			}
                }

        		if (strings.length > 0)
        		{
            		builder.put(key, new StringProvider.Multiple(strings));
        			continue;
        		}
        	}

        	if (element.isJsonObject())
        	{
        		JsonElement string = element.getAsJsonObject().get("reference");

        		if (string != null && string.isJsonPrimitive() && string.getAsJsonPrimitive().isString())
        		{
            		builder.put(key, new StringProvider.Reference(string.getAsString()));
            		continue;
        		}
        	}

            EggWars.instance.getLogger().log(Level.WARNING, "Language \"" + locale + "\" contains an unknown json element for translation key \"" + key + "\".");
        }

        fileinputstream.close();
        return new Language(locale, builder.build());
    }
}
