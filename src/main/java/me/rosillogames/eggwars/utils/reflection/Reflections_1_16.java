package me.rosillogames.eggwars.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.google.gson.JsonObject;

public class Reflections_1_16 implements Reflections
{
    private final boolean newVersion;

    public Reflections_1_16(boolean newV)
    {
        this.newVersion = newV;
    }

    @Nullable
    private final Object toEntityHuman(Player player)
    {
        try
        {
            Method method = this.getOBCClass("entity.CraftPlayer").getDeclaredMethod("getHandle", new Class[0]);
            method.setAccessible(true);
            return method.invoke(player, new Object[0]);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    public void setArmorStandInvisible(ArmorStand stand)
    {
        try
        {
            Object obj = stand.getClass().getMethod("getHandle", new Class[0]).invoke(stand, new Object[0]);
            obj.getClass().getMethod("setInvisible", Boolean.TYPE).invoke(obj, Boolean.valueOf(true));
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void setTNTSource(TNTPrimed tnt, Player player)
    {
        try
        {
            Object obj = tnt.getClass().getMethod("getHandle", new Class[0]).invoke(tnt, new Object[0]);
            Class cEntityTNTPrimed = this.getNMSClass("EntityTNTPrimed");
            Field field = cEntityTNTPrimed.getDeclaredField("source");
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(obj, this.toEntityHuman(player));
            field.setAccessible(accessible);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void setItemAge(Item item, int age)
    {
        try
        {
            Object obj = item.getClass().getMethod("getHandle").invoke(item);
            Field field = obj.getClass().getField("age");
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(obj, age);
            field.setAccessible(accessible);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public ItemStack parseItemStack(JsonObject json)
    {
        try
        {
            Class cMojangsonParser = this.getNMSClass("MojangsonParser");
            Object itemNbt = cMojangsonParser.getMethod("parse", String.class).invoke(null, json.toString());
            Class cItemStack = this.getNMSClass("ItemStack");
            Class cNBTCompound = this.getNMSClass("NBTTagCompound");
            Object itemStack = cItemStack.getMethod("a", cNBTCompound).invoke(null, itemNbt);
            Class cCraftItemStack = this.getOBCClass("inventory.CraftItemStack");
            return (ItemStack)cCraftItemStack.getMethod("asBukkitCopy", cItemStack).invoke((Object)null, itemStack);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return new ItemStack(Material.AIR);
    }

    @Nullable
    @Override
    public BlockData parseBlockData(String string)
    {
        try
        {
            Class cMojangsonParser = this.getNMSClass("MojangsonParser");
            Object blockNbt = cMojangsonParser.getMethod("parse", String.class).invoke(null, string);
            Class cNBTCompound = this.getNMSClass("NBTTagCompound");
            Class cGameProfileSerializer = this.getNMSClass("GameProfileSerializer");
            Object blockData = cGameProfileSerializer.getMethod("c", cNBTCompound).invoke(null, blockNbt);
            Class cIBlockData = this.getNMSClass("IBlockData");
            Class cCraftBlockData = this.getOBCClass("block.data.CraftBlockData");
            return (BlockData)cCraftBlockData.getMethod("fromData", cIBlockData).invoke(null, blockData);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    public void hideDyeFlag(LeatherArmorMeta meta)
    {
        if (this.newVersion)
        {
            meta.addItemFlags(ItemFlag.HIDE_DYE);
        }
    }

    @Nullable
    @Override
    public String getStackName(ItemStack stack)
    {
        try
        {
            Class cItemStack = this.getNMSClass("ItemStack");
            Class cCraftItemStack = this.getOBCClass("inventory.CraftItemStack");
            Object nmsStack = cCraftItemStack.getMethod("asNMSCopy", stack.getClass()).invoke((Object)null, stack);
            Object nameComponent = cItemStack.getMethod("getName").invoke(nmsStack);
            Class cIChatBase = this.getNMSClass("IChatBaseComponent");
            Object string = cIChatBase.getMethod("getString").invoke(nameComponent);
            return (String)string;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    public List<String> getEnchantmentsLore(ItemStack stack)
    {
        List<String> list = new ArrayList();

        try
        {
            for (Map.Entry<Enchantment, Integer> entry : stack.getEnchantments().entrySet())
            {
                Class cCraftEnch = this.getOBCClass("enchantments.CraftEnchantment");
                Object nmsEnch = cCraftEnch.getMethod("getRaw", Enchantment.class).invoke(null, entry.getKey());
                Class cEnch = this.getNMSClass("Enchantment");
                Object nameComponent = cEnch.getMethod("d", int.class).invoke(nmsEnch, entry.getValue());
                Class cIChatBase = this.getNMSClass("IChatBaseComponent");
                Object string = cIChatBase.getMethod("getString").invoke(nameComponent);
                list.add("§7" + (String)string);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return list;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setEnchantGlint(ItemStack stack, boolean glint, boolean force)
    {
        if (!glint)
        {
            return;
        }

        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        stack.addUnsafeEnchantment(Enchantment.getByName("WATER_WORKER"), 1);
    }

    @Override
    public void killOutOfWorld(Player p)
    {
        try
        {
            Object nmsP = p.getClass().getMethod("getHandle").invoke(p);
            Class cDmgSource = this.getNMSClass("DamageSource");
            Object dmgSource = cDmgSource.getField("OUT_OF_WORLD").get(null);
            nmsP.getClass().getMethod("damageEntity", cDmgSource, float.class).invoke(nmsP, dmgSource, 10000F);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void saveFullWorld(World world)
    {
        try
        {
            Object obj = world.getClass().getMethod("getHandle").invoke(world);
            Field field = obj.getClass().getField("savingDisabled");
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            boolean boolVal = field.getBoolean(obj);
            field.setAccessible(accessible);
            obj.getClass().getMethod("save", this.getNMSClass("IProgressUpdate"), boolean.class, boolean.class).invoke(obj, null, true, false);
            field.setAccessible(true);
            field.set(obj, boolVal);
            field.setAccessible(accessible);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Nullable
    @Override
    public Block getEndChestBlock(Player p)
    {
        try
        {
            Object nmsP = p.getClass().getMethod("getHandle").invoke(p);
            Object nmsEC = nmsP.getClass().getMethod("getEnderChest").invoke(nmsP);
            Field field = nmsEC.getClass().getDeclaredFields()[0];
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            Object ecTE = field.get(nmsEC);
            field.setAccessible(accessible);

            if (ecTE != null)
            {
                Object blockPos = ecTE.getClass().getMethod("getPosition").invoke(ecTE);
                int x = (int)blockPos.getClass().getMethod("getX").invoke(blockPos);
                int y = (int)blockPos.getClass().getMethod("getY").invoke(blockPos);
                int z = (int)blockPos.getClass().getMethod("getZ").invoke(blockPos);
                nmsEC.getClass().getMethod("closeContainer", this.getNMSClass("EntityHuman")).invoke(nmsEC, nmsP);
                return p.getWorld().getBlockAt(x, y, z);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    public void sendPacket(Player player, Object packetObj)
    {
        try
        {
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object nmsConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            nmsConnection.getClass().getMethod("sendPacket", this.getNMSClass("Packet")).invoke(nmsConnection, packetObj);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void sendTitle(Player player, Integer integer, Integer integer1, Integer integer2, String s, String s1)
    {
        try
        {
            Object obj = this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get((Object)null);
            Constructor constructor = this.getNMSClass("PacketPlayOutTitle").getConstructor(this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], this.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            Object obj1 = constructor.newInstance(obj, null, integer, integer1, integer2);
            sendPacket(player, obj1);
            Object obj3;
            Object obj5;
            Constructor constructor2;
            Object obj7;

            if (s != null)
            {
                obj3 = this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get((Object)null);
                obj5 = this.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke((Object)null, "{\"text\":\"" + s + "\"}");
                constructor2 = this.getNMSClass("PacketPlayOutTitle").getConstructor(this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], this.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
                obj7 = constructor2.newInstance(obj3, obj5, integer, integer1, integer2);
                sendPacket(player, obj7);
            }

            if (s1 != null)
            {
                obj3 = this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get((Object)null);
                obj5 = this.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke((Object)null, "{\"text\":\"" + s1 + "\"}");
                constructor2 = this.getNMSClass("PacketPlayOutTitle").getConstructor(this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], this.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
                obj7 = constructor2.newInstance(obj3, obj5, integer, integer1, integer2);
                this.sendPacket(player, obj7);
            }
        }
        catch (Exception var13)
        {
            var13.printStackTrace();
        }
    }

    @Override
    public void sendActionBar(Player player, String s, Integer integer, Integer integer1, Integer integer2)
    {
        try
        {
            Constructor constructor = this.getNMSClass("PacketPlayOutTitle").getConstructor(this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], this.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            Object packet = constructor.newInstance(this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get((Object)null), null, integer, integer1, integer2);
            this.sendPacket(player, packet);
            Class cPacketPlayOutChat = this.getNMSClass("PacketPlayOutChat");
            Class cChatSerializer = this.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0];
            Object chatBaseComp = cChatSerializer.getDeclaredMethod("a", String.class).invoke((Object)null, "{\"text\": \"" + s + "\"}");
            Object packet1 = cPacketPlayOutChat.getConstructor(this.getNMSClass("IChatBaseComponent"), this.getNMSClass("ChatMessageType"), UUID.class).newInstance(chatBaseComp, this.getNMSClass("ChatMessageType").getField("GAME_INFO").get((Object)null), this.getNMSClass("SystemUtils").getField("b").get((Object)null));
            this.sendPacket(player, packet1);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public String getNMSPackageName()
    {
        return "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }
}
