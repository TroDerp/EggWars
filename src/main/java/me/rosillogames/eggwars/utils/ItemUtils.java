package me.rosillogames.eggwars.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class ItemUtils
{
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
        } while(true);

        return i;
    }

    public static void removeItems(Player player, Material material, int i)
    {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        int j = 0;

        do
        {
            if (i <= 0)
            {
                break;
            }

            ItemStack itemstack = inv.getItem(j);
            j++;

            if (itemstack != null && itemstack.getType().equals(material))
            {
                boolean flag = true;

                while (i > 0 && flag) 
                {
                    i--;

                    if (itemstack.getAmount() <= 1)
                    {
                        itemstack.setType(Material.AIR);
                        inv.setItem(j - 1, itemstack);
                        flag = false;
                        player.updateInventory();
                    }
                    else
                    {
                        itemstack.setAmount(itemstack.getAmount() - 1);
                    }
                }
            }
        } while(true);

        player.updateInventory();
    }

    public static Material getItemType(String id)
    {
        return ReflectionUtils.parseItemStack(GsonHelper.parse("{id:\"" + id + "\",Count:1}}", true)).getType();
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

    public static Set getNearbyItems(Location location, double d)
    {
        int i = (int)d >= 16 ? ((int)d - (int)d % 16) / 16 : 1;
        HashSet hashset = new HashSet();

        for (int j = 0 - i; j <= i; j++)
        {
            for (int k = 0 - i; k <= i; k++)
            {
                int l = (int)location.getX();
                int i1 = (int)location.getY();
                int j1 = (int)location.getZ();
                Entity aentity[] = (new Location(location.getWorld(), l + j * 16, i1, j1 + k * 16)).getChunk().getEntities();
                int k1 = aentity.length;

                for (int l1 = 0; l1 < k1; l1++)
                {
                    Entity entity = aentity[l1];

                    if (entity.getLocation().distance(location) <= d && (entity instanceof Item))
                    {
                        hashset.add((Item)entity);
                    }
                }
            }
        }

        return hashset;
    }

    public static int getNearbyItemCount(Location location, double d, Material material)
    {
        int count = 0;
        int i = (int)d >= 16 ? ((int)d - (int)d % 16) / 16 : 1;
        HashSet hashset = new HashSet();

        for (int j = 0 - i; j <= i; j++)
        {
            for (int k = 0 - i; k <= i; k++)
            {
                int l = (int)location.getX();
                int i1 = (int)location.getY();
                int j1 = (int)location.getZ();
                Entity aentity[] = (new Location(location.getWorld(), l + j * 16, i1, j1 + k * 16)).getChunk().getEntities();
                int k1 = aentity.length;

                for (int l1 = 0; l1 < k1; l1++)
                {
                    Entity entity = aentity[l1];

                    if (entity.getLocation().distance(location) <= d && (entity instanceof Item) && ((Item)entity).getItemStack().getType().equals(material))
                    {
                        count += ((Item)entity).getItemStack().getAmount();
                    }
                }
            }
        }

        return count;
    }

    public static Set getNearbyItems(Location location, double d, Material material)
    {
        int i = (int)d >= 16 ? ((int)d - (int)d % 16) / 16 : 1;
        HashSet hashset = new HashSet();

        for (int j = 0 - i; j <= i; j++)
        {
            for (int k = 0 - i; k <= i; k++)
            {
                int l = (int)location.getX();
                int i1 = (int)location.getY();
                int j1 = (int)location.getZ();
                Entity aentity[] = (new Location(location.getWorld(), l + j * 16, i1, j1 + k * 16)).getChunk().getEntities();
                int k1 = aentity.length;

                for (int l1 = 0; l1 < k1; l1++)
                {
                    Entity entity = aentity[l1];

                    if (entity.getLocation().distance(location) <= d && (entity instanceof Item) && ((Item)entity).getItemStack().getType().equals(material))
                    {
                        hashset.add((Item)entity);
                    }
                }
            }
        }

        return hashset;
    }

    public static BlockData getBlockData(String s, BlockData def)
    {
        BlockData data = ReflectionUtils.parseBlockData(s);

        if (data == null)
        {
            return def;
        }

        return data;
    }

    public static ItemStack tryColorizeByTeam(TeamTypes team, ItemStack uncolored)
    {
        ItemStack colored = uncolored.clone();
        Colorizer.colorizeItem(colored, team.woolColor());
        return colored;
    }

    public static ItemStack hideStackAttributes(ItemStack itemstack)
    {
        ItemMeta itemmeta = itemstack.getItemMeta();
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        itemmeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemstack.setItemMeta(itemmeta);
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
