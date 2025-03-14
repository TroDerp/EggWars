package me.rosillogames.eggwars.menu;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import com.google.common.collect.Sets;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.player.MenuPlayer;

public abstract class EwMenu
{
    private final MenuType invType;
    protected final Set<MenuPlayer> openers = Sets.newHashSet();
    //Mainly used by shop, but can be used by other menus
    private boolean allowEventsOutside;
    @Nullable
    private Object[] extraData;//try as possible to store only type ids, and not types themselves

    protected EwMenu(MenuType type)
    {
        this.invType = type;
    }

    public void setUsable(boolean flag)
    {
        this.allowEventsOutside = flag;
    }

    public boolean isUsable()
    {
        return this.allowEventsOutside;
    }

    public boolean isPagedMenu()
    {
        return false;
    }

    public Set<MenuPlayer> getOpeners()
    {
        return new HashSet(this.openers);
    }

    public MenuType getMenuType()
    {
        return this.invType;
    }

    /** Should only be called by InventoryListener#updateMenuWhenClosing **/
    public void removeOpener(MenuPlayer player)
    {
        this.openers.remove(player);
        player.setMenu(null);
    }

    public void closeForEveryone(boolean goToParent)
    {
        for (MenuPlayer player : this.openers)
        {
            player.closeMenu(goToParent ? 1 : 0);
        }
    }

    public void closeForOpener(MenuPlayer player)
    {
        player.closeMenu(1);
    }

    public boolean addOpener(MenuPlayer player)
    {
        Inventory inv = this.translateToPlayer(player, true);

        if (inv != null)
        {
            /* Use this order to do the reverse of EwMenu#sendMenuUpdate so EwMenu#removeOpener
             * is properly called from previous menu because of InventoryCloseEvent. */
            player.getPlayer().openInventory(inv);
            player.setCurrentInventory(inv);
            this.openers.add(player);
            player.setMenu(this);
            return true;
        }

        return false;
    }

    public void sendMenuUpdate(boolean reopen)
    {
        for (MenuPlayer player : this.getOpeners())
        {
            this.sendUpdateTo(player, reopen);
        }
    }

    public void sendUpdateTo(MenuPlayer player, boolean reopen)
    {
        Inventory inv = this.translateToPlayer(player, reopen);

        if (inv != null && reopen)
        {
            /* Set new inv as open before InventoryListener#updateMenuWhenClosing is called
             * using Player#openInventory so actual menu doesn't equal closing inventory
             * when checked for calling EwMenu#removeOpener. */
            this.openers.add(player);
            player.setCurrentInventory(inv);
            player.getPlayer().openInventory(inv);
        }
        else if (inv == null)
        {
            this.closeForOpener(player);
        }
    }

    @Nullable
    public abstract Inventory translateToPlayer(MenuPlayer player, boolean reopen);

    public void clickInventory(InventoryClickEvent clickEvent, MenuPlayer player)
    {//Empty but present for menus that don't have any click action
    }

    @Nullable
    public Object[] getExtraData()
    {
        return new Object[0];
    }
}
