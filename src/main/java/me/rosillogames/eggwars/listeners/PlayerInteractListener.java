package me.rosillogames.eggwars.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.SetupGUI;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.loaders.ArenaLoader;
import me.rosillogames.eggwars.loaders.KitLoader;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.VoteUtils;

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

        if (villager.getCustomName() == null)
        {
            return;
        }

        if (!villager.getCustomName().equals(TranslationUtils.getMessage("gameplay.villager.name")))
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
    public void openKits(PlayerInteractEvent playerinteractevent)
    {
        if (!playerinteractevent.getAction().equals(Action.RIGHT_CLICK_AIR) && !playerinteractevent.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        if (playerinteractevent.getPlayer().getInventory().getItemInMainHand() == null)
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(playerinteractevent.getPlayer());

        if (!ewplayer.isInArena() || ewplayer.isEliminated())
        {
            return;
        }

        ItemStack itemstack = playerinteractevent.getPlayer().getInventory().getItemInMainHand();

        if (itemstack.equals(KitLoader.getInvItem(playerinteractevent.getPlayer())))
        {
            playerinteractevent.setCancelled(true);
            EggWars.getKitManager().openKitsInv(ewplayer.getPlayer(), 0);
        }
    }

    @EventHandler
    public void openTeams(PlayerInteractEvent playerinteractevent)
    {
        if (!playerinteractevent.getAction().equals(Action.RIGHT_CLICK_AIR) && !playerinteractevent.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        if (playerinteractevent.getPlayer().getInventory().getItemInMainHand() == null)
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(playerinteractevent.getPlayer());

        if (!ewplayer.isInArena())
        {
            return;
        }

        if (ewplayer.isEliminated())
        {
            return;
        }

        ItemStack itemstack = playerinteractevent.getPlayer().getInventory().getItemInMainHand();

        if (itemstack.equals(TeamUtils.getInvItem(playerinteractevent.getPlayer())))
        {
            playerinteractevent.setCancelled(true);
            ewplayer.getArena().openTeamInv(ewplayer.getPlayer());
        }
    }

    @EventHandler
    public void openVoting(PlayerInteractEvent playerinteractevent)
    {
        if (!playerinteractevent.getAction().equals(Action.RIGHT_CLICK_AIR) && !playerinteractevent.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        if (playerinteractevent.getPlayer().getInventory().getItemInMainHand() == null)
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(playerinteractevent.getPlayer());

        if (!ewplayer.isInArena())
        {
            return;
        }

        if (ewplayer.isEliminated())
        {
            return;
        }

        ItemStack itemstack = playerinteractevent.getPlayer().getInventory().getItemInMainHand();

        if (itemstack.equals(VoteUtils.getInvItem(playerinteractevent.getPlayer())))
        {
            playerinteractevent.setCancelled(true);
            ewplayer.getArena().openVoteInv(ewplayer.getPlayer());
        }
    }

    @EventHandler
    public void leaveArena(PlayerInteractEvent playerinteractevent)
    {
        if (!playerinteractevent.getAction().equals(Action.RIGHT_CLICK_AIR) && !playerinteractevent.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        if (playerinteractevent.getPlayer().getInventory().getItemInMainHand() == null)
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(playerinteractevent.getPlayer());

        if (!ewplayer.isInArena())
        {
            return;
        }

        if (ewplayer.isEliminated())
        {
            return;
        }

        ItemStack itemstack = playerinteractevent.getPlayer().getInventory().getItemInMainHand();

        if (itemstack.equals(ArenaLoader.getLeaveItem(playerinteractevent.getPlayer())))
        {
            playerinteractevent.setCancelled(true);
            ewplayer.getArena().leaveArena(ewplayer, true, false);
        }
    }

    @EventHandler
    public void useCompassTracker(PlayerInteractEvent playerinteractevent)
    {
        if (!playerinteractevent.getAction().equals(Action.RIGHT_CLICK_AIR) && !playerinteractevent.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(playerinteractevent.getPlayer());

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

        //don't use off had because use-spam when placing blocks
        if (ewplayer.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.COMPASS))
        {
            PlayerUtils.setCompassTarget(ewplayer, true);
            ewplayer.getPlayer().playSound(ewplayer.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 2.0F);
            return;
        }
    }

    @EventHandler
    public void openSetupGUI(PlayerInteractEvent playerinteractevent)
    {
        if (!playerinteractevent.getAction().equals(Action.RIGHT_CLICK_AIR) && !playerinteractevent.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        ItemStack itemstack;

        if ((itemstack = playerinteractevent.getPlayer().getInventory().getItemInMainHand()) == null)
        {
            return;
        }

        if (itemstack.equals(SetupGUI.getSetupGUIItem().getTranslated(playerinteractevent.getPlayer())))
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(playerinteractevent.getPlayer());
            Arena arena = EggWars.getArenaManager().getArenaByWorld(ewplayer.getPlayer().getWorld());
            playerinteractevent.setCancelled(true);

            if (arena == null || !arena.getStatus().equals(ArenaStatus.SETTING))
            {
                TranslationUtils.sendMessage("commands.error.not_in_arena_world", playerinteractevent.getPlayer());
                return;
            }

            ewplayer.setSettingArena(arena);
            SetupGUI.openArenaGUI(ewplayer.getPlayer(), arena);
        }
    }

    @EventHandler
    public void logStrip(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getMaterial().toString().contains("_AXE") && e.getClickedBlock() != null) {
            String b = e.getClickedBlock().getType().toString();
            if (b.contains("_LOG") || b.contains("_WOOD") || b.contains("_STEM") || b.contains("_HYPHAE")) {
                e.setCancelled(true);
            }
        }
    }
}
