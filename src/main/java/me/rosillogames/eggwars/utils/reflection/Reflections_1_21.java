package me.rosillogames.eggwars.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;

public class Reflections_1_21 implements Reflections
{
    private final boolean isNewVersion;
    private final boolean isEvenNewer;
    private final boolean fixPaperNames;

    public Reflections_1_21(byte v)
    {
        this.isNewVersion = v > 0;
        this.isEvenNewer = v > 1;
        this.fixPaperNames = v == 2 && EggWars.fixPaperOBC;
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
            Class cHoldLookA = this.getNMSClass("core.HolderLookup").getDeclaredClasses()[this.fixPaperNames ? 1 : 0];
            Object holdLookA = this.getNMSClass("server.MinecraftServer").getMethod("getDefaultRegistryAccess").invoke(null);
            Class cItemStack = this.getNMSClass("world.item.ItemStack");
            Object itemNbt = this.getNMSClass("nbt.MojangsonParser").getMethod("a", String.class).invoke(null, json.toString());
            Class cNBTBase = this.getNMSClass("nbt.NBTBase");
            Optional stack = (Optional)cItemStack.getMethod(this.fixPaperNames ? "parse" : "a", cHoldLookA, cNBTBase).invoke(null, holdLookA, itemNbt);

            if (stack.isPresent())
            {
                Class cCraftItemStack = this.getOBCClass("inventory.CraftItemStack");
                return (ItemStack)cCraftItemStack.getMethod("asBukkitCopy", cItemStack).invoke((Object)null, stack.get());
            }
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
            Class cHolderGetter = this.getNMSClass("core.HolderGetter");
            Class cBIR = this.getNMSClass("core.registries.BuiltInRegistries");
            Object registry = cBIR.getField("e").get((Object)null);
            Object blockData;

            if (this.isNewVersion)
            {
                Class cRegistryBlocks = this.getNMSClass("core.RegistryBlocks");
                blockData = cGameProfileSerializer.getMethod("a", cHolderGetter, cNBTCompound).invoke(null, cRegistryBlocks.cast(registry), blockNbt);
            }
            else
            {
                Object holdLook = registry.getClass().getMethod("q").invoke(registry);
                blockData = cGameProfileSerializer.getMethod("a", cHolderGetter, cNBTCompound).invoke(null, holdLook, blockNbt);
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
            Object nmsStack = null;

            try
            {
                nmsStack = cCraftItemStack.getMethod("asNMSCopy", stack.getClass()).invoke((Object)null, stack);
            }
            catch (NoSuchMethodException ex)
            {//FIX #20
                Field field = stack.getClass().getField("handle");
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                nmsStack = field.get(stack);
                field.setAccessible(accessible);
            }

            Object nameComponent = cItemStack.getMethod(this.isNewVersion ? "y" : "w").invoke(nmsStack);
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
    @Deprecated
    public List<String> getEnchantmentsLore(ItemStack stack)
    {//no longer used
        return new ArrayList<String>();
    }

    @Override
    public void setEnchantGlint(ItemStack stack, boolean glint, boolean force)
    {
        ItemMeta meta = stack.getItemMeta();
        Material mat = stack.getType();

        if (!force && (mat == Material.ENCHANTED_GOLDEN_APPLE || mat == Material.NETHER_STAR || mat == Material.END_CRYSTAL || mat == Material.EXPERIENCE_BOTTLE || mat == Material.ENCHANTED_BOOK || mat == Material.WRITTEN_BOOK || mat == Material.DEBUG_STICK))
        {//don't apply to potions because they disabled it)
            return;
        }

        meta.setEnchantmentGlintOverride(glint);
        stack.setItemMeta(meta);
    }

    @Override
    public void killOutOfWorld(Player p)
    {
        p.damage((double)Float.MAX_VALUE, DamageSource.builder(DamageType.OUT_OF_WORLD).build());
    }

    @Override
    public void saveFullWorld(World world)
    {
        try
        {
            Object obj = world.getClass().getMethod("getHandle").invoke(world);
            Field field = obj.getClass().getField("e");
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
            Object nmsEC = nmsP.getClass().getMethod(this.isNewVersion ? "gw" : "gl").invoke(nmsP);
            Field field = nmsEC.getClass().getDeclaredFields()[0];
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            Object ecTE = field.get(nmsEC);
            field.setAccessible(accessible);

            if (ecTE != null)
            {
                Object blockPos = ecTE.getClass().getMethod(this.isNewVersion ? (this.isEvenNewer ? "aA_" : "aB_") : "aD_").invoke(ecTE);
                int x = (int)blockPos.getClass().getMethod("u").invoke(blockPos);
                int y = (int)blockPos.getClass().getMethod("v").invoke(blockPos);
                int z = (int)blockPos.getClass().getMethod("w").invoke(blockPos);
                nmsEC.getClass().getMethod("c", this.getNMSClass("world.entity.player.EntityHuman")).invoke(nmsEC, nmsP);
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
    {//as of 1.20.2 packet sender class is ServerCommonPacketListenerImpl
        try
        {
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object nmsConnection = nmsPlayer.getClass().getField(this.isNewVersion ? "f" : "c").get(nmsPlayer);
            nmsConnection.getClass().getMethod("b", this.getNMSClass("network.protocol.Packet")).invoke(nmsConnection, packetObj);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void sendTitle(Player player, Integer fadeInTime, Integer stayTime, Integer fadeOutTime, String title, String subtitle)
    {
        player.sendTitle(title, subtitle, fadeInTime, stayTime, fadeOutTime);
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

            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacy(s));
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
