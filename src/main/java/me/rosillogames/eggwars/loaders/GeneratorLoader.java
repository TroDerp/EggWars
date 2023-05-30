package me.rosillogames.eggwars.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.arena.GeneratorType;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.objects.Token;
import me.rosillogames.eggwars.utils.GsonHelper;
import me.rosillogames.eggwars.utils.ItemUtils;

public class GeneratorLoader
{
    private static ItemStack upgradeItem;
    private final Map<String, GeneratorType> generators = new HashMap();

    public GeneratorLoader()
    {
    }

    public static void loadConfig()
    {
        upgradeItem = ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.generator_upgrading"), Material.EXPERIENCE_BOTTLE);
        ItemUtils.hideStackAttributes(upgradeItem);
    }

    public ItemStack getUpgradeItem()
    {
        return upgradeItem.clone();
    }

    @Nullable
    public GeneratorType getType(String type)
    {
        return this.generators.get(type);
    }

    public Map<String, GeneratorType> getGenerators()
    {
        return new HashMap(this.generators);
    }

    public void loadGenerators()
    {
        this.generators.clear();
        EggWars.instance.getLogger().log(Level.INFO, "Loading data for generators...");

        try
        {
            EggWars.instance.saveCustomResource("custom/generators.json", null, false);
            BufferedReader reader = Files.newBufferedReader((new File(EggWars.instance.getDataFolder(), "custom/generators.json")).toPath());
            JsonElement jsonelement = GsonHelper.parse(reader);
            JsonObject generatorsjson = GsonHelper.convertToJsonObject(jsonelement, "generators");
            int defDropRate = GsonHelper.getAsInt(generatorsjson, "default_drop_rate", 20);
            int defMaxItems = GsonHelper.getAsInt(generatorsjson, "default_max_items", 25);
            JsonObject generatorslist = GsonHelper.getAsJsonObject(generatorsjson, "generators");

            for (Map.Entry<String, JsonElement> entry5 : generatorslist.entrySet())
            {
                String name = entry5.getKey();

                try
                {
                    JsonObject generator = (JsonObject)GsonHelper.convertToJsonObject(entry5.getValue(), "generator");
                    boolean showTimeTag = GsonHelper.getAsBoolean(generator, "show_time_tag", false);
                    boolean requiresNearbyPlayers = GsonHelper.getAsBoolean(generator, "requires_nearby_players", false);
                    int color = GsonHelper.getAsInt(generator, "color", 0);
                    Token droppedToken = EggWars.getTokenManager().byName(GsonHelper.getAsString(generator, "dropped_token"));

                    if (droppedToken == null || droppedToken.getMaterial().equals(Material.AIR))
                    {
                        continue;
                    }

                    Map<Integer, Integer> maxItems = Maps.newHashMap();
                    Map<Integer, Integer> tickRates = Maps.newHashMap();
                    Map<Integer, Price> prices = Maps.newHashMap();
                    JsonArray levellist = GsonHelper.getAsJsonArray(generator, "levels", new JsonArray());
                    int lvls = 1;

                    for (int i = 0; i < levellist.size(); ++i)
                    {
                        JsonObject leveljson = (JsonObject)GsonHelper.convertToJsonObject(levellist.get(i), "levels");

                        try
                        {
                            maxItems.put(lvls, GsonHelper.getAsInt(leveljson, "max_items", defMaxItems));
                            tickRates.put(lvls, GsonHelper.getAsInt(leveljson, "drop_rate", defDropRate));
                            prices.put(lvls, Price.parse(GsonHelper.getAsJsonObject(leveljson, "upgrade_price")));
                        }
                        catch (Exception ex)
                        {
                            continue;
                        }

                        lvls++;
                    }

                    this.generators.put(name, new GeneratorType(maxItems, tickRates, prices, name, droppedToken, showTimeTag, requiresNearbyPlayers, lvls - 1, color));
                }
                catch (Exception ex1)
                {
                    EggWars.instance.getLogger().log(Level.WARNING, "Error loading generator \"" + name + "\":", ex1);
                }
            }
        }
        catch (Exception ex2)
        {
            EggWars.instance.getLogger().log(Level.WARNING, "Error loading generators: ", ex2);
        }

        //On plugin load arenas are loaded before so there is no problem.
        for (Arena arena : EggWars.getArenaManager().getArenas())
        {
            for (Generator gen : arena.getGenerators().values())
            {
                gen.reloadCache();
                gen.updateSign();
            }
        }
    }
}
