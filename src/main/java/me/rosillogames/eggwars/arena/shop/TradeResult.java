package me.rosillogames.eggwars.arena.shop;

import javax.annotation.Nullable;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.objects.AutoEquipEntry;

public class TradeResult
{
    private final ItemStack result;
    private boolean colorize;
    @Nullable
    private AutoEquipEntry autoEquip;
    private boolean inheritNameDesc = false;

    public TradeResult(ItemStack resultIn)
    {
        this.result = resultIn;
    }

    public ItemStack getResult()
    {
        return this.result;
    }

    public boolean isUsingTeamColor()
    {
        return this.colorize;
    }

    public void setUsesTeamColor(boolean flag)
    {
        this.colorize = flag;
    }

    @Nullable
    public AutoEquipEntry getAutoEquip()
    {
        return this.autoEquip;
    }

    public void setAutoEquip(AutoEquipEntry conf)
    {
        this.autoEquip = conf;
    }

    public boolean inheritsNameDesc()
    {
        return this.inheritNameDesc;
    }

    public void setInheritsNameDesc(boolean flag)
    {
        this.inheritNameDesc = flag;
    }
}
