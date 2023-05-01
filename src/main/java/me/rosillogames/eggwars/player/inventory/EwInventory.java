package me.rosillogames.eggwars.player.inventory;

import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.inventory.Inventory;
import me.rosillogames.eggwars.player.EwPlayer;

public class EwInventory
{
    private final EwPlayer player;
    private final EwInvType invType;
    @Nullable
    private EwInventory parent;
    private Inventory mcInventory;
    private TranslatableInventory handler;
    @Nullable
    private Object extraData;

    public EwInventory(EwPlayer player1, TranslatableInventory mcInv, EwInvType type)
    {
    	this.player = player1;
    	this.invType = type;
    	this.mcInventory = mcInv.getTranslatedInventory(player1.getPlayer());
        this.handler = mcInv;
    }

    public void setParent(EwInventory parent)
    {
    	this.parent = parent;
    }

    @Nullable
    public EwInventory getParent()
    {
        return this.parent;
    }

    public EwPlayer getPlayer()
    {
        return this.player;
    }

    public EwInvType getInventoryType()
    {
        return this.invType;
    }

    public Inventory getInventory()
    {
        return this.mcInventory;
    }

    public void setExtraData(@Nullable Object data)
    {
        this.extraData = data;
    }

    @Nullable
    public Object getExtraData()
    {
        return this.extraData;
    }

    public int getSize()
    {
        return this.handler.getSize();
    }

    public void updateHandler(@Nullable TranslatableInventory handlerIn, boolean reopen)
    {
        if (handlerIn == null)
        {
            handlerIn = this.handler;
        }
        else
        {
            this.handler = handlerIn;
        }

        if (reopen)
        {
            this.mcInventory = handlerIn.getTranslatedInventory(this.player.getPlayer());
            this.player.getPlayer().openInventory(this.mcInventory);
        }
        else
        {
            this.mcInventory.clear();

            for (Map.Entry<Integer, TranslatableItem> entry : handlerIn.getContents().entrySet())
            {
                this.mcInventory.setItem(((Integer)entry.getKey()).intValue(), ((TranslatableItem)entry.getValue()).getTranslated(this.player.getPlayer()));
            }
        }
    }
}
