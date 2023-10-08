package me.rosillogames.eggwars.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nullable;
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
import org.bukkit.inventory.meta.LeatherArmorMeta;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import me.rosillogames.eggwars.EggWars;

public class Reflections_1_18 implements Reflections
{
    private final String fl_1;
    private final String fl_2;
    private final String fl_3;

    public Reflections_1_18(boolean newV)
    {
        this.fl_1 = newV ? "ao" : "ap";
        this.fl_2 = newV ? "w" : "v";
        this.fl_3 = newV ? "fD" : "fC";
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
            Field field = obj.getClass().getField(this.fl_1);
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
            Class cItemStack = this.getNMSClass("world.item.ItemStack");
            DataResult<Pair> result = ((Decoder)cItemStack.getField("a").get((Object)null)).decode(JsonOps.INSTANCE, json);
            HelpObject<ItemStack> helpstack = new HelpObject<ItemStack>();
            Class cCraftItemStack = this.getOBCClass("inventory.CraftItemStack");
            helpstack.object = new ItemStack(Material.AIR);
            result.resultOrPartial(s -> EggWars.instance.getLogger().log(Level.WARNING, s)).ifPresent((legacystack) ->
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
            Class cNBTCompound = this.getNMSClass("nbt.NBTTagCompound");
            Class cGameProfileSerializer = this.getNMSClass("nbt.GameProfileSerializer");
            Object blockData = cGameProfileSerializer.getMethod("c", cNBTCompound).invoke(null, blockNbt);
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
            Object nameComponent = cItemStack.getMethod(this.fl_2).invoke(nmsStack);
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
                Class cEnchantment = this.getNMSClass("world.item.enchantment.Enchantment");
                Object nameComponent = cEnchantment.getMethod("d", int.class).invoke(nmsEnch, entry.getValue());
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
    public void killOutOfWorld(Player p)
    {
        try
        {
            Object nmsP = p.getClass().getMethod("getHandle").invoke(p);
            Class cDmgSource = this.getNMSClass("world.damagesource.DamageSource");
            Object dmgSource = cDmgSource.getField("m").get(null);
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
            Field field = obj.getClass().getField("b");
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

    @Nullable
    @Override
    public Block getEndChestBlock(Player p)
    {
        try
        {
            Object nmsP = p.getClass().getMethod("getHandle").invoke(p);
            Object nmsEC = nmsP.getClass().getMethod(this.fl_3).invoke(nmsP);
            Field field = nmsEC.getClass().getDeclaredFields()[0];
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            Object ecTE = field.get(nmsEC);
            field.setAccessible(accessible);

            if (ecTE != null)
            {
                Object blockPos = ecTE.getClass().getMethod("p").invoke(ecTE);
                int x = (int)blockPos.getClass().getMethod("u").invoke(blockPos);
                int y = (int)blockPos.getClass().getMethod("v").invoke(blockPos);
                int z = (int)blockPos.getClass().getMethod("w").invoke(blockPos);
                nmsEC.getClass().getMethod("c_", this.getNMSClass("world.entity.player.EntityHuman")).invoke(nmsEC, nmsP);
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

            Class cPacketPlayOutChat = this.getNMSClass("network.protocol.game.PacketPlayOutChat");
            Class cIChatBase = this.getNMSClass("network.chat.IChatBaseComponent");
            Class cChatSerializer = cIChatBase.getDeclaredClasses()[0];
            Object chatBaseComp = cChatSerializer.getDeclaredMethod("a", String.class).invoke(null, "{\"text\": \"" + s + "\"}");
            Object packet1 = cPacketPlayOutChat.getConstructor(cIChatBase, this.getNMSClass("network.chat.ChatMessageType"), UUID.class).newInstance(chatBaseComp, this.getNMSClass("network.chat.ChatMessageType").getField("c").get(null), this.getNMSClass("SystemUtils").getField("b").get(null));
            this.sendPacket(player, packet1);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
