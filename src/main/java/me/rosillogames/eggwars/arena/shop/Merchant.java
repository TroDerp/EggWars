package me.rosillogames.eggwars.arena.shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.ItemType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.player.inventory.EwInvType;
import me.rosillogames.eggwars.player.inventory.InventoryController;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

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
