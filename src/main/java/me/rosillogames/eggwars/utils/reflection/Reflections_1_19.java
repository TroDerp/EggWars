package me.rosillogames.eggwars.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nullable;
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

public class Reflections_1_19 implements Reflections
{
    private final byte version;

    public Reflections_1_19(byte v)
    {
        this.version = v;
    }

    @Override
    public void setArmorStandInvisible(ArmorStand stand)
    {
        stand.setInvisible(true);
    }

    @Override
    public void setTNTSource(TNTPrimed tnt, Player player)
    {
        tnt.setSource(player);
    }

    @Override
    public void setItemAge(Item item, int age)
    {
        try
        {
            Object obj = item.getClass().getMethod("getHandle").invoke(item);
            Field field = obj.getClass().getField(this.version > 1 ? "g" : "ao");
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
            Class cItemStack = this.getNMSClass("world.item.ItemStack");
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
            Class cMojangsonParser = this.getNMSClass("nbt.MojangsonParser");
            Object blockNbt = cMojangsonParser.getMethod("a", String.class).invoke(null, string);
            Class cNBTCompound =  this.getNMSClass("nbt.NBTTagCompound");
            Class cGameProfileSerializer =  this.getNMSClass("nbt.GameProfileSerializer");
            Object blockData;

            if (this.version > 0)
            {
                //Now I'm starting to HATE that HolderLookup thing
                //BuiltInRegistries.BLOCK.asLookup(); = BuiltInRegistries.f.p();
                Class cHolderGetter =  this.getNMSClass("core.HolderGetter");
                Class cBIR =  this.getNMSClass("core.registries.BuiltInRegistries");
                Object registry = cBIR.getField("f").get((Object)null);
                Object holdLook = registry.getClass().getMethod("p").invoke(registry);
                blockData = cGameProfileSerializer.getMethod("a", cHolderGetter, cNBTCompound).invoke(null, holdLook, blockNbt);
            }
            else
            {
                blockData = cGameProfileSerializer.getMethod("c", cNBTCompound).invoke(null, blockNbt);
            }

            Class cIBlockData = this.getNMSClass("world.level.block.state.IBlockData");
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
            Class cItemStack = this.getNMSClass("world.item.ItemStack");
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
        meta.addItemFlags(ItemFlag.HIDE_DYE);
    }

    @Nullable
    @Override
    public String getStackName(ItemStack stack)
    {
        try
        {
            Class cItemStack = this.getNMSClass("world.item.ItemStack");
            Class cCraftItemStack = this.getOBCClass("inventory.CraftItemStack");
            Object nmsStack = cCraftItemStack.getMethod("asNMSCopy", stack.getClass()).invoke((Object)null, stack);
            Object nameComponent = cItemStack.getMethod("x").invoke(nmsStack);
            Class cIChatBase = this.getNMSClass("network.chat.IChatBaseComponent");
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
                Class cEnch = this.getNMSClass("world.item.enchantment.Enchantment");
                Object nameComponent = cEnch.getMethod("d", int.class).invoke(nmsEnch, entry.getValue());
                Class cIChatBase = this.getNMSClass("network.chat.IChatBaseComponent");
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
            Class cBlockPosition = this.getNMSClass("core.BlockPosition");
            Object world = loc.getWorld().getClass().getMethod("getHandle").invoke(loc.getWorld());
            Object blockPos = cBlockPosition.getConstructor(double.class, double.class,double.class).newInstance(loc.getX(), loc.getY(), loc.getZ());
            Object tileEntity = world.getClass().getMethod("getTileEntity", cBlockPosition).invoke(world, blockPos);

            if (tileEntity != null)
            {
                Class cIChatBase = this.getNMSClass("network.chat.IChatBaseComponent");
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
            Class cDmgSource = this.getNMSClass("world.damagesource.DamageSource");
            Object dmgSource;

            if (this.version > 1)
            {
            	Object dmgSources = nmsP.getClass().getMethod("dG").invoke(nmsP);
            	dmgSource = dmgSources.getClass().getMethod("m").invoke(dmgSources);
            }
            else
            {
            	dmgSource = cDmgSource.getField("m").get(null);
            }

            nmsP.getClass().getMethod("a", cDmgSource, float.class).invoke(nmsP, dmgSource, 10000F);
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
            Field field = obj.getClass().getField(this.version > 1 ? "e" : "b");
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            boolean boolVal = field.getBoolean(obj);
            field.setAccessible(accessible);
            obj.getClass().getMethod("a", this.getNMSClass("util.IProgressUpdate"), boolean.class, boolean.class).invoke(obj, null, true, false);
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
            Object nmsConnection = nmsPlayer.getClass().getField("b").get(nmsPlayer);
            nmsConnection.getClass().getMethod("a", this.getNMSClass("network.protocol.Packet")).invoke(nmsConnection, packetObj);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void sendTitle(Player player, Integer fadeInTime, Integer stayTime, Integer fadeOutTime, String title, String subtitle)
    {
        try
        {
            Class cTitleAnimPacket = this.getNMSClass("network.protocol.game.ClientboundSetTitlesAnimationPacket");
            Constructor constructor = cTitleAnimPacket.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
            Object packet = constructor.newInstance(fadeInTime, stayTime, fadeOutTime);
            this.sendPacket(player, packet);
            Class cIChatBase = this.getNMSClass("network.chat.IChatBaseComponent");

            if (title != null)
            {
                Class cTitleTextPacket = this.getNMSClass("network.protocol.game.ClientboundSetTitleTextPacket");
                Constructor constructor1 = cTitleTextPacket.getConstructor(cIChatBase);
                Object chatcomponent = cIChatBase.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
                Object packet1 = constructor1.newInstance(chatcomponent);
                this.sendPacket(player, packet1);
            }

            if (subtitle != null)
            {
                Class cSubtitleTextPacket = this.getNMSClass("network.protocol.game.ClientboundSetSubtitleTextPacket");
                Constructor constructor2 = cSubtitleTextPacket.getConstructor(cIChatBase);
                Object chatcomponent1 = cIChatBase.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + subtitle + "\"}");
                Object packet2 = constructor2.newInstance(chatcomponent1);
                this.sendPacket(player, packet2);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void sendActionBar(Player player, String s, Integer integer, Integer integer1, Integer integer2)
    {
        try
        {
            Class cTitleAnimPacket = this.getNMSClass("network.protocol.game.ClientboundSetTitlesAnimationPacket");
            Constructor constructor = cTitleAnimPacket.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
            Object packet = constructor.newInstance(integer, integer1, integer2);
            this.sendPacket(player, packet);

            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(s));
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
        list.add(Material.MANGROVE_WALL_SIGN);

        if (this.version > 0)
        {
            list.add(Material.BAMBOO_WALL_SIGN);
        }

        if (this.version > 1)
        {
            list.add(Material.CHERRY_WALL_SIGN);
        }

        return list;
    }
}
