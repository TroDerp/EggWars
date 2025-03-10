package me.rosillogames.eggwars.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
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
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.menu.SerializingItems;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class ItemUtils
{
    private static String COUNT_TAG_KEY = null;
    public static EwNamespace<String> genType;
    public static EwNamespace<Integer> genLevel;
    public static EwNamespace<String> arenaId;

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

    public static void setOptionalHUDItem(EwPlayer player, int slot, ItemStack stack)
    {
        player.getPlayer().getInventory().setItem(slot, stack);
    }

    public static void removeItems(Player player, Material material, int amount)
    {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        List<Pair<Integer, ItemStack>> list = new ArrayList();//Don't use inv.all(Material)!! it doesn't select offhand or armor

        for (int i = 0; i < inv.getSize(); i++)
        {
            ItemStack stack = inv.getItem(i);

            if (stack != null && stack.getType().equals(material))
            {
                list.add(new Pair(i, stack));
            }
        }

        Collections.sort(list, (obj0, obj1) -> obj0.getRight().getAmount() - obj1.getRight().getAmount());
        boolean offHandChanged = false;

        for (int j = 0; amount > 0; ++j)
        {
            int slot = list.get(j).getLeft().intValue();
            ItemStack itemstack = inv.getItem(slot);
            boolean isConsumed = true;//if the item has become empty, to skip to next item

            while (amount > 0 && isConsumed)
            {
                amount--;

                if (itemstack.getAmount() <= 1)
                {
                    itemstack.setType(Material.AIR);
                    inv.setItem(slot, itemstack);
                    isConsumed = false;
                    player.updateInventory();//update for the item replaced by air
                }
                else
                {
                    itemstack.setAmount(itemstack.getAmount() - 1);
                }
            }

            if (slot == inv.getSize() - 1)
            {
                offHandChanged = true;
            }
        }

        player.updateInventory();//update for the item that has lost some of the amount

        if (offHandChanged)
        {//Small hack because off-hand didn't update visually
            inv.setItemInOffHand(inv.getItemInOffHand());
        }
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
        if (COUNT_TAG_KEY == null)
        {
            COUNT_TAG_KEY = EggWars.serverVersion.ordinal() >= Versions.V_1_20_R4.ordinal() ? "count" : "Count";
        }

        if (!jsonObject.has(COUNT_TAG_KEY))
        {
            jsonObject.addProperty(COUNT_TAG_KEY, 1);
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
        SerializingItems.OPEN_MENU.setItemReference(stack, menu);
    }

    public static MenuType getOpensMenu(ItemStack stack)
    {
        try
        {
            return SerializingItems.OPEN_MENU.getItemReference(stack);
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
    public static ItemStack makeMenuItem(ItemStack itemstack)
    {
        ItemMeta meta = itemstack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        if (EggWars.serverVersion.ordinal() >= Versions.V_1_20_R4.ordinal())
        {
            if (EggWars.fixPaperOBC && EggWars.serverVersion.ordinal() == Versions.V_1_21_R3.ordinal())
            {//this only affects paper because HIDE_ATTRIBUTES is not working there (paper's issue #10655)
                meta.addAttributeModifier(Attribute.values()[0], new AttributeModifier("dummy", 0, AttributeModifier.Operation.ADD_NUMBER));
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

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
        else if (slot == EquipmentSlot.OFF_HAND)
        {
            return player.getInventory().getItemInOffHand();
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
        else if (slot == EquipmentSlot.OFF_HAND)
        {
            player.getInventory().setItemInOffHand(item);
        }
        else
        {
            player.getInventory().setItemInMainHand(item);
        }
    }
}
