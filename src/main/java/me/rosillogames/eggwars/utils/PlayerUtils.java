package me.rosillogames.eggwars.utils;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PlayerUtils
{
    @Nullable
    public static EwPlayer getEwPlayer(Player player)
    {
        if (player == null)
        {
            return null;
        }

        for (EwPlayer ewplayer : EggWars.players)
        {
            if (ewplayer.getPlayer().getName().equals(player.getName()) || ewplayer.getPlayer().getUniqueId().equals(player.getUniqueId()))
            {
                return ewplayer;
            }
            else if (ewplayer.getPlayer() == player)
            {
                return ewplayer;
            }
        }

        //Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Couldn't find EwPlayer for player \"" + player.getName() + "\", creating new one.");
        //EwPlayer newPl = new EwPlayer(player);
        //EggWars.players.add(newPl);
        return null;//newPl;
    }

    @Nullable
    public static EwPlayer getNearestPlayer(Location location, double d0, Predicate<EwPlayer> predicate)
    {
        EwPlayer ewplayer = null;
        double d1 = d0;

        for (Player player : location.getWorld().getPlayers())
        {
            EwPlayer ewplayer1;

            if ((d1 == -1 || player.getLocation().distance(location) < d1) && predicate.test(ewplayer1 = PlayerUtils.getEwPlayer(player)))
            {
                d1 = player.getLocation().distance(location);
                ewplayer = ewplayer1;
            }
        }

        return ewplayer;
    }

    public static int getNearbyPlayerAmount(Location location, double d, Arena arena)
    {
        int count = 0;
        int i = (int)d >= 16 ? ((int)d - (int)d % 16) / 16 : 1;

        for (int j = 0 - i; j <= i; j++)
        {
            for (int k = 0 - i; k <= i; k++)
            {
                int l = (int)location.getX();
                int i1 = (int)location.getY();
                int j1 = (int)location.getZ();
                Entity aentity[] = (new Location(location.getWorld(), l + j * 16, i1, j1 + k * 16)).getChunk().getEntities();
                int k1 = aentity.length;

                for (int l1 = 0; l1 < k1; l1++)
                {
                    Entity entity = aentity[l1];

                    if (entity.getLocation().distance(location) > d || !(entity instanceof Player) || getEwPlayer((Player)entity) == null)
                    {
                        continue;
                    }

                    EwPlayer ewplayer = getEwPlayer((Player)entity);

                    if (ewplayer.isInArena() && ewplayer.getArena() == arena && !ewplayer.isEliminated())
                    {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    /** Adds the specified amount of points to the player and sends a message **/
    public static void addPoints(EwPlayer ewplayer, int i)
    {
        if (i <= 0)
        {
            return;
        }

        float mul = EggWars.config.pointMultiplier;
        int j = i;
        String m = "";

        if (ewplayer.getPlayer().hasPermission("eggwars.multpoints") && mul != 1.0F)
        {
            j = (int)((float)i * mul);
            m = TranslationUtils.getMessage("gameplay.misc.points_multiplied", ewplayer.getPlayer(), Float.valueOf(mul));
        }

        ewplayer.setPoints(ewplayer.getPoints() + j);
        TranslationUtils.sendMessage("gameplay.misc.add_points", ewplayer.getPlayer(), Integer.valueOf(j), m);
    }

    public static void tpToLobby(Player player, boolean sendBungee)
    {
        if (player == null || player == null || !player.isOnline())
        {
            return;
        }

        if (EggWars.bungee.isEnabled())
        {
            if (sendBungee)
            {
                sendBungeeLobby(player);
            }

            return;
        }

        player.setFallDistance(0.0F);

        if (EggWars.config.lobby == null || EggWars.config.lobby.getWorld() == null)
        {
            player.teleport(new Location((World)Bukkit.getWorlds().get(0), 0.0D, 100.5D, 0.0D), PlayerTeleportEvent.TeleportCause.END_PORTAL);
        }
        else
        {
            player.teleport(EggWars.config.lobby.clone().add(0.0D, 0.5D, 0.0D), PlayerTeleportEvent.TeleportCause.END_PORTAL);
        }
    }

    public static void removePotionEffects(Player player)
    {
        for (PotionEffectType type : PotionEffectType.values())
        {
            player.removePotionEffect(type);
        }
    }

    public static void setCompassTarget(EwPlayer ewplayer, boolean updateNearby)
    {
        if (!ewplayer.isInArena() || ewplayer.getTeam() == null || ewplayer.isEliminated())
        {
            return;
        }

        if (!updateNearby && ewplayer.getTrackedPlayer() != null && ewplayer.getTrackedPlayer().isInArena() && ewplayer.getTrackedPlayer().getArena() == ewplayer.getArena() && !ewplayer.getTrackedPlayer().isEliminated())
        {
            EwPlayer ewplayer1 = ewplayer.getTrackedPlayer();
            ewplayer.getPlayer().setCompassTarget(ewplayer1.getPlayer().getLocation());

            if (ewplayer.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.COMPASS) || ewplayer.getPlayer().getInventory().getItemInOffHand().getType().equals(Material.COMPASS))
            {
                ReflectionUtils.sendActionBar(ewplayer.getPlayer(), TranslationUtils.getMessage("gameplay.ingame.compass_target", ewplayer.getPlayer(), new Object[] {TeamUtils.translateTeamType(ewplayer1.getTeam().getType(), ewplayer.getPlayer(), false), TeamUtils.colorizePlayerName(ewplayer1), String.format("%.1f", Double.valueOf(ewplayer.getPlayer().getLocation().distance(ewplayer1.getPlayer().getLocation())))}), Integer.valueOf(0), Integer.valueOf(25), Integer.valueOf(0));
            }

            return;
        }

        EwPlayer ewplayer2 = getNearestPlayer(ewplayer.getPlayer().getLocation(), -1, (foundPlayer) -> foundPlayer.isInArena() && foundPlayer.getArena() == ewplayer.getArena() && foundPlayer.getTeam() != null && foundPlayer.getTeam() != ewplayer.getTeam() && !foundPlayer.isEliminated());

        if (ewplayer2 == null)
        {
            return;
        }

        ewplayer.getPlayer().setCompassTarget(ewplayer2.getPlayer().getLocation());

        if (ewplayer.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.COMPASS) || ewplayer.getPlayer().getInventory().getItemInOffHand().getType().equals(Material.COMPASS))
        {
            ReflectionUtils.sendActionBar(ewplayer.getPlayer(), TranslationUtils.getMessage("gameplay.ingame.compass_target", ewplayer.getPlayer(), new Object[] {TeamUtils.translateTeamType(ewplayer2.getTeam().getType(), ewplayer.getPlayer(), false), TeamUtils.colorizePlayerName(ewplayer2), String.format("%.1f", Double.valueOf(ewplayer.getPlayer().getLocation().distance(ewplayer2.getPlayer().getLocation())))}), Integer.valueOf(0), Integer.valueOf(25), Integer.valueOf(0));
        }
    }

    public static void sendBungeeLobby(Player player)
    {
        if (EggWars.bungee.getLobby() != null && !EggWars.bungee.getLobby().isEmpty())
        {
            ByteArrayDataOutput bytearraydataoutput = ByteStreams.newDataOutput();
            bytearraydataoutput.writeUTF("Connect");
            bytearraydataoutput.writeUTF(EggWars.bungee.getLobby());
            player.sendPluginMessage(EggWars.instance, "BungeeCord", bytearraydataoutput.toByteArray());
        }
    }

    public static String getPrefix(Player player)
    {
        if (!Bukkit.getPluginManager().isPluginEnabled("PermissionsEx"))
        {
            return "";
        }
        else
        {
            PermissionUser permissionuser = PermissionsEx.getUser(player);
            return permissionuser.getPrefix();
        }
    }
}
