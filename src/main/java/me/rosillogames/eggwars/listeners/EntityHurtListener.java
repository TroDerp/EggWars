package me.rosillogames.eggwars.listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

import org.bukkit.event.entity.FoodLevelChangeEvent;

public class EntityHurtListener implements Listener
{
    @EventHandler
    public void cancelHurtOutsideGame(EntityDamageEvent damageEvent)
    {
        if (!(damageEvent.getEntity() instanceof Player))
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)damageEvent.getEntity());

        if (ewplayer == null)
        {
            return;
        }

        if (ewplayer.isInArena())
        {
            if (!ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME))
            {
                damageEvent.setCancelled(true);
                return;
            }

            if (ewplayer.isInvincible())
            {
                damageEvent.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void cancelDamageByPlayer(EntityDamageByEntityEvent damageByEntityEvent)
    {
        if (!(damageByEntityEvent.getEntity() instanceof Player))
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)damageByEntityEvent.getEntity());

        if (ewplayer == null)
        {
            return;
        }

        if (!ewplayer.isInArena())
        {
            return;
        }

        if ((damageByEntityEvent.getDamager() instanceof Arrow) && (((Arrow)damageByEntityEvent.getDamager()).getShooter() instanceof Player))
        {
            EwPlayer ewplayer1 = PlayerUtils.getEwPlayer((Player)((Arrow)damageByEntityEvent.getDamager()).getShooter());

            if (!ewplayer1.isInArena() || !ewplayer1.getArena().equals(ewplayer.getArena()) || ewplayer1.isEliminated() || ewplayer1.getTeam() == null || ewplayer.getTeam() == null)
            {
                damageByEntityEvent.setCancelled(true);
                return;
            }

            if (ewplayer.getTeam().equals(ewplayer1.getTeam()))
            {
                damageByEntityEvent.setCancelled(true);
                return;
            }
            else
            {
                ewplayer.setLastDamager(ewplayer1);
                return;
            }
        }

        if (damageByEntityEvent.getDamager() instanceof Player)
        {
            EwPlayer ewplayer2 = PlayerUtils.getEwPlayer((Player)damageByEntityEvent.getDamager());

            if (!ewplayer2.isInArena() || !ewplayer2.getArena().equals(ewplayer.getArena()) || ewplayer2.isEliminated() || ewplayer2.getTeam() == null || ewplayer.getTeam() == null)
            {
                damageByEntityEvent.setCancelled(true);
                return;
            }

            if (ewplayer.getTeam().equals(ewplayer2.getTeam()))
            {
                damageByEntityEvent.setCancelled(true);
                return;
            }
            else
            {
                ewplayer.setLastDamager(ewplayer2);
                ewplayer2.clearInvincible();
                return;
            }
        }
        else
        {
            return;
        }
    }

    @EventHandler
    public void villagerImmunity(EntityDamageEvent entitydamageevent)
    {
        if (!(entitydamageevent.getEntity() instanceof Villager))
        {
            return;
        }

        try
        {
            Arena arena = EggWars.getArenaManager().getArenaByWorld(entitydamageevent.getEntity().getWorld());

            if (arena == null || arena.getStatus().equals(ArenaStatus.SETTING))
            {
                return;
            }

            entitydamageevent.setCancelled(true);
        }
        catch (Exception exception)
        {
        }
    }

    @EventHandler
    public void cancelFoodDecreaseOutsideGame(FoodLevelChangeEvent foodlevelchangeevent)
    {
        if (!(foodlevelchangeevent.getEntity() instanceof Player))
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)foodlevelchangeevent.getEntity());

        if (ewplayer == null)
        {
            return;
        }

        if (ewplayer.isInArena() && !ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME))
        {
            foodlevelchangeevent.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void cancelFriendlyDamage(EntityDamageByEntityEvent entityDamageByEntityEvent)
    {
        if (entityDamageByEntityEvent.getEntity().getType() == EntityType.PLAYER && entityDamageByEntityEvent.getDamager().getType() == EntityType.FIREWORK)
        {
            EwPlayer pl = PlayerUtils.getEwPlayer((Player)entityDamageByEntityEvent.getEntity());

            if (!pl.isInArena())
            {
                return;
            }

            entityDamageByEntityEvent.setCancelled(true);
            return;
        }

        if (entityDamageByEntityEvent.getDamager().getType() == EntityType.PRIMED_TNT && entityDamageByEntityEvent.getCause() == DamageCause.ENTITY_EXPLOSION)
        {
            if (!(entityDamageByEntityEvent.getEntity() instanceof Player))
            {
                return;
            }

            EwPlayer pl = PlayerUtils.getEwPlayer((Player)entityDamageByEntityEvent.getEntity());

            if (!pl.isInArena())
            {
                return;
            }

            if (((TNTPrimed)entityDamageByEntityEvent.getDamager()).getSource() instanceof Player)
            {
                EwPlayer pl1 = PlayerUtils.getEwPlayer((Player)((TNTPrimed)entityDamageByEntityEvent.getDamager()).getSource());

                if (!pl1.isInArena())
                {
                    return;
                }

                if (pl.getTeam() == pl1.getTeam())
                {
                    entityDamageByEntityEvent.setCancelled(true);
                }

                return;
            }
        }

        return;
    }
}
