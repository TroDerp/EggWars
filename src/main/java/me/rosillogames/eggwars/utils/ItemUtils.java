package me.rosillogames.eggwars.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class ItemUtils
{
    public static NamespacedKey genType;
    public static NamespacedKey genLevel;
    public static NamespacedKey openMenu;
    public static NamespacedKey arenaId;

    public static int countItems(Player player, Material material)
    {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        int i = 0;
        int j = 0;

        do
        {
            if (j >= inv.getSize())
            {
                break;
            }

            ItemStack itemstack = inv.getItem(j);
            j++;

            if (itemstack != null && itemstack.getType().equals(material))
            {
                i += itemstack.getAmount();
            }
        }
        while (true);

        return i;
    }

    public static void removeItems(Player player, Material material, int i)
    {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        ArrayList<Map.Entry<Integer, ItemStack>> list = new ArrayList(inv.all(material).entrySet());
        Collections.sort(list, (obj0, obj1) -> ((ItemStack)((Map.Entry)obj0).getValue()).getAmount() - ((ItemStack)((Map.Entry)obj1).getValue()).getAmount());
        int j = 0;

        do
        {
            if (i <= 0)
            {
                break;
            }

            int slot = list.get(j).getKey().intValue();
            ItemStack itemstack = inv.getItem(slot);
            boolean flag = true;

            while (i > 0 && flag)
            {
                i--;

                if (itemstack.getAmount() <= 1)
                {
                    itemstack.setType(Material.AIR);
                    inv.setItem(slot, itemstack);
                    flag = false;
                    player.updateInventory();
                }
                else
                {
                    itemstack.setAmount(itemstack.getAmount() - 1);
                }
            }

            j++;
        }
        while (true);

        player.updateInventory();
    }

    public static Material getItemType(String id)
    {
        return ReflectionUtils.parseItemStack(GsonHelper.parse("{id:\"" + id + "\",Count:1}}", true)).getType();
    }

    public static ItemStack[] copyContents(ItemStack[] contents)
    {
        ItemStack[] copy = new ItemStack[contents.length];

        for (int idx = 0; idx < contents.length; ++idx)
        {
            ItemStack stack = contents[idx];

            if (stack != null)
            {
                copy[idx] = stack.clone();
            }
        }

        return copy;
    }

    public static ItemStack getItemLegacy(JsonObject jsonObject)
    {
        if (!jsonObject.has("Count"))
        {
            jsonObject.addProperty("Count", 1);
        }

        return ReflectionUtils.parseItemStack(jsonObject);
    }

    public static ItemStack getItemOrDefault(String s, Material material)
    {
        ItemStack stack1 = null;

        try
        {
            stack1 = getItemLegacy(GsonHelper.parse(s, true));
        }
        catch (Exception ex)
        {
            EggWars.instance.getLogger().log(Level.WARNING, "Error parsing json item \"" + s + "\", returning " + material, ex);
            return new ItemStack(material);
        }

        if (stack1 == null || stack1.getType().equals(Material.AIR))
        {
            return new ItemStack(material);
        }

        return stack1;
    }

    public static int getNearbyItemCount(Location loc, double radius, Material mat)
    {
        int chunkRadius = (int)radius >= 16 ? ((int)radius - (int)radius % 16) / 16 : 1;
        int count = 0;

        for (int xChunk = 0 - chunkRadius; xChunk <= chunkRadius; xChunk++)
        {
            for (int zChunk = 0 - chunkRadius; zChunk <= chunkRadius; zChunk++)
            {
                int x = (int)loc.getX();
                int y = (int)loc.getY();
                int z = (int)loc.getZ();
                Entity aentity[] = (new Location(loc.getWorld(), x + xChunk * 16, y, z + zChunk * 16)).getChunk().getEntities();
                int k1 = aentity.length;

                for (int l1 = 0; l1 < k1; l1++)
                {
                    Entity entity = aentity[l1];

                    if (entity.getLocation().distance(loc) <= radius && (entity instanceof Item) && ((Item)entity).getItemStack().getType().equals(mat))
                    {
                        count += ((Item)entity).getItemStack().getAmount();
                    }
                }
            }
        }

        return count;
    }

    public static BlockData getBlockData(String s, BlockData def)
    {
        BlockData data = ReflectionUtils.parseBlockData(s);

        if (data == null || data.getMaterial() == Material.AIR)
        {
            return def;
        }

        return data;
    }

    public static void setOpensMenu(ItemStack stack, MenuType menu)
    {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(openMenu, PersistentDataType.STRING, menu.toString());
        stack.setItemMeta(meta);
    }

    public static MenuType getOpensMenu(ItemStack stack)
    {
        try
        {
            return MenuType.parse(getPersistentData(stack, openMenu, PersistentDataType.STRING));
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    @Nullable
    public static <T, Z> Z getPersistentData(ItemStack stack, NamespacedKey key, PersistentDataType<T, Z> type)
    {
        try
        {
            return stack.getItemMeta().getPersistentDataContainer().get(key, type);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static ItemStack tryColorizeByTeam(TeamType team, ItemStack uncolored)
    {
        ItemStack colored = uncolored.clone();
        Colorizer.colorizeItem(colored, team.woolColor());
        return colored;
    }

    @SuppressWarnings({"deprecation", "removal"})
    public static ItemStack hideStackAttributes(ItemStack itemstack)
    {
        ItemMeta meta = itemstack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        if (EggWars.serverVersion.ordinal() >= Versions.V_1_20_R4.ordinal())
        {
            meta.addAttributeModifier(Attribute.values()[0], new AttributeModifier("dummy", 0, AttributeModifier.Operation.ADD_NUMBER));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            //the things above only affects paper because HIDE_ATTRIBUTES is not working there (paper's #10655)
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setMaxStackSize(99);
        }
        else
        {
            meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));
        }

        itemstack.setItemMeta(meta);
        return itemstack;
    }

    //add shield to offhand?
    public static ItemStack getSlot(Player player, EquipmentSlot slot)
    {
        if (slot == EquipmentSlot.HEAD)
        {
            return player.getInventory().getHelmet();
        }
        else if (slot == EquipmentSlot.CHEST)
        {
            return player.getInventory().getChestplate();
        }
        else if (slot == EquipmentSlot.LEGS)
        {
            return player.getInventory().getLeggings();
        }
        else if (slot == EquipmentSlot.FEET)
        {
            return player.getInventory().getBoots();
        }

        return player.getInventory().getItemInMainHand();
    }

    public static void setToSlot(Player player, EquipmentSlot slot, ItemStack item)
    {
        if (slot == EquipmentSlot.HEAD)
        {
            player.getInventory().setHelmet(item);
        }
        else if (slot == EquipmentSlot.CHEST)
        {
            player.getInventory().setChestplate(item);
        }
        else if (slot == EquipmentSlot.LEGS)
        {
            player.getInventory().setLeggings(item);
        }
        else if (slot == EquipmentSlot.FEET)
        {
            player.getInventory().setBoots(item);
        }
        else
        {
            player.getInventory().setItemInMainHand(item);
        }
    }

    public static EquipmentSlot getTradeSlot(Player player, ItemStack item)
    {
        Material mat = item.getType();

        //Leather & Gold
        if (mat == Material.LEATHER_HELMET || mat == Material.GOLDEN_HELMET)
        {
            Material armor = player.getInventory().getHelmet() != null ? player.getInventory().getHelmet().getType() : Material.AIR;
            return (armor == Material.CHAINMAIL_HELMET || armor == Material.IRON_HELMET || armor == Material.DIAMOND_HELMET || armor == Material.NETHERITE_HELMET) ? EquipmentSlot.HAND : EquipmentSlot.HEAD;
        }

        if (mat == Material.LEATHER_CHESTPLATE || mat == Material.GOLDEN_CHESTPLATE)
        {
            Material armor = player.getInventory().getChestplate() != null ? player.getInventory().getChestplate().getType() : Material.AIR;
            return (armor == Material.CHAINMAIL_CHESTPLATE || armor == Material.IRON_CHESTPLATE || armor == Material.DIAMOND_CHESTPLATE || armor == Material.NETHERITE_CHESTPLATE) ? EquipmentSlot.HAND : EquipmentSlot.CHEST;
        }

        if (mat == Material.LEATHER_LEGGINGS || mat == Material.GOLDEN_LEGGINGS)
        {
            Material armor = player.getInventory().getLeggings() != null ? player.getInventory().getLeggings().getType() : Material.AIR;
            return (armor == Material.CHAINMAIL_LEGGINGS || armor == Material.IRON_LEGGINGS || armor == Material.DIAMOND_LEGGINGS || armor == Material.NETHERITE_LEGGINGS) ? EquipmentSlot.HAND : EquipmentSlot.LEGS;
        }

        if (mat == Material.LEATHER_BOOTS || mat == Material.GOLDEN_BOOTS)
        {
            Material armor = player.getInventory().getBoots() != null ? player.getInventory().getBoots().getType() : Material.AIR;
            return (armor == Material.CHAINMAIL_BOOTS || armor == Material.IRON_BOOTS || armor == Material.DIAMOND_BOOTS || armor == Material.NETHERITE_BOOTS) ? EquipmentSlot.HAND : EquipmentSlot.FEET;
        }

        //Chain
        if (mat == Material.CHAINMAIL_HELMET)
        {
            Material armor = player.getInventory().getHelmet() != null ? player.getInventory().getHelmet().getType() : Material.AIR;
            return (armor == Material.IRON_HELMET || armor == Material.DIAMOND_HELMET || armor == Material.NETHERITE_HELMET) ? EquipmentSlot.HAND : EquipmentSlot.HEAD;
        }

        if (mat == Material.CHAINMAIL_CHESTPLATE)
        {
            Material armor = player.getInventory().getChestplate() != null ? player.getInventory().getChestplate().getType() : Material.AIR;
            return (armor == Material.IRON_CHESTPLATE || armor == Material.DIAMOND_CHESTPLATE || armor == Material.NETHERITE_CHESTPLATE) ? EquipmentSlot.HAND : EquipmentSlot.CHEST;
        }

        if (mat == Material.CHAINMAIL_LEGGINGS)
        {
            Material armor = player.getInventory().getLeggings() != null ? player.getInventory().getLeggings().getType() : Material.AIR;
            return (armor == Material.IRON_LEGGINGS || armor == Material.DIAMOND_LEGGINGS || armor == Material.NETHERITE_LEGGINGS) ? EquipmentSlot.HAND : EquipmentSlot.LEGS;
        }

        if (mat == Material.CHAINMAIL_BOOTS)
        {
            Material armor = player.getInventory().getBoots() != null ? player.getInventory().getBoots().getType() : Material.AIR;
            return (armor == Material.IRON_BOOTS || armor == Material.DIAMOND_BOOTS || armor == Material.NETHERITE_BOOTS) ? EquipmentSlot.HAND : EquipmentSlot.FEET;
        }

        //Iron
        if (mat == Material.IRON_HELMET)
        {
            Material armor = player.getInventory().getHelmet() != null ? player.getInventory().getHelmet().getType() : Material.AIR;
            return (armor == Material.DIAMOND_HELMET || armor == Material.NETHERITE_HELMET) ? EquipmentSlot.HAND : EquipmentSlot.HEAD;
        }

        if (mat == Material.IRON_CHESTPLATE)
        {
            Material armor = player.getInventory().getChestplate() != null ? player.getInventory().getChestplate().getType() : Material.AIR;
            return (armor == Material.DIAMOND_CHESTPLATE || armor == Material.NETHERITE_CHESTPLATE) ? EquipmentSlot.HAND : EquipmentSlot.CHEST;
        }

        if (mat == Material.IRON_LEGGINGS)
        {
            Material armor = player.getInventory().getLeggings() != null ? player.getInventory().getLeggings().getType() : Material.AIR;
            return (armor == Material.DIAMOND_LEGGINGS || armor == Material.NETHERITE_LEGGINGS) ? EquipmentSlot.HAND : EquipmentSlot.LEGS;
        }

        if (mat == Material.IRON_BOOTS)
        {
            Material armor = player.getInventory().getBoots() != null ? player.getInventory().getBoots().getType() : Material.AIR;
            return (armor == Material.DIAMOND_BOOTS || armor == Material.NETHERITE_BOOTS) ? EquipmentSlot.HAND : EquipmentSlot.FEET;
        }

        //Diamond
        if (mat == Material.DIAMOND_HELMET)
        {
            Material armor = player.getInventory().getHelmet() != null ? player.getInventory().getHelmet().getType() : Material.AIR;
            return (armor == Material.NETHERITE_HELMET) ? EquipmentSlot.HAND : EquipmentSlot.HEAD;
        }

        if (mat == Material.DIAMOND_CHESTPLATE)
        {
            Material armor = player.getInventory().getChestplate() != null ? player.getInventory().getChestplate().getType() : Material.AIR;
            return (armor == Material.NETHERITE_CHESTPLATE) ? EquipmentSlot.HAND : EquipmentSlot.CHEST;
        }

        if (mat == Material.DIAMOND_LEGGINGS)
        {
            Material armor = player.getInventory().getLeggings() != null ? player.getInventory().getLeggings().getType() : Material.AIR;
            return (armor == Material.NETHERITE_LEGGINGS) ? EquipmentSlot.HAND : EquipmentSlot.LEGS;
        }

        if (mat == Material.DIAMOND_BOOTS)
        {
            Material armor = player.getInventory().getBoots() != null ? player.getInventory().getBoots().getType() : Material.AIR;
            return (armor == Material.NETHERITE_BOOTS) ? EquipmentSlot.HAND : EquipmentSlot.FEET;
        }

        //Netherite
        if (mat == Material.NETHERITE_HELMET)
        {
            return EquipmentSlot.HEAD;
        }

        if (mat == Material.NETHERITE_CHESTPLATE)
        {
            return EquipmentSlot.CHEST;
        }

        if (mat == Material.NETHERITE_LEGGINGS)
        {
            return EquipmentSlot.LEGS;
        }

        if (mat == Material.NETHERITE_BOOTS)
        {
            return EquipmentSlot.FEET;
        }

        return EquipmentSlot.HAND;
    }
}
