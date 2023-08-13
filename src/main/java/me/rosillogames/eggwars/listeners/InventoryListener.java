package me.rosillogames.eggwars.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.EwInventory;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class InventoryListener implements Listener
{
    @EventHandler
    public void openTeamEC(InventoryOpenEvent openEvent)
    {
        if (!(openEvent.getPlayer() instanceof Player) || openEvent.getPlayer() == null)
        {
            return;
        }

        EwPlayer ewply = PlayerUtils.getEwPlayer((Player)openEvent.getPlayer());

        if (ewply != null && ewply.getTeam() != null && openEvent.getInventory().getType() == InventoryType.ENDER_CHEST && ewply.getInv() == null && EggWars.config.shareTeamEC)
        {
            openEvent.setCancelled(true);
            EwInventory ewInv = new EwInventory(ewply, ewply.getTeam().getEnderChest(), MenuType.TEAM_ENDER_CHEST);
            Block block = ReflectionUtils.getEndChestBlock(ewply.getPlayer());

            if (block != null && block.getState() instanceof EnderChest)
            {
                ((EnderChest)block.getState()).open();
                ewInv.setExtraData(block);
            }

            ewply.setInv(ewInv);
            ewply.getPlayer().openInventory(ewInv.getInventory());
        }

        return;
    }

    @EventHandler
    public void updatePlayerInvWhenClosing(InventoryCloseEvent closeEvent)
    {
        if (!(closeEvent.getPlayer() instanceof Player) || closeEvent.getPlayer() == null || !((Player)closeEvent.getPlayer()).isOnline())
        {
            return;
        }

        EwPlayer ewply = PlayerUtils.getEwPlayer((Player)closeEvent.getPlayer());

        if (ewply == null || ewply.getInv() == null)
        {
            return;
        }

        if (ewply.getInv().getInventoryType() == MenuType.TEAM_ENDER_CHEST && ewply.getInv().getExtraData() != null)
        {
            BlockState state = ((Block)ewply.getInv().getExtraData()).getLocation().getBlock().getState();

            if (state instanceof EnderChest)
            {
                ((EnderChest)state).close();
            }
        }

        if (ewply.getInv().getInventory() == closeEvent.getInventory())
        {
            ewply.setInv(null);
        }

        return;
    }
}
