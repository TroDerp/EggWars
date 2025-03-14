package me.rosillogames.eggwars.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.util.Vector;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.MenuPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class InventoryListener implements Listener
{
    @EventHandler
    public void cancelIllegalInventoryChanges(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.isInArena() && clickEvent.getClickedInventory() != null)
        {
            if (clickEvent.getClickedInventory().getType() == InventoryType.PLAYER && !ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME))
            {
                clickEvent.setCancelled(true);
                return;
            }

            //TODO: Store original contents of containers modified during the game to fix a bug
            //where containers are saved between games (also happens to chests), in new arena class
            //if (clickEvent.getClickedInventory().getType() == InventoryType.FURNACE)
            //{
                //clickEvent.setCancelled(true);
                //return;
            //}
        }
    }

    @EventHandler
    public void cancelIllegalItemSwap(PlayerSwapHandItemsEvent event)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (ewplayer.isInArena() && !ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME))
        {
            event.setCancelled(true);
            return;
        }
        else
        {
            return;
        }
    }

    @EventHandler
    public void openTeamEC(InventoryOpenEvent openEvent)
    {
        if (!(openEvent.getPlayer() instanceof Player))
        {
            return;
        }

        EwPlayer ewply = PlayerUtils.getEwPlayer((Player)openEvent.getPlayer());

        if (ewply != null && ewply.getTeam() != null && openEvent.getInventory().getType() == InventoryType.ENDER_CHEST && EggWars.config.shareTeamEC)
        {
            openEvent.setCancelled(true);
            ewply.getMenuPlayer().openMenu(ewply.getTeam().getEnderChest());
            Block block = ReflectionUtils.getEndChestBlock(ewply.getPlayer());

            if (block != null && block.getState() instanceof EnderChest)
            {
                if (EggWars.serverVersion.ordinal() >= Versions.V_1_19_R1.ordinal())
                {
                    ((EnderChest)block.getState()).open();
                }

                ewply.getTeam().addEChester(block.getLocation().toVector(), ewply);
            }
        }

        return;
    }

    @EventHandler
    public void updatePlayerInvWhenClosing(InventoryCloseEvent event)
    {
        if (!(event.getPlayer() instanceof Player) || !((Player)event.getPlayer()).isOnline())
        {
            return;
        }

        EwPlayer ewply = PlayerUtils.getEwPlayer((Player)event.getPlayer());

        if (ewply == null)
        {
            return;
        }

        MenuPlayer menuply = ewply.getMenuPlayer();

        if (menuply.getMenu() != null)
        {
            if (menuply.getMenu().getMenuType() == MenuType.TEAM_ENDER_CHEST)
            {
                Vector vec = ewply.getTeam().removeEChester(ewply);

                if (vec != null)
                {
                    BlockState state = (new Location(event.getPlayer().getWorld(), vec.getBlockX(), vec.getBlockY(), vec.getBlockZ())).getBlock().getState();

                    if (state instanceof EnderChest && EggWars.serverVersion.ordinal() >= Versions.V_1_19_R1.ordinal())
                    {
                        ((EnderChest)state).close();
                    }
                }
            }

            if (event.getInventory().equals(menuply.getCurrentInventory()))
            {//Closing inventory will not equal actual menu inventory when it's updating using re-open.
                menuply.getMenu().removeOpener(menuply);
            }

            return;
        }

        return;
    }

    @EventHandler
    public void shopDrag(InventoryDragEvent dragEvent)
    {
        MenuPlayer menuply = PlayerUtils.getEwPlayer((Player)dragEvent.getWhoClicked()).getMenuPlayer();

        // Check cancelled to prevent collision with other plugins.
        if (menuply.getMenu() == null || dragEvent.isCancelled() || !menuply.getMenu().isUsable())
        {
            return;
        }

    //this controls whether if drag action is invalid in the current slot (inside villager container)
        for (Integer integer : dragEvent.getRawSlots())
        {
            if (integer.intValue() < dragEvent.getInventory().getSize())
            {
                dragEvent.setCancelled(true);
                return;
            }
        }

        return;
    }

    @EventHandler
    public void newMenus(InventoryClickEvent clickEvent)
    {
        MenuPlayer menuply = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked()).getMenuPlayer();

        // Check cancelled to prevent collision with other plugins.
        if (menuply.getMenu() != null && !clickEvent.isCancelled())
        {
            if (menuply.getMenu().getMenuType() == MenuType.TEAM_ENDER_CHEST)
            {//Should I add a field instead to check this?
                return;
            }

            if (menuply.getMenu().isUsable())
            {
                if (clickEvent.getAction() == InventoryAction.DROP_ONE_CURSOR || clickEvent.getAction() == InventoryAction.DROP_ALL_CURSOR)
                {
                    return;
                }

                if (clickEvent.getRawSlot() > (clickEvent.getInventory().getSize() - 1))
                {//this controls whether if shift click is invalid in the current slot (inside villager container)
                    if (clickEvent.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
                    {
                        clickEvent.setCancelled(true);
                        return;
                    }

                    return;
                }
            }

            clickEvent.setCancelled(true);

            if (clickEvent.getCurrentItem() == null)
            {
                return;
            }

            menuply.getMenu().clickInventory(clickEvent, menuply);
            return;
        }
    }
}
