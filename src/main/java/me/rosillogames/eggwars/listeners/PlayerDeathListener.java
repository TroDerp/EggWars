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
        arena.getScores().updateScores(false);

        if (!diedPlayer.getTeam().canRespawn())
        {
            diedPlayer.setEliminated(true);
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

        (new BukkitRunnable()
        {
            public void run()
            {
                diedPlayer.getPlayer().spigot().respawn();
            }

        }).runTaskLater(EggWars.instance, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void respawn(PlayerRespawnEvent event)
    {
        final EwPlayer pl = PlayerUtils.getEwPlayer(event.getPlayer());

        if (!pl.isInArena())
        {
            return;
        }

        if (pl.isEliminated())
        {
            event.setRespawnLocation(pl.getArena().getCenter());

            if (EggWars.config.canSpectStay)
            {
                ReflectionUtils.sendTitle(pl.getPlayer(), Integer.valueOf(20), Integer.valueOf(40), Integer.valueOf(20), TranslationUtils.getMessage("gameplay.ingame.you_died", pl.getPlayer()), TranslationUtils.getMessage("gameplay.ingame.you_died_exit", pl.getPlayer()));
                pl.getPlayer().setGameMode(GameMode.SPECTATOR);
            }
            else
            {
                pl.getArena().leaveArena(pl, true, false);
            }
        }
        else
        {
            if (EggWars.config.respawnDelay < 1)
            {
                event.setRespawnLocation(Locations.toMiddle(pl.getTeam().getRespawn()));
                performRespawn(pl);
            }
            else
            {
                event.setRespawnLocation(pl.getArena().getCenter());
                pl.getPlayer().setGameMode(GameMode.SPECTATOR);
                ReflectionUtils.sendTitle(pl.getPlayer(), Integer.valueOf(5), Integer.valueOf(22), Integer.valueOf(5), TranslationUtils.getMessage("gameplay.ingame.you_died", pl.getPlayer()), TranslationUtils.getMessage("gameplay.ingame.you_died_respawning", pl.getPlayer(), TranslationUtils.translateTime(pl.getPlayer(), EggWars.config.respawnDelay, false)));
                (new BukkitRunnable()
                {
                    private int countDown = EggWars.config.respawnDelay;

                    public void run()
                    {
                        if (!pl.getPlayer().isOnline() || !pl.isInArena())
                        {
                            this.cancel();
                            return;
                        }

                        this.countDown--;

                        switch (this.countDown)
                        {
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 10:
                            case 15:
                                ReflectionUtils.sendTitle(pl.getPlayer(), Integer.valueOf(0), Integer.valueOf(22), Integer.valueOf(0), TranslationUtils.getMessage("gameplay.ingame.you_died", pl.getPlayer()), TranslationUtils.getMessage("gameplay.ingame.you_died_respawning", pl.getPlayer(), TranslationUtils.translateTime(pl.getPlayer(), this.countDown, false)));
                                break;
                            case 0:
                                pl.getPlayer().setGameMode(GameMode.SURVIVAL);
                                pl.getPlayer().teleport(Locations.toMiddle(pl.getTeam().getRespawn()));
                                performRespawn(pl);
                                this.cancel();
                                return;
                        }
                    }
                }).runTaskTimer(EggWars.instance, 20L, 20L);
            }
        }
    }

    private static void performRespawn(EwPlayer pl)
    {
        ReflectionUtils.sendTitle(pl.getPlayer(), Integer.valueOf(0), Integer.valueOf(40), Integer.valueOf(5), TranslationUtils.getMessage("gameplay.ingame.respawning", pl.getPlayer()), "");
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
