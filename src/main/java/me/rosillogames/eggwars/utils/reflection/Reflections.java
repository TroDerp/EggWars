package me.rosillogames.eggwars.utils.reflection;

import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import com.google.gson.JsonObject;

public interface Reflections
{
    public void setArmorStandInvisible(ArmorStand stand);

    public void setTNTSource(TNTPrimed tnt, Player player);

    public void setItemAge(Item item, int age);

    public ItemStack parseItemStack(JsonObject json);

    @Nullable
    public BlockData parseBlockData(String string);

    public void hideDyeFlag(LeatherArmorMeta leatherArmorMeta);

    @Nullable
    public String getStackName(ItemStack stack);

    public List<String> getEnchantmentsLore(ItemStack stack);

    public void killOutOfWorld(Player p);

    public void saveFullWorld(World world);

    @Nullable
    public Block getEndChestBlock(Player player);

    public void sendPacket(Player player, Object packetObj);

    public void sendTitle(Player player, Integer fadeInTime, Integer stayTime, Integer fadeOutTime, String title, String subtitle);

    public void sendActionBar(Player player, String s, Integer integer, Integer integer1, Integer integer2);

    default String getNMSPackageName()
    {
        return "net.minecraft";
    }

    default Class getNMSClass(String className) throws ClassNotFoundException
    {
        return Class.forName(this.getNMSPackageName() + "." + className);
    }

    default Class getOBCClass(String className) throws ClassNotFoundException
    {
        return Class.forName("org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + className);
    }
}
