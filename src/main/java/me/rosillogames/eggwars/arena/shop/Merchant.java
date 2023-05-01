package me.rosillogames.eggwars.arena.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;

public class Merchant
{
    private final List<Offer> offers = new ArrayList();
    private final EwPlayerMenu.MenuSize size;
    private final int[] armor;
    protected TranslatableInventory inventory;
    protected Map<Integer, Offer> slots;

    public Merchant(EwPlayerMenu.MenuSize sizeIn, int[] armorIn)
    {
        this.size = sizeIn;
        this.armor = armorIn;
    }

    public Merchant addOffer(Offer offer)
    {
        this.offers.add(offer);
        return this;
    }

    public EwPlayerMenu.MenuSize getSize()
    {
        return this.size;
    }

    public int[] getArmorSlots()
    {
        return this.armor;
    }

    public List<Offer> getOffers()
    {
        return this.offers;
    }

    public boolean isEmpty()
    {
        return this.offers.isEmpty();
    }
}
