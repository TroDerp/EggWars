package me.rosillogames.eggwars.managers;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.objects.Kit;
import me.rosillogames.eggwars.objects.KitsMenu;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.InventoryController;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.GsonHelper;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.Pair;

public class KitManager
{
    private static TranslatableItem invItem;
    public static int cooldownSeconds;
    private final List<Kit> kits = new ArrayList();
    private final KitsMenu kitsMenu = new KitsMenu();

    public KitManager()
    {
    }

    public static void loadConfig()
    {
        ItemStack stack = ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.kit_selection.item"), Material.PAPER);
        ItemUtils.hideStackAttributes(stack);
        ItemUtils.setOpensMenu(stack, MenuType.KIT_SELECTION);
        invItem = TranslatableItem.translatableNameLore(stack, "gameplay.kits.item_lore", "gameplay.kits.item_name");
        cooldownSeconds = EggWars.instance.getConfig().getInt("kits.cooldown_time");
    }

    public static ItemStack getInvItem(Player player)
    {
        return invItem.getTranslated(player);
    }

    public void loadKits()
    {
        this.kits.clear();
        EggWars.instance.getLogger().log(Level.INFO, "Loading data for kits...");

        try
        {
            EggWars.instance.saveCustomResource("custom/kits.json", null, false);
            BufferedReader bufferedReader13 = Files.newBufferedReader((new File(EggWars.instance.getDataFolder(), "custom/kits.json")).toPath());
            JsonElement jsonelement = GsonHelper.parse(bufferedReader13);
            JsonObject kitjson = GsonHelper.convertToJsonObject(jsonelement, "kits");
            JsonObject kitsobj = GsonHelper.getAsJsonObject(kitjson, "kits");

            for (Map.Entry<String, JsonElement> entry : kitsobj.entrySet())
            {
                String key = entry.getKey();

                try
                {
                    JsonObject kit = (JsonObject)GsonHelper.convertToJsonObject(entry.getValue(), "kit");
                    ItemStack displayItem = ItemUtils.getItemLegacy(GsonHelper.getAsJsonObject(kit, "display_item"));

                    if (displayItem == null || displayItem.getType().equals(Material.AIR))
                    {
                        displayItem = new ItemStack(Material.BARRIER);
                    }

                    ItemUtils.hideStackAttributes(displayItem);
                    List<Pair<EquipmentSlot, ItemStack>> items = Lists.<Pair<EquipmentSlot, ItemStack>>newArrayList();
                    JsonArray itemlist = GsonHelper.getAsJsonArray(kit, "items", new JsonArray());

                    for (int i = 0; i < itemlist.size(); ++i)
                    {
                        JsonObject itemjson = (JsonObject)GsonHelper.convertToJsonObject(itemlist.get(i), "items");
                        ItemStack item = ItemUtils.getItemLegacy(GsonHelper.getAsJsonObject(itemjson, "item"));

                        if (item == null || item.getType().equals(Material.AIR))
                        {
                            continue;
                        }

                        EquipmentSlot slot = null;

                        try
                        {
                            slot = EquipmentSlot.valueOf(GsonHelper.getAsString(itemjson, "custom_slot").toUpperCase());
                        }
                        catch (Exception ex)
                        {
                        }

                        items.add(new Pair(slot, item));
                    }

                    int cooldown = GsonHelper.getAsInt(kit, "cooldown_time", -1);
                    int price = GsonHelper.getAsInt(kit, "price", 0);
                    this.kits.add(new Kit(items, key, displayItem, price, cooldown));
                }
                catch (Exception ex)
                {
                    EggWars.instance.getLogger().log(Level.WARNING, "Error loading kit \"" + key + "\":", ex);
                }
            }
        }
        catch (Exception ex1)
        {
            EggWars.instance.getLogger().log(Level.WARNING, "Error loading kits: ", ex1);
        }

        this.kitsMenu.loadGui();
    }

    public List<Kit> getKits()
    {
        return new ArrayList(this.kits);
    }

    @Nullable
    public Kit getKit(String kit)
    {
        for (Kit kt : this.kits)
        {
            if (kt.id().equals(kit))
            {
                return kt;
            }
        }

        return null;
    }

    public KitsMenu getKitsMenu()
    {
        return this.kitsMenu;
    }

    public void openKitsInv(Player player, int page)
    {
        InventoryController.openInventory(player, kitsMenu.getInventory(page), MenuType.KIT_SELECTION).setExtraData(page);
    }

    public static boolean buyKit(EwPlayer ewplayer, Kit kit)
    {
        boolean flag = ewplayer.getPlayer().hasPermission("eggwars.freekits");

        if (ewplayer.getPoints() < kit.price() && !flag)
        {
            return false;
        }

        EggWars.getDB().getPlayerData(ewplayer.getPlayer()).unlockKit(kit.id());

        if (!flag)
        {
            ewplayer.setPoints(ewplayer.getPoints() - kit.price());
        }

        return true;
    }

    public static boolean canBuy(EwPlayer ewplayer, Kit kit)
    {
        return ewplayer.getPlayer().hasPermission("eggwars.freekits") || ewplayer.getPoints() >= kit.price();
    }
}
