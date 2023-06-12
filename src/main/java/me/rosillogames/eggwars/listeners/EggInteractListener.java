package me.rosillogames.eggwars.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.events.EwPlayerRemoveEggEvent;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class EggInteractListener implements Listener
{
    @EventHandler
    public void interact(PlayerInteractEvent event)
    {
        if ((!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.LEFT_CLICK_BLOCK)) || !event.getClickedBlock().getType().equals(Material.DRAGON_EGG))
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (!ewplayer.isInArena())
        {
            Arena arena = EggWars.getArenaManager().getArenaByWorld(ewplayer.getPlayer().getWorld());

            if (arena != null && (ewplayer.getSettingArena() != arena || ewplayer.getSettingArena().getStatus() != ArenaStatus.SETTING))
            {
                event.setCancelled(true);
            }

            return;
        }

        if (ewplayer.isEliminated())
        {
            event.setCancelled(true);
            return;
        }

        Arena arena = ewplayer.getArena();
        Team team = TeamUtils.getTeamByEggLocation(arena, event.getClickedBlock().getLocation());

        if (team == null || team.isEliminated())
        {
            return;
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && team.equals(ewplayer.getTeam()) && isPlaceableBlockItem(event.getPlayer().getInventory().getItemInMainHand()))
        {
            if (event.getPlayer().isSneaking())
            {
                return;
            }

            event.setCancelled(true);
            Block placing = event.getClickedBlock().getRelative(event.getBlockFace());
            BlockState replacedBS = placing.getState();

            if (!placing.isEmpty())
            {
                return;
            }

            ItemStack copy = event.getPlayer().getInventory().getItemInMainHand().clone();
            placing.setType(event.getPlayer().getInventory().getItemInMainHand().getType());
            arena.addPlacedBlock(placing.getLocation());
            ewplayer.getIngameStats().addStat(StatType.BLOCKS_PLACED, 1);

            if ((event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1) == 0)
            {
                event.getPlayer().getInventory().setItemInMainHand(null);
            }
            else
            {
                event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            }

            event.setUseItemInHand(Result.ALLOW);
            event.getPlayer().playSound(placing.getLocation(), Sound.BLOCK_METAL_PLACE, 1.0f, 1.0f);
            Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(placing, replacedBS, event.getClickedBlock(), copy, event.getPlayer(), true, event.getHand()));
            return;
        }
        else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && team.equals(ewplayer.getTeam()) && isPlaceableBlockItem(event.getPlayer().getInventory().getItemInOffHand()))
        {
            if (event.getPlayer().isSneaking())
            {
                return;
            }

            event.setCancelled(true);
            Block placing = event.getClickedBlock().getRelative(event.getBlockFace());
            BlockState rBS = placing.getState();

            if (!placing.isEmpty())
            {
                return;
            }

            ItemStack copy = event.getPlayer().getInventory().getItemInOffHand().clone();
            placing.setType(event.getPlayer().getInventory().getItemInOffHand().getType());
            arena.addPlacedBlock(placing.getLocation());
            ewplayer.getIngameStats().addStat(StatType.BLOCKS_PLACED, 1);

            if ((event.getPlayer().getInventory().getItemInOffHand().getAmount() - 1) == 0)
            {
                event.getPlayer().getInventory().setItemInOffHand(null);
            }
            else
            {
                event.getPlayer().getInventory().getItemInOffHand().setAmount(event.getPlayer().getInventory().getItemInOffHand().getAmount() - 1);
            }

            event.setUseItemInHand(Result.ALLOW);
            event.getPlayer().playSound(placing.getLocation(), Sound.BLOCK_METAL_PLACE, 1.0f, 1.0f);
            Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(placing, rBS, event.getClickedBlock(), copy, event.getPlayer(), true, event.getHand()));
            return;
        }

        event.setCancelled(true);

        if (team.equals(ewplayer.getTeam()))
        {
            return;
        }

        try
        {
            EwPlayerRemoveEggEvent ewplayerremoveeggevent = new EwPlayerRemoveEggEvent(ewplayer, team);
            Bukkit.getPluginManager().callEvent(ewplayerremoveeggevent);

            if (ewplayerremoveeggevent.isCancelled())
            {
                return;
            }
        }
        catch (Exception exception)
        {
        }

        ewplayer.getIngameStats().addStat(StatType.EGGS_BROKEN, 1);

        for (EwPlayer player : team.getArena().getPlayers())
        {
            TranslationUtils.sendMessage("gameplay.ingame.team_egg_destroyed", player.getPlayer(), TeamUtils.translateTeamType(team.getType(), player.getPlayer(), false), TeamUtils.colorizePlayerName(ewplayer));
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100F, 1.0F);

            if (player.getTeam() == team)
            {
                ReflectionUtils.sendTitle(player.getPlayer(), Integer.valueOf(5), Integer.valueOf(20), Integer.valueOf(5), "", TranslationUtils.getMessage("gameplay.ingame.your_egg_destroyed", player.getPlayer()));
                TranslationUtils.sendMessage("gameplay.ingame.your_egg_destroyed", player.getPlayer());
            }
        }

        event.getClickedBlock().setType(Material.AIR);
        PlayerUtils.addPoints(ewplayer, EggWars.instance.getConfig().getInt("gameplay.points.on_egg"));
        arena.getScores().updateScores(false);
    }

    @EventHandler
    public void disableTeleport(BlockFromToEvent event)
    {
        if (event.getBlock().getType().equals(Material.DRAGON_EGG) && EggWars.getArenaManager().getArenaByWorld(event.getBlock().getWorld()) != null)
        {
            event.setCancelled(true);
        }
    }

    private static boolean isPlaceableBlockItem(ItemStack stack)
    {
        return stack != null && stack.getType().isBlock() && stack.getType() != Material.AIR;
    }
}
