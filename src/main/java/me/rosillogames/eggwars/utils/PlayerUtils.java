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
import org.bukkit.potion.PotionEffect;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.dependencies.VaultEconomy;
import me.rosillogames.eggwars.events.PlayerChangeEwLangEvent;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.Kit;
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

        /*try
        {
            EggWars.getDB().loadPlayer(player);
            EwPlayer newPl = new EwPlayer(player);
            EggWars.players.add(newPl);
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Couldn't find EggWars instance of player \"" + player.getName() + "\", successfully created new one.");
            new Exception().printStackTrace();
            return newPl;
        }
        catch (Exception ex)
        {
            Bukkit.getPluginManager().disablePlugin(EggWars.instance);
            throw new IllegalStateException("EGGWARS ERROR: Can't create Eggwars Player instance for \"" + player.getName() + "\", please review your server configuration.", ex);
        }*/
        return null;
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

    public static int getPoints(Player player)
    {
        if (EggWars.config.vault)
        {
            int i = VaultEconomy.getBalance(player);
            EggWars.getDB().getPlayerData(player).setPoints(i);
            return i;
        }
        else
        {
            return EggWars.getDB().getPlayerData(player).getPoints();
        }
    }

    public static void setPoints(Player player, int i)
    {
        if (EggWars.config.vault)
        {
            VaultEconomy.setPoints(player, i);
        }

        EggWars.getDB().getPlayerData(player).setPoints(i);
    }

    /** Adds the specified amount of points to the player and sends a message **/
    public static void addPoints(Player player, int i)
    {
        if (i <= 0)
        {
            return;
        }

        float mul = EggWars.config.pointMultiplier;
        String msg = "";

        if (player.hasPermission("eggwars.multpoints") && mul != 1.0F)
        {
            i = (int)((float)i * mul);
            msg = TranslationUtils.getMessage("gameplay.misc.points_multiplied", player, Float.valueOf(mul));
        }

        setPoints(player, getPoints(player) + i);
        TranslationUtils.sendMessage("gameplay.misc.add_points", player, Integer.valueOf(i), msg);
    }

    public static boolean hasKit(Player player, Kit kit)
    {
        return EggWars.getDB().getPlayerData(player).hasKit(kit.id());
    }

    @Nullable
    public static Kit getSelectedKit(Player player)
    {
        return EggWars.getKitManager().getKit(EggWars.getDB().getPlayerData(player).getKit());
    }

    public static boolean setSelectedKit(Player player, Kit kit)
    {
        return EggWars.getDB().getPlayerData(player).setKit(kit == null ? "" : kit.id());
    }

    public static String getLangId(Player player)
    {
        return EggWars.getDB().getPlayerData(player).getLocale();
    }

    public static void setLangId(Player player, String langCode)
    {
        String prevLang = getLangId(player);
        EggWars.getDB().getPlayerData(player).setLocale(langCode);

        try
        {
            PlayerChangeEwLangEvent ewplayerchangelangevent = new PlayerChangeEwLangEvent(player, prevLang);
            Bukkit.getPluginManager().callEvent(ewplayerchangelangevent);
        }
        catch (LinkageError linkageerror)
        {
        }
    }

    public static void tpToLobby(Player player, boolean sendBungee)
    {
        if (player == null || !player.isOnline())
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
        for (PotionEffect effect : player.getActivePotionEffects())
        {
            player.removePotionEffect(effect.getType());
        }
    }

    public static boolean setCompassTarget(EwPlayer tracker, boolean updateNearby)
    {
        if (!tracker.isInArena() || tracker.getTeam() == null || tracker.isEliminated())
        {
            return false;
        }

        if (!updateNearby && tracker.getTrackedPlayer() != null && tracker.getTrackedPlayer().isInArena() && tracker.getTrackedPlayer().getArena() == tracker.getArena() && !tracker.getTrackedPlayer().isEliminated())
        {
            EwPlayer ewplayer1 = tracker.getTrackedPlayer();
            tracker.getPlayer().setCompassTarget(ewplayer1.getPlayer().getLocation());

            if (tracker.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.COMPASS) || tracker.getPlayer().getInventory().getItemInOffHand().getType().equals(Material.COMPASS))
            {
                ReflectionUtils.sendActionBar(tracker.getPlayer(), TranslationUtils.getMessage("gameplay.ingame.compass_target", tracker.getPlayer(), TeamUtils.translateTeamType(ewplayer1.getTeam().getType(), tracker.getPlayer(), false), TeamUtils.colorizePlayerName(ewplayer1), String.format("%.1f", Double.valueOf(tracker.getPlayer().getLocation().distance(ewplayer1.getPlayer().getLocation())))), 0, 25, 0);
            }

            return true;
        }

        EwPlayer ewplayer2 = getNearestPlayer(tracker.getPlayer().getLocation(), -1, (foundPlayer) -> foundPlayer.isInArena() && foundPlayer.getArena() == tracker.getArena() && foundPlayer.getTeam() != null && foundPlayer.getTeam() != tracker.getTeam() && !foundPlayer.isEliminated());

        if (ewplayer2 == null)
        {
            return false;
        }

        tracker.getPlayer().setCompassTarget(ewplayer2.getPlayer().getLocation());

        if (tracker.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.COMPASS) || tracker.getPlayer().getInventory().getItemInOffHand().getType().equals(Material.COMPASS))
        {
            ReflectionUtils.sendActionBar(tracker.getPlayer(), TranslationUtils.getMessage("gameplay.ingame.compass_target", tracker.getPlayer(), TeamUtils.translateTeamType(ewplayer2.getTeam().getType(), tracker.getPlayer(), false), TeamUtils.colorizePlayerName(ewplayer2), String.format("%.1f", Double.valueOf(tracker.getPlayer().getLocation().distance(ewplayer2.getPlayer().getLocation())))), 0, 25, 0);
        }

        return true;
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
