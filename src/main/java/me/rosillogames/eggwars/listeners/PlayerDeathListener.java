package me.rosillogames.eggwars.listeners;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.arena.game.Finish;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.AttackInstance;
import me.rosillogames.eggwars.objects.Cooldown;
import me.rosillogames.eggwars.objects.Kit;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.Pair;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

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

        DamageCause cause = diedPlayer.getPlayer().getLastDamageCause() != null ? diedPlayer.getPlayer().getLastDamageCause().getCause() : DamageCause.CUSTOM;
        Arena arena = diedPlayer.getArena();

        if (diedPlayer.getLastDamager() != null)
        {
            boolean fk = !diedPlayer.getTeam().canRespawn();
            EwPlayer killerPlayer = diedPlayer.getLastDamager();
            TeamType killerTeam = diedPlayer.getLastDamagerTeam();
            float killerDmg = 0.0F;
            float totalDmg = 0.0F;
            Map<EwPlayer, Pair<TeamType, Float>> assists = new HashMap();

            //TODO: Cleanup when replacing EwPlayer with ArenaPlayer
            if (EggWars.config.enableAssists.applies(arena.getMode()))
            {
                for (AttackInstance atck : diedPlayer.getAssists())
                {
                    float damage;
                    totalDmg += (damage = atck.getDamage());
                    EwPlayer attacker = atck.getAttacker();
                    assists.put(attacker, new Pair(atck.getTeamColor(), (assists.containsKey(attacker) ? assists.get(attacker).getRight() : 0.0F) + damage));
                }

                if (assists.size() >= 2)
                {
                    if (EggWars.config.bestAssistIsKiller)
                    {
                        float highestDamage = 0.0F;

                        for (Map.Entry<EwPlayer, Pair<TeamType, Float>> entry : assists.entrySet())
                        {
                            if (entry.getValue().getRight() >= highestDamage)
                            {
                                killerPlayer = entry.getKey();
                                killerTeam = entry.getValue().getLeft();
                                highestDamage = entry.getValue().getRight();
                            }
                        }
                    }

                    killerDmg = assists.remove(killerPlayer).getRight();
                }
            }

            killerPlayer.getIngameStats().addStat(StatType.KILLS, 1);

            if (fk)
            {
                killerPlayer.getIngameStats().addStat(StatType.ELIMINATIONS, 1);
            }

            for (EwPlayer ewpl : arena.getPlayers())
            {
                TextComponent txt = new TextComponent("");

                if (assists.size() >= 2)
                {
                    Player player = ewpl.getPlayer();
                    txt = new TextComponent(TranslationUtils.getMessage(assists.size() == 1 ? "gameplay.one_assist" : "gameplay.assists", player, assists.size()));
                    StringBuilder contents = new StringBuilder("");

                    for (Map.Entry<EwPlayer, Pair<TeamType, Float>> entry : assists.entrySet())
                    {
                        contents.append(TranslationUtils.getMessage("assists.entry", player, TeamUtils.colorizePlayerName(entry.getKey().getPlayer(), entry.getValue().getLeft()), String.format("%.1f", (entry.getValue().getRight() * 100.0F) / totalDmg)));
                    }

                    txt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TranslationUtils.getMessage("assists.container", player, TeamUtils.colorizePlayerName(killerPlayer.getPlayer(), killerTeam), String.format("%.1f", (killerDmg * 100.0F) / totalDmg), contents.toString()))));
                }

                ewpl.getPlayer().spigot().sendMessage(new TextComponent(new TextComponent(TranslationUtils.getMessage("gameplay.death." + EggWars.languageManager().getDeathMsgKey(cause) + ".player", ewpl.getPlayer(), TeamUtils.colorizePlayerName(diedPlayer), TeamUtils.colorizePlayerName(killerPlayer.getPlayer(), killerTeam))), txt));
            }

            //Reward points message for killer comes before elimination message
            PlayerUtils.addPoints(killerPlayer, fk ? EggWars.instance.getConfig().getInt("game.points.on_final_kill") : EggWars.instance.getConfig().getInt("game.points.on_kill"));
        }
        else
        {
            arena.sendBroadcast("gameplay.death." + EggWars.languageManager().getDeathMsgKey(cause), TeamUtils.colorizePlayerName(diedPlayer));
        }

        diedPlayer.getIngameStats().addStat(StatType.DEATHS, 1);
        diedPlayer.clearLastDamager();
        diedPlayer.clearAssists();

        if (!EggWars.config.dropInv || cause == DamageCause.VOID)
        {
            deathevent.getDrops().clear();
            deathevent.setDroppedExp(0);//You can't normally pick up experience in the game, but still
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
