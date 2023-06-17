package me.rosillogames.eggwars.api;

import javax.annotation.Nullable;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class EggWarsAPI
{
    @Nullable
    public static EwPlayer getEggWarsPlayer(Player bukkitPlayer)
    {
        return PlayerUtils.getEwPlayer(bukkitPlayer);
    }

    public static Arena getEggWarsArena(String name)
    {
        return EggWars.getArenaManager().getArenaByName(name);
    }

    public static boolean forceStart(Arena arena)
    {
        return arena.forceStart();
    }

    public static void joinArena(Arena arena, EwPlayer player)
    {
        if (!arena.getStatus().isLobby())
        {
            arena.joinArena(player, true, true);
        }
        else
        {
            arena.joinArena(player, false, false);
        }
    }

    public static boolean leaveArena(EwPlayer player, boolean silent)
    {
        if (player.isInArena())
        {
            player.getArena().leaveArena(player, EggWars.bungee.isEnabled(), silent);
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean setVotePower(Player bukkitPlayer, float power)
    {
        EwPlayer player = PlayerUtils.getEwPlayer(bukkitPlayer);

        if (player != null)
        {
            player.setVotePower(power);
            return true;
        }

        return false;
    }
}
