package me.rosillogames.eggwars.menu;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import com.google.common.collect.Maps;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.player.EwPlayer;

public abstract class EwMenu
{
    private final MenuType invType;
    protected final Map<EwPlayer, Inventory> openers = Maps.newHashMap();
    //Mainly used by shop, but can be used by other menus
    private boolean allowEventsOutside;
    @Nullable
    private EwMenu parent;
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

    //@Deprecated
    /** Provisional method and field to keep "Go back" button functionality 
     ** TODO MenuPlayer will handle all the "parenting" system **/
    public void setParent(@Nullable EwMenu menu)
    {
        this.parent = menu;
    }

    public boolean isPagedMenu()
    {
        return false;
    }

    public Set<EwPlayer> getOpeners()
    {
        return new HashSet(this.openers.keySet());
    }

    @Nullable
    public Inventory getPlayerInv(EwPlayer player)
    {
        return this.openers.get(player);
    }

    public MenuType getMenuType()
    {
        return this.invType;
    }

    public void removeOpener(EwPlayer player)
    {
        this.openers.remove(player);
        player.setMenu(null);
    }

    public void closeForEveryone(boolean goToParent)
    {
        for (EwPlayer player : this.getOpeners())
        {
            if (goToParent && this.parent != null)
            {
                this.parent.addOpener(player);
                return;
            }

            player.getPlayer().closeInventory();/* removeOpener is called from close listener */
        }
    }

    public void closeForOpener(EwPlayer player)
    {
        if (this.parent != null)
        {
            this.parent.addOpener(player);
            return;
        }

        player.getPlayer().closeInventory();/* removeOpener will be called from close listener */
    }

    public boolean addOpener(EwPlayer player)
    {
        Inventory inv = this.translateToPlayer(player, true);

        if (inv != null)
        {
            /* Use this order to do the reverse of EwMenu#sendMenuUpdate so EwMenu#removeOpener
             * is properly called from previous menu because of InventoryCloseEvent. */
            player.getPlayer().openInventory(inv);
            this.openers.put(player, inv);
            player.setMenu(this);
            return true;
        }

        return false;
    }

    public void sendMenuUpdate(boolean reopen)
    {
        for (EwPlayer player : this.getOpeners())
        {
            this.sendUpdateTo(player, reopen);
        }
    }

    public void sendUpdateTo(EwPlayer player, boolean reopen)
    {
        Inventory inv = this.translateToPlayer(player, reopen);

        if (inv != null && reopen)
        {
            /* Set new inv as open before InventoryListener#updateMenuWhenClosing is called
             * using Player#openInventory so actual menu doesn't equal closing inventory
             * when checked for calling EwMenu#removeOpener. */
            this.openers.put(player, inv);
            player.getPlayer().openInventory(inv);
        }
        else if (inv == null)
        {
            this.closeForOpener(player);
        }
    }

    @Nullable
    public abstract Inventory translateToPlayer(EwPlayer player, boolean reopen);

    public void clickInventory(InventoryClickEvent clickEvent, EwPlayer player)
    {//Empty but present for menus that don't have any click action
    }

    @Nullable
    public Object[] getExtraData()
    {
        return new Object[0];
    }
}
