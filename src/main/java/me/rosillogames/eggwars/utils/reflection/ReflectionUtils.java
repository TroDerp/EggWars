package me.rosillogames.eggwars.utils.reflection;

import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

    public static <T extends Entity> T createEntity(World world, Location location, Class<? extends Entity> clazz, T fallback)
    {
        return currentReflections.createEntity(world, location, clazz, fallback);
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

    public static JsonObject getItemJson(ItemStack stack)
    {
        return currentReflections.getItemJson(stack);
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

    public static List<String> getEnchantmentsLore(ItemStack stack)
    {
        return currentReflections.getEnchantmentsLore(stack);
    }

    public static void setFormatAndSetSignLines(Location loc, String line1, String line2, String line3, String line4)
    {
        currentReflections.setFormatAndSetSignLines(loc, line1, line2, line3, line4);
    }

    public static void killOutOfWorld(Player p)
    {
        currentReflections.killOutOfWorld(p);
    }

    public static void saveFullWorld(World world)
    {
        currentReflections.saveFullWorld(world);
    }

    public static void sendPacket(Player player, Object packetObj)
    {
        currentReflections.sendPacket(player, packetObj);
    }

    public static void sendTitle(Player player, Integer fadeInTime, Integer stayTime, Integer fadeOutTime, String title, String subtitle)
    {
        currentReflections.sendTitle(player, fadeInTime, stayTime, fadeOutTime, title, subtitle);
    }

    public static void sendActionBar(Player player, String s, Integer integer, Integer integer1, Integer integer2)
    {
        currentReflections.sendActionBar(player, s, integer, integer1, integer2);
    }

    public static List<Material> getWallSigns()
    {
        return currentReflections.getWallSigns();
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
            case OTHER:
            case V_1_19_R3:
                currentReflections = new Reflections_1_19((byte)2);
        }
    }
}
