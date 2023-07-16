package me.rosillogames.eggwars.loaders;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.Sets;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;

public class ArenaLoader
{
    public static final char[] ILLEGAL_FILE_CHARACTERS = new char[] {'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};
    private static final Pattern COPY_COUNTER_PATTERN = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
    private static TranslatableItem leaveItem;
    private Set<Arena> arenas = Sets.<Arena>newHashSet();

    public ArenaLoader()
    {
    }

    public static void loadConfig()
    {
        leaveItem = TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.leave.item"), Material.RED_BED)), "gameplay.leave.item_lore", "gameplay.leave.item_name");
    }

    public static ItemStack getLeaveItem(Player player)
    {
        return leaveItem.getTranslated(player);
    }

    public static Arena loadArena(File arenaFolderIn)
    {
        return new Arena(arenaFolderIn, null);
    }

    public static String getValidArenaID(String name)
    {//TODO
        for (char c0 : ILLEGAL_FILE_CHARACTERS)
        {
            name = name.replace(c0, '_');
        }

        name = name.replaceAll("[./\"]", "_");

        Matcher matcher = COPY_COUNTER_PATTERN.matcher(name);
        int j = 0;

        if (matcher.matches())
        {
            name = matcher.group("name");
            j = Integer.parseInt(matcher.group("count"));
        }

        if (name.length() > 255)
        {
            name = name.substring(0, 255);
        }

        if (j != 0)
        {
            String s1 = " (" + j + ")";
            int i = 255 - s1.length();

            if (name.length() > i)
            {
                name = name.substring(0, i);
            }

            name = name + s1;
        }

        return name;
    }

    public Set<Arena> getArenas()
    {
        return new HashSet(this.arenas);
    }

    public boolean addArena(Arena arena)
    {
        return this.arenas.add(arena);
    }

    public boolean removeArena(Arena arena)
    {
        return this.arenas.remove(arena);
    }

    public Arena getArenaByName(String s)
    {
        for (Arena arena : this.getArenas())
        {
            if (arena.getName().equalsIgnoreCase(s))
            {
                return arena;
            }
        }

        return null;
    }

    public Arena getArenaByWorld(World world)
    {
        for (Arena arena : this.getArenas())
        {
            if (arena.getWorld() == world)
            {
                return arena;
            }
        }

        return null;
    }
}
