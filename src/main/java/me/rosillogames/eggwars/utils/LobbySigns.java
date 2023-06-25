package me.rosillogames.eggwars.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.FileConfiguration;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.objects.ArenaSign;

public class LobbySigns
{
    public static boolean activeSign;
    public static BlockData lobby;
    public static BlockData starting;
    public static BlockData ingame;
    public static BlockData finished;
    public static BlockData setting;

    public static void loadConfig()
    {
        FileConfiguration fileconf = EggWars.instance.getConfig();
        activeSign = fileconf.getBoolean("lobby.sign_status.active");
        lobby = ItemUtils.getBlockData(fileconf.getString("lobby.sign_status.lobby"), Material.LIME_STAINED_GLASS.createBlockData());
        starting = ItemUtils.getBlockData(fileconf.getString("lobby.sign_status.starting"), Material.YELLOW_STAINED_GLASS.createBlockData());
        ingame = ItemUtils.getBlockData(fileconf.getString("lobby.sign_status.ingame"), Material.RED_STAINED_GLASS.createBlockData());
        finished = ItemUtils.getBlockData(fileconf.getString("lobby.sign_status.finished"), Material.MAGENTA_STAINED_GLASS.createBlockData());
        setting = ItemUtils.getBlockData(fileconf.getString("lobby.sign_status.setting"), Material.CYAN_STAINED_GLASS.createBlockData());
    }

    public static void setBlock(ArenaSign sign)
    {
        if (!activeSign || !isValidWallSign(sign.getLocation().getBlock().getState()))
        {
            return;
        }

        Block block = sign.getSupport().getBlock();

        switch (sign.getArena().getStatus())
        {
            case LOBBY:
                block.setBlockData(lobby);
                break;
            case STARTING:
                block.setBlockData(starting);
                break;
            case IN_GAME:
            case STARTING_GAME:
                block.setBlockData(ingame);
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

            if (!allowSupport)
            {
                continue;
            }

            if (ewsign.getSupport().equals(location.getBlock().getLocation()))
            {
                return ewsign;
            }
        }

        return null;
    }

    public static boolean isValidWallSign(BlockState state)
    {
        return state.getBlockData() instanceof WallSign;
    }
}
