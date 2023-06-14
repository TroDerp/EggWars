package me.rosillogames.eggwars.arena.shop;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.ItemType;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.player.inventory.InventoryController;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class Category
{
    private ItemStack displayItem;
    private String translationKey;
    private Map<ItemType, Merchant> merchant = new EnumMap(ItemType.class);

    public Category(ItemStack stack, String name)
    {
        this.displayItem = stack;
        this.translationKey = name;
    }

    public ItemStack getDisplayItem()
    {
        return this.displayItem;
    }

    public String getTranslation()
    {
        return this.translationKey;
    }

    public void setMerchant(ItemType type, Merchant merch)
    {
        //build merchant menu
        TranslatableInventory tInv = new TranslatableInventory(merch.getSize().getSlots(), this.translationKey + ".title");
        tInv.setItem(merch.getSize().getSlots() - 5, EwPlayerMenu.getCloseItem());
        int[] armor = merch.getArmorSlots();

        if (armor != null && armor.length == 4)
        {
            for (int i = 0; i < 4; ++i)
            {
                final int fnl = i + 1;
                TranslatableItem tItem = new TranslatableItem((player1) ->
                {
                    ItemStack stack = player1 == null ? null : fnl == 1 ? player1.getInventory().getHelmet() : fnl == 2 ? player1.getInventory().getChestplate() : fnl == 3 ? player1.getInventory().getLeggings() : player1.getInventory().getBoots();

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
                tInv.setItem(armor[i], tItem);
            }
        }

        merch.inventory = tInv;
        merch.slots = Offer.fillInventory(tInv, merch.getOffers());

        //put merchant menu on map
        this.merchant.put(type, merch);
    }

    public Merchant getMerchant(ItemType type)
    {
        return this.merchant.get(type);
    }

    public void openTrading(Player player, ItemType type)
    {
        Merchant merchant = this.merchant.get(type);

        try
        {
            if (EggWars.getDB().getPlayerData(player).isClassicShop())
            {
                Villager villager = ReflectionUtils.<Villager>createEntity(player.getWorld(), player.getLocation().clone(), Villager.class, null);

                if (villager != null)
                {
                    String translatedTitle = TranslationUtils.getMessage(this.translationKey + ".title", player);
                    villager.setCustomName(translatedTitle);
                    villager.setRecipes(Offer.convertToRecipes(player, merchant.getOffers()));
                    player.openMerchant(villager, true);
                }
            }
            else
            {
                InventoryController.openInventory(player, merchant.inventory, MenuType.VILLAGER_TRADING).setExtraData(merchant.slots);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public boolean isEmpty()//do calculate if one merchant is empty, or if all are empty?
    {
        for (Merchant merch : this.merchant.values())
        {
            if (!merch.isEmpty())
            {
                return false;
            }
        }

        return true;
    }
}
