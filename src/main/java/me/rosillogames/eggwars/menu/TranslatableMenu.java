package me.rosillogames.eggwars.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;

public class TranslatableMenu extends EwMenu
{
    private final List<TranslatableInventory> inventories;
    private final MenuClickListener listener;
    @Nullable
    private Object[] extraData;//try as possible to store only type ids, and not types themselves

    public TranslatableMenu(MenuType type)
    {
        this(type, (event, pl, menu) -> MenuClickListener.defaultListener(event, pl, menu));
    }

    public TranslatableMenu(MenuType type, MenuClickListener listenerIn)
    {
        super(type);
        this.inventories = new ArrayList();
        this.listener = (event, pl, menu) ->
        {
            if (!MenuClickListener.listenGeneric(event, pl, menu))
            {
                listenerIn.listenClick(event, pl, menu);
            }
        };
    }

    public void addPage(TranslatableInventory inventory)
    {
        this.inventories.add(inventory);
    }

    public TranslatableInventory getPage(int page)
    {
        return this.inventories.get(page);
    }

    public void clearPages()
    {
        this.inventories.clear();
    }

    @Override
    public boolean addOpener(EwPlayer player)
    {
        boolean flag = super.addOpener(player);

        if (flag)
        {
            player.setMenuPage(0);
        }

        return flag;
    }

    @Override
    public void removeOpener(EwPlayer player)
    {
        super.removeOpener(player);
        player.setMenuPage(0);
    }

    public boolean isPagedMenu()
    {
        return this.inventories.size() > 1;
    }

    @Nullable
    @Override
    public Inventory translateToPlayer(EwPlayer player, boolean reopen)
    {
        Inventory mcInventory;
        int page = Math.min(player.getMenuPage(), this.inventories.size() - 1);

        if (player.getMenu() == this && !reopen)
        {
            mcInventory = this.openers.get(player);
            mcInventory.clear();

            for (Map.Entry<Integer, Function<Player, ItemStack>> entry : this.inventories.get(page).getContents().entrySet())
            {
                mcInventory.setItem(((Integer)entry.getKey()).intValue(), entry.getValue().apply(player.getPlayer()));
            }
        }
        else
        {
            mcInventory = this.inventories.get(page).getTranslatedInventory(player.getPlayer());
        }

        return mcInventory;
    }

    @Override
    public void clickInventory(InventoryClickEvent clickEvent, EwPlayer player)
    {
        this.listener.listenClick(clickEvent, player, this);
    }

    public void setExtraData(@Nullable Object... data)
    {
        this.extraData = data;
    }

    @Nullable
    @Override
    public Object[] getExtraData()
    {
        return this.extraData;
    }
}
