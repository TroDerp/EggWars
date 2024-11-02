package me.rosillogames.eggwars.utils.reflection;

import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.enums.Versions;

public class ReflectionUtils
{
    private static Reflections currentReflections;

    public static <T extends Entity> T createEntity(World world, Location location, Class<? extends Entity> clazz, T fallback)
    {
        try
        {
            Object obj = world.getClass().getMethod("createEntity", Location.class, Class.class).invoke(world, location, clazz);

            if (obj instanceof Entity)
            {
                return (T)obj;
            }

            return (T)obj.getClass().getMethod("getBukkitEntity").invoke(obj);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return fallback;
    }

    public static void setArmorStandInvisible(ArmorStand stand)
    {
        currentReflections.setArmorStandInvisible(stand);
    }

    public static void setTNTSource(TNTPrimed tnt, Player player)
    {
        currentReflections.setTNTSource(tnt, player);
    }

    public static void setItemAge(Item item, int age)
    {
        currentReflections.setItemAge(item, age);
    }

    public static ItemStack parseItemStack(JsonObject json)
    {
        return currentReflections.parseItemStack(json);
    }

    @Nullable
    public static BlockData parseBlockData(String string)
    {
        return currentReflections.parseBlockData(string);
    }

    public static void hideDyeFlag(LeatherArmorMeta leatherArmorMeta)
    {
        currentReflections.hideDyeFlag(leatherArmorMeta);
    }

    @Nullable
    public static String getStackName(ItemStack stack)
    {
        return currentReflections.getStackName(stack);
    }

    @Deprecated
    /**
     * Used in < 1.20.5 to copy all enchantments lore when skipping the enchant glint,
     * however it is now possible by disabling enchantment glint without removing enchs
     */
    public static List<String> getEnchantmentsLore(ItemStack stack)
    {
        return currentReflections.getEnchantmentsLore(stack);
    }

    public static void setEnchantGlint(ItemStack stack, boolean enable, boolean force)
    {
        currentReflections.setEnchantGlint(stack, enable, force);
    }

    public static void killOutOfWorld(Player p)
    {
        currentReflections.killOutOfWorld(p);
    }

    public static void saveFullWorld(World world)
    {
        currentReflections.saveFullWorld(world);
    }

    @Nullable
    public static Block getEndChestBlock(Player player)
    {
        return currentReflections.getEndChestBlock(player);
    }

    public static void sendPacket(Player player, Object packetObj)
    {
        currentReflections.sendPacket(player, packetObj);
    }

    public static void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title, String subtitle)
    {
        currentReflections.sendTitle(player, Integer.valueOf(fadeIn), Integer.valueOf(stay), Integer.valueOf(fadeOut), title, subtitle);
    }

    public static void sendActionBar(Player player, String s, int fadeIn, int stay, int fadeOut)
    {
        currentReflections.sendActionBar(player, s, Integer.valueOf(fadeIn), Integer.valueOf(stay), Integer.valueOf(fadeOut));
    }

    public static void setReflections(Versions v)
    {
        switch (v)
        {
            case V_1_16_R1:
            case V_1_16_R2:
                currentReflections = new Reflections_1_16(false);
                return;
            case V_1_16_R3:
                currentReflections = new Reflections_1_16(true);
                return;
            case V_1_17:
                currentReflections = new Reflections_1_17();
                return;
            case V_1_18_R1:
                currentReflections = new Reflections_1_18(false);
                return;
            case V_1_18_R2:
                currentReflections = new Reflections_1_18(true);
                return;
            case V_1_19_R1:
                currentReflections = new Reflections_1_19((byte)0);
                return;
            case V_1_19_R2:
                currentReflections = new Reflections_1_19((byte)1);
                return;
            case V_1_19_R3:
                currentReflections = new Reflections_1_19((byte)2);
                return;
            case V_1_20_R1:
                currentReflections = new Reflections_1_20_0((byte)0);
                return;
            case V_1_20_R2:
                currentReflections = new Reflections_1_20_0((byte)1);
                return;
            case V_1_20_R3:
                currentReflections = new Reflections_1_20_0((byte)2);
                return;
            case V_1_20_R4:
                currentReflections = new Reflections_1_20_1();
                return;
            case V_1_21_R1:
                currentReflections = new Reflections_1_21(false);
                return;
            case OTHER:
            case V_1_21_R2:
                currentReflections = new Reflections_1_21(true);
                return;
        }
    }
}
