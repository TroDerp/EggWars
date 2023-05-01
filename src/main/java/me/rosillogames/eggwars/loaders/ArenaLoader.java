package me.rosillogames.eggwars.loaders;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
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
