package me.rosillogames.eggwars.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.objects.Token;
import me.rosillogames.eggwars.utils.GsonHelper;
import me.rosillogames.eggwars.utils.ItemUtils;

public class TokenLoader
{
    private static final String TOKENS_FILE_PATH = "custom/tokens.json";
    private final Map<String, Token> tokenTypes = new HashMap();

    public void loadTokens()
    {
        this.tokenTypes.clear();
        EggWars.instance.getLogger().log(Level.INFO, "Loading data for token types...");

        try
        {
            EggWars.instance.saveCustomResource(TOKENS_FILE_PATH, null, false);
            BufferedReader reader = Files.newBufferedReader((new File(EggWars.instance.getDataFolder(), TOKENS_FILE_PATH)).toPath());
            JsonObject tokensjson = GsonHelper.parse(reader);
            JsonObject tokenslist = GsonHelper.getAsJsonObject(tokensjson, "tokens");

            for (Map.Entry<String, JsonElement> token : tokenslist.entrySet())
            {
                String name = token.getKey();

                try
                {
                    JsonObject tokenjson = (JsonObject)GsonHelper.convertToJsonObject(token.getValue(), "token");
                    Material mat = ItemUtils.getItemType(GsonHelper.getAsString(tokenjson, "item_type"));

                    if (mat == null || mat.equals(Material.AIR))
                    {
                        continue;
                    }

                    ChatColor col = ChatColor.valueOf(GsonHelper.getAsString(tokenjson, "color"));
                    this.tokenTypes.put(name, new Token(mat, col, name));
                }
                catch (Exception ex1)
                {
                	EggWars.instance.getLogger().log(Level.WARNING, "Error loading token type \"" + name + "\":", ex1);
                }
            }
        }
        catch (Exception ex2)
        {
        	EggWars.instance.getLogger().log(Level.WARNING, "Error loading token types: ", ex2);
        }
    }

    @Nullable
    public Token byName(String name)
    {
        return this.tokenTypes.get(name);
    }
}
