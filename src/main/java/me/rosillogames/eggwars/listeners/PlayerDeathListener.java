package me.rosillogames.eggwars.listeners;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.arena.game.Finish;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.Cooldown;
import me.rosillogames.eggwars.objects.Kit;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class PlayerDeathListener implements Listener
{
    @EventHandler
    public void death(PlayerDeathEvent deathevent)
    {
        final EwPlayer diedPlayer = PlayerUtils.getEwPlayer(deathevent.getEntity());

        if (!diedPlayer.isInArena() || diedPlayer.getTeam() == null)
        {
            return;
        }

        String cause = EggWars.languageManager().getDeathMsgKey(diedPlayer.getPlayer().getLastDamageCause().getCause());
        Arena arena = diedPlayer.getArena();

        if (diedPlayer.getLastDamager() != null)
        {
            boolean fk = !diedPlayer.getTeam().canRespawn();
            EwPlayer killerPlayer = diedPlayer.getLastDamager();
            killerPlayer.getIngameStats().addStat(StatType.KILLS, 1);

            if (fk)
            {
                killerPlayer.getIngameStats().addStat(StatType.ELIMINATIONS, 1);
            }

            arena.sendBroadcast("gameplay.death." + cause + ".player", TeamUtils.colorizePlayerName(diedPlayer), TeamUtils.colorizePlayerName(killerPlayer.getPlayer(), diedPlayer.getLastDamagerTeam()));
            //Reward points message for killer comes before elimination message
            PlayerUtils.addPoints(killerPlayer, fk ? EggWars.instance.getConfig().getInt("game.points.on_final_kill") : EggWars.instance.getConfig().getInt("game.points.on_kill"));
        }
        else
        {
            arena.sendBroadcast("gameplay.death." + cause, TeamUtils.colorizePlayerName(diedPlayer));
        }

        diedPlayer.getIngameStats().addStat(StatType.DEATHS, 1);
        diedPlayer.clearLastDamager();

        if (!EggWars.config.dropInv)
        {
            deathevent.getDrops().clear();
        }

        deathevent.setKeepInventory(true);

        if (!EggWars.config.keepInv)
        {
            diedPlayer.getPlayer().getInventory().clear();
        }

        deathevent.setDeathMessage(null);

        if (!diedPlayer.getTeam().canRespawn())
        {
            diedPlayer.setEliminated(true);
            arena.getScores().updateScores(false);
            arena.sendBroadcast("gameplay.ingame.player_eliminated", diedPlayer.getPlayer().getDisplayName());
            Team diedTeam = diedPlayer.getTeam();

            if (diedTeam.isEliminated() && arena.getMode().isTeam())
            {
                diedTeam.broadcastEliminated();
            }

            Finish.sendFinishStats(diedPlayer);
            Team team = arena.getWinner();

            if (team != null)
            {
                Finish.finish(arena, team);
            }
        }
        else
        {
            arena.getScores().updateScores(false);
        }

        (new BukkitRunnable()
        {
            public void run()
            {//it has to be executed later due to a bug
                diedPlayer.getPlayer().spigot().respawn();
            }
        }).runTaskLater(EggWars.instance, 0L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void respawn(PlayerRespawnEvent event)
    {
        EwPlayer player = PlayerUtils.getEwPlayer(event.getPlayer());

        if (!player.isInArena())
        {
            return;
        }

        event.setRespawnLocation(EggWars.config.respawnDelay < 1 && !player.isEliminated() ? Locations.toMiddle(player.getTeam().getRespawn()) : player.getArena().getCenter());

        (new BukkitRunnable()
        {
            public void run()
            {//it has to be executed later due to a bug, too
                prepareRespawn(player);
            }
        }).runTaskLater(EggWars.instance, 0L);
    }

    private static void prepareRespawn(EwPlayer pl)
    {
        if (pl.isEliminated())
        {
            if (EggWars.config.canSpectStay)
            {
                ReflectionUtils.sendTitle(pl.getPlayer(), 20, 40, 20, TranslationUtils.getMessage("gameplay.ingame.you_died", pl.getPlayer()), TranslationUtils.getMessage("gameplay.ingame.you_died_exit", pl.getPlayer()));
                pl.getPlayer().setGameMode(GameMode.SPECTATOR);
            }
            else
            {
                pl.getArena().leaveArena(pl, true, false);
            }
        }
        else if (EggWars.config.respawnDelay > 0)
        {
            pl.getPlayer().setGameMode(GameMode.SPECTATOR);
            (new BukkitRunnable()
            {
                private int countDown = EggWars.config.respawnDelay + 1;

                public void run()
                {
                    if (!pl.getPlayer().isOnline() || !pl.isInArena())
                    {
                        this.cancel();
                        return;
                    }

                    this.countDown--;

                    if (this.countDown > 0)
                    {
                        ReflectionUtils.sendTitle(pl.getPlayer(), 0, 22, 5, TranslationUtils.getMessage("gameplay.ingame.you_died", pl.getPlayer()), TranslationUtils.getMessage("gameplay.ingame.you_died_respawning", pl.getPlayer(), TranslationUtils.translateTime(pl.getPlayer(), this.countDown, false)));
                    }
                    else
                    {
                        pl.getPlayer().setGameMode(GameMode.SURVIVAL);
                        pl.getPlayer().teleport(Locations.toMiddle(pl.getTeam().getRespawn()));
                        performRespawn(pl);
                        this.cancel();
                    }
                }
            }).runTaskTimer(EggWars.instance, 0L, 20L);
        }
        else
        {
            performRespawn(pl);
        }
    }

    private static void performRespawn(EwPlayer pl)
    {
        ReflectionUtils.sendTitle(pl.getPlayer(), 0, 40, 5, TranslationUtils.getMessage("gameplay.ingame.respawning", pl.getPlayer()), "");
        TranslationUtils.sendMessage("gameplay.ingame.respawned_by_egg", pl.getPlayer());

        if (EggWars.config.invincibleTime > 0)
        {
            pl.setInvincible();
            TranslationUtils.sendMessage("gameplay.ingame.invincible", pl.getPlayer(), TranslationUtils.translateTime(pl.getPlayer(), EggWars.config.invincibleTime, true));
        }

        Kit plKit = pl.getKit();

        if (plKit != null && plKit.cooldownTime() >= 0)
        {
            Cooldown cooldown = pl.getKitCooldown();

            if (cooldown.hasFinished())
            {
                plKit.equip(pl.getPlayer());
                int time = plKit.cooldownTime();

                if (time > 0)
                {
                    cooldown.setFinish(time);
                    TranslationUtils.sendMessage("gameplay.kits.cooldown_started", pl.getPlayer(), TranslationUtils.translateTime(pl.getPlayer(), cooldown.timeUntilFinish(), true));
                }
            }
            else
            {
                TranslationUtils.sendMessage("gameplay.kits.on_cooldown", pl.getPlayer(), TranslationUtils.translateTime(pl.getPlayer(), cooldown.timeUntilFinish(), true));
            }
        }

        pl.getArena().setPlayerMaxHealth(pl);
    }
}
