package me.rosillogames.eggwars.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Scoreboards;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.arena.game.Countdown;
import me.rosillogames.eggwars.arena.game.Finish;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.language.TranslationUtils;
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

        String cause = diedPlayer.getPlayer().getLastDamageCause().getCause().name().toLowerCase();

        if (diedPlayer.getLastDamager() != null)
        {
            boolean fk = !diedPlayer.getTeam().canRespawn();
            EwPlayer killerPlayer = diedPlayer.getLastDamager();
            killerPlayer.getIngameStats().addStat(StatType.KILLS, 1);

            if (fk)
            {
                killerPlayer.getIngameStats().addStat(StatType.ELIMINATIONS, 1);
            }

            diedPlayer.getArena().sendBroadcast("gameplay.death." + cause + ".player", TeamUtils.colorizePlayerName(diedPlayer), TeamUtils.colorizePlayerName(killerPlayer));
            //Reward points message for killer comes before elimination message
            PlayerUtils.addPoints(killerPlayer, fk ? EggWars.instance.getConfig().getInt("gameplay.points.on_final_kill") : EggWars.instance.getConfig().getInt("gameplay.points.on_kill"));
        }
        else
        {
            diedPlayer.getArena().sendBroadcast("gameplay.death." + cause, TeamUtils.colorizePlayerName(diedPlayer));
        }

        diedPlayer.getIngameStats().addStat(StatType.DEATHS, 1);
        deathevent.getDrops().clear();
        deathevent.setKeepInventory(true);

        if (!EggWars.config.keepInv)
        {
            diedPlayer.getPlayer().getInventory().clear();
            diedPlayer.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
            diedPlayer.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));
            diedPlayer.getPlayer().getInventory().setLeggings(new ItemStack(Material.AIR));
            diedPlayer.getPlayer().getInventory().setBoots(new ItemStack(Material.AIR));
            diedPlayer.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        }
        else
        {
            deathevent.setKeepInventory(true);
        }

        deathevent.setDeathMessage(null);

        if (!diedPlayer.getTeam().canRespawn())
        {
            diedPlayer.setEliminated(true);
            Team diedTeam = diedPlayer.getTeam();
            diedTeam.removePlayer(diedPlayer);
            Arena arena = diedPlayer.getArena();
            arena.sendBroadcast("gameplay.ingame.player_eliminated", diedPlayer.getPlayer().getDisplayName());
            Scoreboards.setScore(arena);

            if (diedTeam.isEliminated() && arena.getMode().isTeam())
            {
                for (EwPlayer ewplayer : diedPlayer.getArena().getPlayers())
                {
                    ewplayer.getPlayer().sendMessage("");
                    TranslationUtils.sendMessage("gameplay.ingame.team_eliminated", ewplayer.getPlayer(), TeamUtils.translateTeamType(diedTeam.getType(), ewplayer.getPlayer(), false));
                    ewplayer.getPlayer().sendMessage("");
                    ewplayer.getPlayer().playSound(ewplayer.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 0.0F);
                }
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
                Countdown countdown = new Countdown(EggWars.config.respawnDelay);
                (new BukkitRunnable()
                {
                    public void run()
                    {
                        if (!pl.getPlayer().isOnline() || !pl.isInArena())
                        {
                            this.cancel();
                            return;
                        }

                        countdown.decrease();

                        switch (countdown.getCountdown())
                        {
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 10:
                            case 15:
                                ReflectionUtils.sendTitle(pl.getPlayer(), Integer.valueOf(0), Integer.valueOf(22), Integer.valueOf(0), TranslationUtils.getMessage("gameplay.ingame.you_died", pl.getPlayer()), TranslationUtils.getMessage("gameplay.ingame.you_died_respawning", pl.getPlayer(), TranslationUtils.translateTime(pl.getPlayer(), countdown.getCountdown(), false)));
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

        if (EggWars.config.invincibilityTime > 0)
        {
            pl.setInvincible();
            TranslationUtils.sendMessage("gameplay.ingame.invincible", pl.getPlayer(), TranslationUtils.translateTime(pl.getPlayer(), EggWars.config.invincibilityTime, true));
        }

        if (pl.getKit() != null && pl.getKit().cooldownTime() >= 0)
        {
            if (pl.timeUntilKit() <= 0)
            {
                pl.getKit().equip(pl.getPlayer());
                int cooldown = pl.getKit().cooldownTime();

                if (cooldown > 0)
                {
                    pl.startKitCooldown(cooldown);
                }
            }
            else
            {
                TranslationUtils.sendMessage("gameplay.kits.on_cooldown", pl.getPlayer(), TranslationUtils.translateTime(pl.getPlayer(), pl.timeUntilKit(), true));
            }
        }

        pl.getArena().setPlayerMaxHealth(pl);
    }
}
