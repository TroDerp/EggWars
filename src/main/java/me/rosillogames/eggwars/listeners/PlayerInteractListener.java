package me.rosillogames.eggwars.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.LobbySigns;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class PlayerInteractListener implements Listener
{
    @EventHandler
    public void openShop(PlayerInteractEntityEvent event)
    {
        if (!(event.getRightClicked() instanceof Villager))
        {
            return;
        }

        Villager villager = (Villager)event.getRightClicked();

        if (!TranslationUtils.getMessage("gameplay.villager.name").equals(villager.getCustomName()))
        {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().closeInventory();

        if (!event.getPlayer().hasPermission("eggwars.shop"))
        {
            TranslationUtils.sendMessage("commands.error.no_permission", (Player)event.getPlayer());
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (!ewplayer.isInArena() || ewplayer.isEliminated())
        {
            return;
        }

        if (ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME))
        {
            ewplayer.getArena().openVillagerInv(event.getPlayer(), 0);
            return;
        }
    }

    @EventHandler
    public void openKits(PlayerInteractEvent event)
    {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        if (event.getPlayer().getInventory().getItemInMainHand() == null)
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (!ewplayer.isInArena() || ewplayer.isEliminated())
        {
            return;
        }

        ItemStack itemstack = event.getPlayer().getInventory().getItemInMainHand();

        if (ItemUtils.getOpensMenu(itemstack) == MenuType.KIT_SELECTION)
        {
            event.setCancelled(true);
            EggWars.getKitManager().openKitsInv(ewplayer.getPlayer(), 0);
        }
    }

    @EventHandler
    public void openTeams(PlayerInteractEvent event)
    {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        if (event.getPlayer().getInventory().getItemInMainHand() == null)
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (!ewplayer.isInArena())
        {
            return;
        }

        if (ewplayer.isEliminated())
        {
            return;
        }

        ItemStack itemstack = event.getPlayer().getInventory().getItemInMainHand();

        if (ItemUtils.getOpensMenu(itemstack) == MenuType.TEAM_SELECTION)
        {
            event.setCancelled(true);
            ewplayer.getArena().openTeamInv(ewplayer.getPlayer());
        }
    }

    @EventHandler
    public void openVoting(PlayerInteractEvent event)
    {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        if (event.getPlayer().getInventory().getItemInMainHand() == null)
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (!ewplayer.isInArena())
        {
            return;
        }

        if (ewplayer.isEliminated())
        {
            return;
        }

        ItemStack itemstack = event.getPlayer().getInventory().getItemInMainHand();

        if (ItemUtils.getOpensMenu(itemstack) == MenuType.VOTING)
        {
            event.setCancelled(true);
            ewplayer.getArena().openVoteInv(ewplayer.getPlayer());
        }
    }

    @EventHandler
    public void leaveArena(PlayerInteractEvent event)
    {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        if (event.getPlayer().getInventory().getItemInMainHand() == null)
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (!ewplayer.isInArena())
        {
            return;
        }

        if (ewplayer.isEliminated())
        {
            return;
        }

        ItemStack itemstack = event.getPlayer().getInventory().getItemInMainHand();

        if (ItemUtils.getOpensMenu(itemstack) == MenuType.LEAVE_ARENA)
        {
            event.setCancelled(true);
            ewplayer.getArena().leaveArena(ewplayer, true, false);
        }
    }

    @EventHandler
    public void useCompassTracker(PlayerInteractEvent event)
    {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (!ewplayer.isInArena())
        {
            return;
        }

        if (!ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME))
        {
            return;
        }

        if (ewplayer.isEliminated())
        {
            return;
        }

        //don't use off hand because use-spam when placing blocks
        if (ewplayer.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.COMPASS))
        {
            boolean success = PlayerUtils.setCompassTarget(ewplayer, true);

            if (success)
            {
                ewplayer.getPlayer().playSound(ewplayer.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 2.0F);
            }

            return;
        }
    }

    @EventHandler
    public void openSetupGUI(PlayerInteractEvent event)
    {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        Player player = event.getPlayer();
        ItemStack stack;

        if ((stack = player.getInventory().getItemInMainHand()) == null)
        {
            return;
        }

        if (ItemUtils.getOpensMenu(stack) == MenuType.ARENA_SETUP)
        {
            event.setCancelled(true);
            String arenaId = ItemUtils.getPersistentData(stack, ItemUtils.arenaId, PersistentDataType.STRING);
            Arena arena;
            Arena idArena = EggWars.getArenaManager().getArenaById(arenaId);
            Arena worldArena = EggWars.getArenaManager().getArenaByWorld(player.getWorld());

            if (idArena == null && arenaId != null)
            {
                TranslationUtils.sendMessage("commands.error.arena_does_not_exist", player, arenaId);
                return;
            }
            else if (arenaId == null)
            {//Old behavior (when arenaId didn't exist)
                if (worldArena == null)
                {
                    TranslationUtils.sendMessage("commands.error.not_in_arena_world", player);
                    return;
                }

                arena = worldArena;
            }
            else
            {
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && worldArena == null && LobbySigns.isValidWallSign(event.getClickedBlock()))
                {//return if so, to prevent conflict with SetupGUI.Listener.signClick()
                    return;
                }

                arena = idArena;
            }

            arena.getSetupGUI().openArenaGUI(player);
        }
    }

    @EventHandler
    public void logStrip(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.useInteractedBlock() != Event.Result.DENY)
        {
            Arena arena = EggWars.getArenaManager().getArenaByWorld(event.getClickedBlock().getWorld());

            if (arena == null || arena.getStatus() == ArenaStatus.SETTING || arena.getReplacedBlocks().containsKey(event.getClickedBlock().getLocation()))
            {
                return;
            }

            String b = event.getClickedBlock().getType().toString();

            //log strip
            if (event.getMaterial().toString().contains("_AXE") && (b.contains("_LOG") || b.contains("_WOOD") || b.contains("_STEM") || b.contains("_HYPHAE")))
            {
                event.setCancelled(true);
            }

            //hoe till
            if (event.getMaterial().toString().contains("_HOE") && (b.contains("DIRT") || b.contains("_PATH") || b.contains("GRASS_BLOCK")))
            {
                event.setCancelled(true);
            }

            //dirt path
            if (event.getMaterial().toString().contains("_SHOVEL") && (b.contains("DIRT") || b.contains("GRASS_BLOCK") || b.contains("PODZOL") || b.contains("MYCELIUM")))
            {
                event.setCancelled(true);
            }
        }
    }
}
