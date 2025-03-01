package me.rosillogames.eggwars.arena.shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.menu.EwMenu;
import me.rosillogames.eggwars.menu.MenuClickListener;
import me.rosillogames.eggwars.menu.ProfileMenus;
import me.rosillogames.eggwars.menu.SerializingItems;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class Category extends EwMenu
{
    private final Map<String, Offer> offers = new HashMap();
    /* Offer names are properly sorted for classic shop here */
    private final List<String> offerOrdinal = new ArrayList();
    private final ProfileMenus.MenuSize size;
    private final Map<EquipmentSlot, Integer> displayArmor;
    private TranslatableInventory inventory;
    private ItemStack displayItem;
    private String translationKey;

    public Category(ProfileMenus.MenuSize sizeIn, Map<EquipmentSlot, Integer> armorIn, ItemStack stack, String transKey)
    {
        super(MenuType.VILLAGER_TRADING);
        this.setUsable(true);
        this.size = sizeIn;
        this.displayArmor = armorIn;
        this.displayItem = stack;
        this.translationKey = transKey;
    }

    public void addOffer(String name, Offer offer)
    {
        this.offers.put(name, offer);
        this.offerOrdinal.add(name);
    }

    public Map<String, Offer> getOffers()
    {
        return this.offers;
    }

    public boolean isEmpty()//TODO: re-evaluate / do calculate if one merchant is empty, or if all are empty?
    {
        return this.offers.isEmpty();
    }

    public ItemStack getDisplayItem()
    {
        return this.displayItem;
    }

    public String getTranslation()
    {
        return this.translationKey;
    }

    public void buildMenu()
    {
        //build merchant menu
        TranslatableInventory tInv = new TranslatableInventory(this.size.getSlots(), this.translationKey + ".title");
        tInv.setItem(this.size.getSlots() - 5, ProfileMenus.getCloseItem());

        for (Map.Entry<EquipmentSlot, Integer> entry : this.displayArmor.entrySet())
        {
            TranslatableItem tItem = new TranslatableItem((player1) ->
            {
                ItemStack stack = player1 == null ? null : ItemUtils.getSlot(player1, entry.getKey());

                if (stack == null || stack.getType() == Material.AIR)
                {
                    stack = new ItemStack(Material.BARRIER);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(TranslationUtils.getMessage("shop.empty_armor_item.name", player1));
                    meta.setLore(Arrays.asList(TranslationUtils.getMessage("shop.empty_armor_item.desc", player1).split("\\n")));
                    stack.setItemMeta(meta);
                }

                return stack;
            });
            tItem.dontResetLore();
            tInv.setItem(entry.getValue(), tItem);
        }

        this.inventory = tInv;

        for (Map.Entry<String, Offer> entry : this.offers.entrySet())
        {
            tInv.setItem(entry.getValue().getSlot(), (player1) ->
            {
                ItemStack stack = entry.getValue().getDisplayItem(player1);
                SerializingItems.BUY_OFFER.setItemReference(stack, entry.getKey());
                return stack;
            });
        }
    }

    public void openTrading(EwPlayer player)
    {
        try
        {
            Player ply = player.getPlayer();

            if (EggWars.getDB().getPlayerData(ply).isClassicShop())
            {
                Villager villager = ReflectionUtils.<Villager>createEntity(ply.getWorld(), ply.getLocation().clone(), Villager.class, null);

                if (villager != null)
                {
                    String translatedTitle = TranslationUtils.getMessage(this.translationKey + ".title", ply);
                    villager.setCustomName(translatedTitle);
                    villager.setRecipes(this.getMerchantRecipes(ply));
                    ply.openMerchant(villager, true);
                }
            }
            else
            {
                this.addOpener(player);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private List<MerchantRecipe> getMerchantRecipes(Player player)
    {
        List<MerchantRecipe> recipeList = new ArrayList();

        for (String name : this.offerOrdinal)
        {
            Offer offer = this.offers.get(name);
            MerchantRecipe recipe;

            if (offer != null && (recipe = offer.getAsRecipe(player)) != null)
            {
                recipeList.add(recipe);
            }
        }

        return recipeList;
    }

    @Nullable
    @Override
    public Inventory translateToPlayer(EwPlayer player, boolean reopen)
    {
        Inventory mcInventory;

        if (player.getMenu() == this && !reopen)
        {
            mcInventory = this.openers.get(player);
            mcInventory.clear();

            for (Map.Entry<Integer, Function<Player, ItemStack>> entry : this.inventory.getContents().entrySet())
            {
                mcInventory.setItem(((Integer)entry.getKey()).intValue(), entry.getValue().apply(player.getPlayer()));
            }
        }
        else
        {
            mcInventory = this.inventory.getTranslatedInventory(player.getPlayer());
        }

        return mcInventory;
    }

    @Override
    public void clickInventory(InventoryClickEvent clickEvent, EwPlayer player)
    {
        if (MenuClickListener.listenGeneric(clickEvent, player, this) || !player.isInArena())
        {
            return;
        }

        ItemStack currItem = clickEvent.getCurrentItem();

        if (SerializingItems.BUY_OFFER.equals(SerializingItems.getReferenceType(currItem)))
        {
            Offer trade = this.offers.get(SerializingItems.BUY_OFFER.getItemReference(currItem));

            if (trade != null)
            {
                boolean traded = trade.trade(player.getPlayer(), clickEvent.getClick().isShiftClick());
                player.getPlayer().playSound(player.getPlayer().getLocation(), traded ? Sound.UI_BUTTON_CLICK : Sound.BLOCK_ANVIL_LAND, 1.0F, 2.0F);
                this.sendUpdateTo(player, false);
                return;
            }

            return;
        }
    }
}
