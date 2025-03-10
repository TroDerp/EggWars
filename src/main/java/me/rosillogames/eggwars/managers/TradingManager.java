package me.rosillogames.eggwars.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.shop.Category;
import me.rosillogames.eggwars.arena.shop.MultiOffer;
import me.rosillogames.eggwars.arena.shop.Offer;
import me.rosillogames.eggwars.arena.shop.TradeResult;
import me.rosillogames.eggwars.enums.ItemType;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.menu.ProfileMenus;
import me.rosillogames.eggwars.menu.ShopMenu;
import me.rosillogames.eggwars.objects.AutoEquipEntry;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.GsonHelper;
import me.rosillogames.eggwars.utils.ItemUtils;

public class TradingManager
{
    private static final String TRADES_FILE = "custom/trades.json";
    public static final String SPEC_TRADES_FILE = "trades_specific.json";
    private final Map<ItemType, ShopMenu> shopsPerType = new EnumMap<ItemType, ShopMenu>(ItemType.class);
    private final Map<String, AutoEquipEntry> autoEquips = new HashMap<String, AutoEquipEntry>();

    public TradingManager()
    {
        for (ItemType type : ItemType.values())
        {
            this.shopsPerType.put(type, new ShopMenu(type));
        }
    }

    public Map<ItemType, ShopMenu> getShops()
    {
        return new EnumMap(this.shopsPerType);
    }

    public void openEggWarsShop(EwPlayer player, ItemType type)
    {
        this.shopsPerType.get(type).addOpener(player);
    }

    public void loadTrades()
    {
        for (ShopMenu shop : this.shopsPerType.values())
        {
            shop.resetClear();
        }

        this.autoEquips.clear();
        EggWars.instance.getLogger().log(Level.INFO, "Loading data for eggwars shop...");

        try
        {
            EggWars.instance.saveCustomResource(TRADES_FILE, null, false);
            BufferedReader buffer = Files.newBufferedReader((new File(EggWars.instance.getDataFolder(), "custom/trades.json")).toPath());
            JsonObject fileJson = GsonHelper.convertToJsonObject(GsonHelper.parse(buffer), "trades");

            if (isConfigCompatible(fileJson, TRADES_FILE))
            {
                JsonObject equipJson = GsonHelper.getAsJsonObject(fileJson, "auto_equip_configurations");

                for (Map.Entry<String, JsonElement> entry : equipJson.entrySet())
                {
                    String name = entry.getKey();

                    try
                    {
                        AutoEquipEntry conf = loadAutoEquip(GsonHelper.convertToJsonObject(entry.getValue(), "auto_equip_entry"));

                        if (conf != null)
                        {
                            this.autoEquips.put(name, conf);
                        }
                    }
                    catch (Exception ex)
                    {
                        EggWars.instance.getLogger().log(Level.WARNING, "Error loading auto equipping config for \"" + name + "\": ", ex);
                    }
                }

                JsonObject shopJson = GsonHelper.getAsJsonObject(fileJson, "shop");

                try
                {
                    for (ItemType type : ItemType.values())
                    {
                        ShopMenu shopMenu = this.shopsPerType.get(type);
                        JsonObject tierJson = GsonHelper.getAsJsonObject(shopJson, type.toString());
        
                        for (Map.Entry<String, JsonElement> entry : tierJson.entrySet())
                        {
                            String name = entry.getKey();
        
                            try
                            {
                                Category category = this.loadCategory(name, GsonHelper.convertToJsonObject(entry.getValue(), "category"));
        
                                if (!category.isEmpty())
                                {
                                    shopMenu.addCategory(name, category);
                                }
                            }
                            catch (Exception ex1)
                            {
                                EggWars.instance.getLogger().log(Level.WARNING, "Error loading trade category \"" + name + "\" for " + type.toString() + " tier: ", ex1);
                            }
                        }
                    }
                }
                catch (Exception ex2)
                {
                    EggWars.instance.getLogger().log(Level.WARNING, "Error loading shop merchants: ", ex2);
                }
            }

            buffer.close();
        }
        catch (IOException ex2)
        {
            EggWars.instance.getLogger().log(Level.WARNING, "Error loading trades.json: ", ex2);
        }
    }

    public Category loadSpecialCategory(File folder, ItemType type) throws IOException
    {
        BufferedReader buffer = Files.newBufferedReader((new File(folder, SPEC_TRADES_FILE)).toPath());
        JsonObject specCategJson = GsonHelper.convertToJsonObject(GsonHelper.parse(buffer), "special_category");
        Category category = null;

        if (isConfigCompatible(specCategJson, SPEC_TRADES_FILE))
        {
            JsonObject tierJson = GsonHelper.getAsJsonObject(GsonHelper.getAsJsonObject(specCategJson, "category_tiers"), type.toString());
            category = this.loadCategory("special", tierJson);
        }

        buffer.close();
        return category;//Error loading special shop category for arena \"
    }

    private Category loadCategory(String catName, JsonObject categoryJson)
    {
        ItemStack displayItem = ItemUtils.getItemLegacy(GsonHelper.getAsJsonObject(categoryJson, "display_item"));

        if (displayItem == null || displayItem.getType().equals(Material.AIR))
        {
            displayItem = new ItemStack(Material.BARRIER);
        }

        JsonObject armorSlots = GsonHelper.getAsJsonObject(categoryJson, "viewable_equipment", new JsonObject());
        Map<EquipmentSlot, Integer> armor = new EnumMap(EquipmentSlot.class);

        for (Map.Entry<String, JsonElement> entry : armorSlots.entrySet())
        {
            String name = entry.getKey();

            if ("hand".equalsIgnoreCase(name))
            {
                continue;
            }

            try
            {
                armor.put(EquipmentSlot.valueOf(name.toUpperCase()), GsonHelper.convertToInt(entry.getValue(), "slot"));
            }
            catch (Exception ex)
            {
                EggWars.instance.getLogger().log(Level.WARNING, "Error loading viewable slot " + name + ": ", ex);
                continue;
            }
        }

        ProfileMenus.MenuSize size = ProfileMenus.MenuSize.parse(GsonHelper.getAsString(categoryJson, "menu_size", null), null);

        if (size == null)//this cannot be null
        {
            size = ProfileMenus.MenuSize.FULL;
        }

        Category category = new Category(size, armor, displayItem, GsonHelper.getAsString(categoryJson, "translation_key"));
        List<Integer> usedSlots = new ArrayList();
        JsonObject offers = GsonHelper.getAsJsonObject(categoryJson, "offers");

        for (Map.Entry<String, JsonElement> entry : offers.entrySet())
        {
            String offName = String.format("%s:%s", catName, entry.getKey());
            JsonObject offerjson = GsonHelper.convertToJsonObject(entry.getValue(), "offer");

            try
            {
                Price price = Price.parse(GsonHelper.getAsJsonObject(offerjson, "price"));
                JsonObject resultjson = GsonHelper.getAsJsonObject(offerjson, "result");
                TradeResult result = this.getResult(resultjson);

                if (result == null)
                {
                    continue;
                }

                int slot = GsonHelper.getAsInt(offerjson, "slot");

                if (usedSlots.contains(Integer.valueOf(slot)))
                {
                    throw new IllegalStateException("Duplicate input slot!");
                }
                else if (slot >= size.getSlots())
                {
                    throw new IllegalStateException("The input slot is outside the menu!");
                }

                boolean isDuplicate = GsonHelper.getAsBoolean(offerjson, "is_duplicate", false);
                List<TradeResult> multiResults = new ArrayList();
                JsonArray array = GsonHelper.getAsJsonArray(offerjson, "multi_results", new JsonArray());

                for (int j = 0; j < array.size(); ++j)
                {
                    TradeResult multiResult = this.getResult(GsonHelper.convertToJsonObject(array.get(j), "items"));

                    if (multiResult == null)
                    {
                        continue;
                    }

                    multiResults.add(multiResult);
                }

                Offer offer;

                if (!multiResults.isEmpty())
                {
                    offer = new MultiOffer(slot, result, multiResults, price);
                }
                else
                {
                    offer = new Offer(slot, result, price, isDuplicate);
                }

                category.addOffer(offName, offer);
            }
            catch (Exception ex)
            {
                EggWars.instance.getLogger().log(Level.WARNING, "Error loading offer " + offName + ": ", ex);
                continue;
            }
        }

        return category;
    }

    private TradeResult getResult(JsonObject resultJ)
    {
        ItemStack resultItem = ItemUtils.getItemLegacy(GsonHelper.getAsJsonObject(resultJ, "item"));

        if (resultItem == null || resultItem.getType().equals(Material.AIR))
        {
            return null;
        }

        TradeResult result = new TradeResult(resultItem);
        result.setUsesTeamColor(GsonHelper.getAsBoolean(resultJ, "use_team_color", false));
        String equip = GsonHelper.getAsString(resultJ, "equipment_config", "");

        /* If the item has no equipment config assigned but there's an equipment config 
         * with no name (empty string), that equipment config will be used. */
        if (this.autoEquips.containsKey(equip))
        {
            result.setAutoEquip(this.autoEquips.get(equip));
        }

        result.setNameTranslation(GsonHelper.getAsString(resultJ, "translate_name", ""));
        result.setDescTranslation(GsonHelper.getAsString(resultJ, "translate_desc", ""));
        result.setInheritsNameDesc(GsonHelper.getAsBoolean(resultJ, "keep_info_when_bought", false));
        return result;
    }

    private static AutoEquipEntry loadAutoEquip(JsonObject entryJson) throws Exception
    {
        String slotArg = GsonHelper.getAsString(entryJson, "slot");
        EquipmentSlot slot = EquipmentSlot.valueOf(slotArg.toUpperCase());

        if (slot == null || slot == EquipmentSlot.HAND)
        {
            return null;
        }

        boolean replaceEnchs = GsonHelper.getAsBoolean(entryJson, "replace_enchanted");
        JsonArray replaces = GsonHelper.getAsJsonArray(entryJson, "replace", new JsonArray());
        List<Material> doesReplace = new ArrayList();
        List<Material> doesNotReplace = new ArrayList();

        for (int i = 0; i < replaces.size(); ++i)
        {
            String item = GsonHelper.convertToString(replaces.get(i), "item_type");

            if (item.equals("*"))
            {
                return new AutoEquipEntry(slot, replaceEnchs);
            }

            Material gotMaterial = null;
            boolean negate = false;

            if (item.startsWith("!"))
            {
                gotMaterial = ItemUtils.getItemType(item.replaceFirst("!", ""));
                negate = true;
            }
            else
            {
                gotMaterial = ItemUtils.getItemType(item);
            }

            if (gotMaterial != null && gotMaterial != Material.AIR)
            {
                (negate ? doesNotReplace : doesReplace).add(gotMaterial);
            }
        }

        return new AutoEquipEntry(slot, doesReplace, doesNotReplace, replaceEnchs);
    }

    public static boolean isConfigCompatible(JsonObject fileJson, String name)
    {
        JsonArray formatvs = GsonHelper.getAsJsonArray(fileJson, "format_versions", new JsonArray());
        String[] versions = new String[formatvs.size()];

        for (int i = 0; i < versions.length; i++)
        {
            versions[i] = GsonHelper.convertToString(formatvs.get(i), "format_version"); 
        }

        if (versions.length != 0 && !EggWars.serverVersion.isFormatCompatible(versions))
        {
            EggWars.instance.getLogger().log(Level.WARNING, "File " + name + " is not compatible with "
                + "this server version! Your server is currently compatible with files with versions "
                + Versions.getCompatibleList(new String[] {String.valueOf(EggWars.serverVersion.getFormatVersion())})
                + " and this one is meant for versions " + Versions.getCompatibleList(versions)
                + "! Skipping...");
            return false;
        }

        return true;
    }
}
