package me.rosillogames.eggwars.listeners;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class TickClock
{
    private static BukkitTask compassTask = null;

    public static void start()
    {
        if (compassTask == null)
        {
            compassTask = (new BukkitRunnable()
            {
                public void run()
                {
                    for (Arena arena : EggWars.getArenaManager().getArenas())
                    {
                        if (arena.getStatus().equals(ArenaStatus.IN_GAME))
                        {
                            for (EwPlayer player : arena.getPlayers())
                            {
                                PlayerUtils.setCompassTarget(player, false);

                                if (!player.isEliminated())
                                {
                                    player.getIngameStats().addStat(StatType.TIME_PLAYED, 1);
                                }
                            }
                        }
                    }
                }
            }).runTaskTimer(EggWars.instance, 0L, 20L);
        }
    }
}
