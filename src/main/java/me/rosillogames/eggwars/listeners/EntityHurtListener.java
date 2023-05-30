package me.rosillogames.eggwars.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

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
    public void cancelDamageByPlayer(EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof Player))
        {
            return;
        }

        EwPlayer ewVictim = PlayerUtils.getEwPlayer((Player)event.getEntity());

        if (ewVictim == null || !ewVictim.isInArena())
        {
            return;
        }

        Entity damager = event.getDamager();
        EwPlayer ewDamager = null;

        if (damager instanceof Projectile && (((Projectile)damager).getShooter() instanceof Player))
        {
            ewDamager = PlayerUtils.getEwPlayer((Player)((Projectile)damager).getShooter());
        }

        if (damager instanceof TNTPrimed && (((TNTPrimed)damager).getSource() instanceof Player))
        {
            ewDamager = PlayerUtils.getEwPlayer((Player)((TNTPrimed)damager).getSource());
        }

        if (damager instanceof Player)
        {
            ewDamager = PlayerUtils.getEwPlayer((Player)damager);
        }

        if (ewDamager != null)
        {
            if (!ewDamager.isInArena() || !ewDamager.getArena().equals(ewVictim.getArena()) || ewDamager.getTeam() == null || ewVictim.getTeam() == null)
            {
                event.setCancelled(true);
                return;
            }

            if (ewVictim.getTeam().equals(ewDamager.getTeam()))
            {
                event.setCancelled(true);
                return;
            }
            else
            {
                ewVictim.setLastDamager(ewDamager);
                ewDamager.clearInvincible();
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
    public void cancelFriendlyDamage(EntityDamageByEntityEvent event)
    {
        if (event.getEntity().getType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.FIREWORK)
        {
            EwPlayer pl = PlayerUtils.getEwPlayer((Player)event.getEntity());

            if (!pl.isInArena())
            {
                return;
            }

            event.setCancelled(true);
            return;
        }

        return;
    }
}
