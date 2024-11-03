package me.rosillogames.eggwars.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import com.google.gson.JsonObject;

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
        if (age == -32768)
        {
            item.setUnlimitedLifetime(true);
        }
        else
        {
            item.setUnlimitedLifetime(false);
            item.setTicksLived(age);
        }
    }

    @Override
    public ItemStack parseItemStack(JsonObject json)
    {
        try
        {
            Class cMojangsonParser = this.getNMSClass("nbt.MojangsonParser");
            Object itemNbt = cMojangsonParser.getMethod("a", String.class).invoke(null, json.toString());
            Class cItemStack = this.getNMSClass("world.item.ItemStack");
            Class cNBTCompound = this.getNMSClass("nbt.NBTTagCompound");
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
            Class cMojangsonParser = this.getNMSClass("nbt.MojangsonParser");
            Object blockNbt = cMojangsonParser.getMethod("a", String.class).invoke(null, string);
            Class cNBTCompound = this.getNMSClass("nbt.NBTTagCompound");
            Class cGameProfileSerializer = this.getNMSClass("nbt.GameProfileSerializer");
            Object blockData;

            if (this.version > 0)
            {
                //Now I'm starting to HATE that HolderLookup thing
                //BuiltInRegistries.BLOCK.asLookup(); = BuiltInRegistries.f.p();
                Class cHolderGetter = this.getNMSClass("core.HolderGetter");
                Class cBIR = this.getNMSClass("core.registries.BuiltInRegistries");
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

    @Nullable
    @Override
    public Block getEndChestBlock(Player p)
    {
        try
        {
            Object nmsP = p.getClass().getMethod("getHandle").invoke(p);
            Object nmsEC = nmsP.getClass().getMethod(this.version < 1 ? "fO" : this.version < 2 ? "fR" : "fW").invoke(nmsP);
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
                nmsEC.getClass().getMethod(this.version < 1 ? "c_" : this.version < 2 ? "d_" : "c", this.getNMSClass("world.entity.player.EntityHuman")).invoke(nmsEC, nmsP);
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

            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(s));
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
