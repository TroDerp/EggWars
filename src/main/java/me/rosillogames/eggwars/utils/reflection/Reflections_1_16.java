package me.rosillogames.eggwars.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;

import me.rosillogames.eggwars.EggWars;

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
    public <T extends Entity> T createEntity(World world, Location location, Class<? extends Entity> clazz, T fallback)
    {
        try
        {
            Object obj = world.getClass().getMethod("createEntity", Location.class, Class.class).invoke(world, location, clazz);
            fallback = (T)obj.getClass().getMethod("getBukkitEntity").invoke(obj);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return fallback;
    }

    @Override
    public ItemStack parseItemStack(JsonObject json)
    {
        try
        {
            Class cItemStack = this.getNMSClass("ItemStack");
            DataResult<Pair> result = ((Decoder)cItemStack.getField("a").get((Object)null)).decode(JsonOps.INSTANCE, json);
            HelpObject<ItemStack> helpstack = new HelpObject<ItemStack>();
            Class cCraftItemStack = this.getOBCClass("inventory.CraftItemStack");
            helpstack.object = new ItemStack(Material.AIR);
            result.resultOrPartial((s) ->
            {
                EggWars.instance.getLogger().log(Level.WARNING, s);
            }).ifPresent((legacystack) ->
            {
                try
                {
                    helpstack.object = (ItemStack)cCraftItemStack.getMethod("asBukkitCopy", cItemStack).invoke((Object)null, legacystack.getFirst());
                }
                catch (Exception var5)
                {
                }

            });
            return helpstack.object;
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
    public JsonObject getItemJson(ItemStack stack)
    {
        HelpObject<JsonObject> helpjson = new HelpObject<JsonObject>();
        helpjson.object = new JsonObject();

        try
        {
            Class cCraftItemStack = this.getOBCClass("inventory.CraftItemStack");
            Object nmsStack = cCraftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, stack);
            Class cItemStack = this.getNMSClass("ItemStack");
            DataResult<JsonObject> result = ((Encoder)cItemStack.getField("a").get(null)).encode(nmsStack, JsonOps.INSTANCE, new JsonObject());
            result.resultOrPartial((s) ->
            {
                EggWars.instance.getLogger().log(Level.WARNING, s);
            }).ifPresent((jsonObj) ->
            {
                helpjson.object = jsonObj;
            });
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return helpjson.object;
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
    public void setFormatAndSetSignLines(Location loc, String line1, String line2, String line3, String line4)
    {
        try
        {
            Class cBlockPos = this.getNMSClass("BlockPosition");
            Object world = loc.getWorld().getClass().getMethod("getHandle").invoke(loc.getWorld());
            Object blockPos = cBlockPos.getConstructor(double.class, double.class, double.class).newInstance(loc.getX(), loc.getY(), loc.getZ());
            Object tileEntity = world.getClass().getMethod("getTileEntity", cBlockPos).invoke(world, blockPos);

            if (tileEntity != null)
            {
                Class cIChatBase = this.getNMSClass("IChatBaseComponent");
                Class cChatSerializer = cIChatBase.getClasses()[0];
                Object fLine1 = cChatSerializer.getMethod("a", String.class).invoke(null, line1);
                Object fLine2 = cChatSerializer.getMethod("a", String.class).invoke(null, line2);
                Object fLine3 = cChatSerializer.getMethod("a", String.class).invoke(null, line3);
                Object fLine4 = cChatSerializer.getMethod("a", String.class).invoke(null, line4);
                tileEntity.getClass().getMethod("a", int.class, cIChatBase).invoke(tileEntity, 0, fLine1);
                tileEntity.getClass().getMethod("a", int.class, cIChatBase).invoke(tileEntity, 1, fLine2);
                tileEntity.getClass().getMethod("a", int.class, cIChatBase).invoke(tileEntity, 2, fLine3);
                tileEntity.getClass().getMethod("a", int.class, cIChatBase).invoke(tileEntity, 3, fLine4);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
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
    public List<Material> getWallSigns()
    {
        List<Material> list = new ArrayList<Material>();
        list.add(Material.OAK_WALL_SIGN);
        list.add(Material.SPRUCE_WALL_SIGN);
        list.add(Material.BIRCH_WALL_SIGN);
        list.add(Material.JUNGLE_WALL_SIGN);
        list.add(Material.DARK_OAK_WALL_SIGN);
        list.add(Material.ACACIA_WALL_SIGN);
        list.add(Material.CRIMSON_WALL_SIGN);
        list.add(Material.WARPED_WALL_SIGN);
        return list;
    }

    @Override
    public String getNMSPackageName()
    {
        return "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }
}
