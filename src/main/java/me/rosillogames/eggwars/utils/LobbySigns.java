package me.rosillogames.eggwars.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.objects.ArenaSign;

public class LobbySigns
{
    public static boolean activeSign;
    public static BlockData waiting;
    public static BlockData starting;
    public static BlockData in_game;
    public static BlockData finished;
    public static BlockData setting;

    public static void loadConfig()
    {
        FileConfiguration fileconf = EggWars.instance.getConfig();
        activeSign = fileconf.getBoolean("lobby.sign_status.active");
        waiting = ItemUtils.getBlockData(fileconf.getString("lobby.sign_status.waiting"), Material.LIME_STAINED_GLASS.createBlockData());
        starting = ItemUtils.getBlockData(fileconf.getString("lobby.sign_status.starting"), Material.YELLOW_STAINED_GLASS.createBlockData());
        in_game = ItemUtils.getBlockData(fileconf.getString("lobby.sign_status.in_game"), Material.RED_STAINED_GLASS.createBlockData());
        finished = ItemUtils.getBlockData(fileconf.getString("lobby.sign_status.finishing"), Material.ORANGE_STAINED_GLASS.createBlockData());
        setting = ItemUtils.getBlockData(fileconf.getString("lobby.sign_status.setting"), Material.CYAN_STAINED_GLASS.createBlockData());
    }

    public static void setBlock(ArenaSign sign)
    {
        if (!activeSign)
        {
            return;
        }

        Block block = sign.getSupport().getBlock();

        switch (sign.getArena().getStatus())
        {
            case WAITING:
                block.setBlockData(waiting);
                break;
            case STARTING:
                block.setBlockData(starting);
                break;
            case STARTING_GAME:
            case IN_GAME:
                block.setBlockData(in_game);
                break;
            case FINISHING:
                block.setBlockData(finished);
                break;
            case SETTING:
                block.setBlockData(setting);
        }
    }

    public static ArenaSign getSignByLocation(Location location, boolean allowSupport)
    {
        for (ArenaSign ewsign : EggWars.signs)
        {
            if (location.getBlock().getLocation().equals(ewsign.getLocation().getBlock().getLocation()))
            {
                return ewsign;
            }

            if (allowSupport && ewsign.getSupport().equals(location.getBlock().getLocation()))
            {
                return ewsign;
            }
        }

        return null;
    }

    /** Checks if this is a valid block to place an arena sign (IMPORTANT! it also makes sure
     * the place is not already used by another arena-joining sign) **/
    public static boolean isValidBlockSign(Block block)
    {
        return block.getState() instanceof Sign && LobbySigns.getSignByLocation(block.getLocation(), false) == null;
    }
}
