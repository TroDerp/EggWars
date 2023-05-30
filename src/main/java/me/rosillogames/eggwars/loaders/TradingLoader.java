package me.rosillogames.eggwars.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.shop.Category;
import me.rosillogames.eggwars.arena.shop.Merchant;
import me.rosillogames.eggwars.arena.shop.MultiOffer;
import me.rosillogames.eggwars.arena.shop.Offer;
import me.rosillogames.eggwars.enums.ItemType;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.utils.GsonHelper;
import me.rosillogames.eggwars.utils.ItemUtils;

public class TradingLoader
{
    public static final String SPEC_TRADES_FILE = "trades_specific.json";
    private final List<Category> merchants = new ArrayList();

    public TradingLoader()
    {
    }

    public List<Category> getMerchants()
    {
        return new ArrayList(this.merchants);
    }

    public void loadTrades()
    {
        this.merchants.clear();
        EggWars.instance.getLogger().log(Level.INFO, "Loading data for main trades...");

        try
        {
            EggWars.instance.saveCustomResource("custom/trades.json", null, false);
            BufferedReader buffer = Files.newBufferedReader((new File(EggWars.instance.getDataFolder(), "custom/trades.json")).toPath());
            JsonObject shop = GsonHelper.getAsJsonObject(GsonHelper.convertToJsonObject(GsonHelper.parse(buffer), "trades"), "shop");

            for (Map.Entry<String, JsonElement> entry : shop.entrySet())
            {
                String name = entry.getKey();

                try
                {
                    Category category = loadCategory(GsonHelper.convertToJsonObject(entry.getValue(), "category"));

                    if (!category.isEmpty())
                    {
                        this.merchants.add(category);
                    }
                }
                catch (Exception ex1)
                {
                    EggWars.instance.getLogger().log(Level.WARNING, "Error loading trade category \"" + name + "\": ", ex1);
                }
            }

            buffer.close();
        }
        catch (Exception ex2)
        {
            EggWars.instance.getLogger().log(Level.WARNING, "Error loading main trades: ", ex2);
        }
    }

    public static Category loadCategory(JsonObject categoryJson)
    {
        ItemStack displayItem = ItemUtils.getItemLegacy(GsonHelper.getAsJsonObject(categoryJson, "display_item"));

        if (displayItem == null || displayItem.getType().equals(Material.AIR))
        {
            displayItem = new ItemStack(Material.BARRIER);
        }

        Category category = new Category(displayItem, GsonHelper.getAsString(categoryJson, "translation_key"));
        JsonObject typesJson = GsonHelper.getAsJsonObject(categoryJson, "types");
        Merchant defMerch = loadMerchant(GsonHelper.getAsJsonObject(typesJson, "default"), null);

        for (ItemType type : ItemType.values())
        {
            category.setMerchant(type, loadMerchant(GsonHelper.getAsJsonObject(typesJson, type.toString()), defMerch));
        }

        return category;
    }

    private static Merchant loadMerchant(JsonObject tradesJson, Merchant def)
    {
        JsonArray armorSlots = GsonHelper.getAsJsonArray(tradesJson, "armor_slots", new JsonArray());
        int[] armor = null;//these can be null

        if (armorSlots.size() >= 4)
        {
            armor = new int[4];

            for (int i = 0; i < 4; ++i)
            {
                armor[i] = GsonHelper.convertToInt(armorSlots.get(i), "armor_slot");
            }
        }

        if (def != null && armor == null && def.getArmorSlots() != null)
        {
            armor = def.getArmorSlots();
        }

        EwPlayerMenu.MenuSize size = EwPlayerMenu.MenuSize.parse(GsonHelper.getAsString(tradesJson, "menu_size", null), null);

        if (def != null && size == null && def.getSize() != null)
        {
            size = def.getSize();
        }
        else if (size == null)//this cannot be null
        {
            size = EwPlayerMenu.MenuSize.FULL;
        }

        Merchant merchant = new Merchant(size, armor);
        JsonArray offers = GsonHelper.getAsJsonArray(tradesJson, "offers", new JsonArray());

        for (int i = 0; i < offers.size(); ++i)
        {
            JsonObject offerjson = (JsonObject)GsonHelper.convertToJsonObject(offers.get(i), "offers");

            try
            {
                Price price = Price.parse(GsonHelper.getAsJsonObject(offerjson, "price"));
                JsonObject result = GsonHelper.getAsJsonObject(offerjson, "result");
                ItemStack resultItem = ItemUtils.getItemLegacy(GsonHelper.getAsJsonObject(result, "item"));
                int slot = GsonHelper.getAsInt(offerjson, "slot");
                boolean isDuplicate = GsonHelper.getAsBoolean(offerjson, "is_duplicate", false);

                if (resultItem == null || resultItem.getType().equals(Material.AIR))
                {
                    continue;
                }

                List<Pair<Boolean, ItemStack>> multiResults = new ArrayList();

                if (offerjson.has("multi_results"))
                {
                    JsonArray array = GsonHelper.getAsJsonArray(offerjson, "multi_results");

                    for (int j = 0; j < array.size(); ++j)
                    {
                        JsonObject result1 = (JsonObject)GsonHelper.convertToJsonObject(array.get(j), "items");
                        ItemStack resultItem1 = ItemUtils.getItemLegacy(GsonHelper.getAsJsonObject(result1, "item"));

                        if (resultItem1 == null || resultItem1.getType().equals(Material.AIR))
                        {
                            continue;
                        }

                        multiResults.add(Pair.of(GsonHelper.getAsBoolean(result1, "use_team_color", false), resultItem1));
                    }
                }

                Offer offer;

                if (!multiResults.isEmpty())
                {
                    offer = new MultiOffer(slot, GsonHelper.getAsString(offerjson, "name", null), multiResults, resultItem, price);
                }
                else
                {
                    offer = new Offer(slot, resultItem, price, isDuplicate);
                }

                if (GsonHelper.getAsBoolean(result, "use_team_color", false))
                {
                    offer.colorize = true;
                }

                merchant.addOffer(offer);
            }
            catch (Exception ex)
            {
                EggWars.instance.getLogger().log(Level.WARNING, "Error loading offer " + i + ": ", ex);
                continue;
            }
        }

        //Comment this merchant condition to make it always add offers from default
        if (def != null && /* merchant.isEmpty() && */ !def.isEmpty())
        {
            for (Offer offer : def.getOffers())
            {
                merchant.addOffer(offer);
            }
        }

        return merchant;
    }
}
