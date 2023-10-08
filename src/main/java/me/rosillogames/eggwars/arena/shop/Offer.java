package me.rosillogames.eggwars.arena.shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.objects.Token;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.Colorizer;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class Offer
{
    protected static final String INVENTORY_FULL_KEY = "shop.inventory_full";
    private final int slot;
    private final ItemStack result;
    public boolean colorize;
    protected final Price price;
    /**
     * This is used for classic shop, this can be set to true and it will not be displayed,
     * because the old GUI would be too large if we used too many ways to buy one same item
     * for different amounts.
     **/
    private final boolean noClassic;

    public Offer(int slotIn, ItemStack resultIn, Price priceIn, boolean noClassic)
    {
        this.slot = slotIn;
        this.result = resultIn;
        this.price = priceIn;
        this.noClassic = noClassic;
    }

    public ItemStack getDisplayItem(Player player)
    {
        return ItemUtils.hideStackAttributes(adjustForRecipe(player, this.result, this.colorize));
    }

    public boolean trade(Player player, boolean shift)
    {
        boolean hasBought = false;

        while (this.canAfford(player))
        {//stacks must be cloned one by one as well because they are "live" items from inventory
            ItemStack[] prev = ItemUtils.copyContents(player.getInventory().getContents());
            ItemStack stack = adjustForRecipe(player, this.result, this.colorize);
            EquipmentSlot slot = ItemUtils.getTradeSlot(player, stack);

            if (slot != EquipmentSlot.HAND)
            {
                ItemStack slotItem;

                if ((slotItem = ItemUtils.getSlot(player, slot)) != null)
                {
                    /*
                     * REMEMBER addItem returns items that were not added, which means that if one item was given
                     * and returns one, it clearly means that the item was not added, however this complicates when
                     * you give an item stack with amount grater than one, then if the stack on the inv is not fully
                     * complete but there isn't enough space for the offered amount then it would give free items till
                     * the stack is full. This is why we have to revert all inv changes by storing previous value.
                     */
                    Map<Integer, ItemStack> map = player.getInventory().addItem(slotItem);

                    if (!map.isEmpty())
                    {
                        player.getInventory().setContents(prev);
                        TranslationUtils.sendMessage(INVENTORY_FULL_KEY, player);
                        return false;
                    }
                }

                ItemUtils.setToSlot(player, slot, stack);
                Price.sellItems(player, this.price);
                return true;
            }

            Map<Integer, ItemStack> map = player.getInventory().addItem(stack);

            if (!map.isEmpty())
            {
                player.getInventory().setContents(prev);
                TranslationUtils.sendMessage(INVENTORY_FULL_KEY, player);
                return false;
            }

            Price.sellItems(player, this.price);
            hasBought = true;

            if (shift && this.stackable())//if true it will try keep buying items
            {
                continue;
            }

            break;
        }

        if (!hasBought)
        {
            TranslationUtils.sendMessage("shop.not_enough_items", player, Price.leftFor(this.price, player), this.price.getToken().getFormattedName(player));
        }

        return hasBought;
    }

    public boolean stackable()
    {
        return this.result.getMaxStackSize() > 1;
    }

    public boolean canAfford(Player player)
    {
        return Price.canAfford(player, this.price);
    }

    public int boughtAmount()
    {
        return this.result.getAmount();
    }

    public int affordingAmount(Player player)
    {
        return ItemUtils.countItems(player, this.price.getMaterial()) / this.price.getAmount();
    }

    public int priceOfFullAffording(int affording, Player player)
    {
        return affording * this.price.getAmount();
    }

    public int amountOfPrice(Player player)
    {
        return Price.amountOf(this.price, player);
    }

    public static Map<Integer, Offer> fillInventory(TranslatableInventory translatableInv, List<Offer> merchantOffers)
    {
        Map<Integer, Offer> map = new HashMap();

        for (Offer offer : merchantOffers)
        {
            TranslatableItem tItem = TranslatableItem.fullTranslatable((player1) ->
            {
                ItemStack stack = offer.getDisplayItem(player1).clone();
                List<String> list = ReflectionUtils.getEnchantmentsLore(stack);

                for (Enchantment ench : Enchantment.values())
                {
                    stack.removeEnchantment(ench);
                }

                ItemMeta meta = stack.getItemMeta();
                meta.setLore(list);

                if (offer.canAfford(player1))
                {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                stack.setItemMeta(meta);

                if (offer.canAfford(player1))
                {
                    stack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);
                }

                return stack;
            }, (player1) ->
            {
                Token priceToken = offer.price.getToken();
                int first_param = offer.boughtAmount();
                int second_param = offer.price.getAmount();
                String third_param = TranslationUtils.getMessage(priceToken.getTypeName(), player1);
                String fourth_param = offer.canAfford(player1) ? "§a✔" : "§c✘";
                int i = offer.affordingAmount(player1);
                i = (i < 1 ? 1 : i);
                int fifth_param = offer.boughtAmount() * i;
                int sixth_param = offer.priceOfFullAffording(i, player1);
                int seventh_param = offer.amountOfPrice(player1);
                String eight_param = offer.canAfford(player1) ? "§6" : "§c";
                String ninth_param = priceToken.getColor().toString();
                return offer.stackable() ? TranslationUtils.getMessage("shop.buy_item_stackable.desc", player1, first_param, second_param, third_param, fourth_param, fifth_param, sixth_param, seventh_param, eight_param, ninth_param) : TranslationUtils.getMessage("shop.buy_item_unstackable.desc", player1, first_param, second_param, third_param, fourth_param, seventh_param, eight_param, ninth_param);
            }, (player1) ->
            {
                return TranslationUtils.getMessage("menu.item_title", player1, (offer instanceof MultiOffer && ((MultiOffer)offer).getName() != null ? ((MultiOffer)offer).getName() : ReflectionUtils.getStackName(offer.getDisplayItem(player1))));
            });
            tItem.dontResetLore();
            translatableInv.setItem(offer.slot, tItem);
            map.put(offer.slot, offer);
        }

        return map;
    }

    public static List<MerchantRecipe> convertToRecipes(Player player, List<Offer> offers)
    {
        List<MerchantRecipe> recipeList = new ArrayList();

        for (Offer offer : offers)
        {
            if (offer.noClassic)
            {
                continue;
            }

            MerchantRecipe recipe = new MerchantRecipe(adjustForRecipe(player, offer.result, offer.colorize), 0x7fffffff);
            recipe.setIngredients(Arrays.asList(new ItemStack(offer.price.getMaterial(), offer.price.getAmount())));
            recipeList.add(recipe);
        }

        return recipeList;
    }

    public static ItemStack adjustForRecipe(Player player, ItemStack original, boolean colorize)
    {
        ItemStack colored = original.clone();

        if (player != null && colorize)
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);

            if (ewplayer.getArena() != null && ewplayer.getTeam() != null)
            {
                Colorizer.colorizeItem(colored, ewplayer.getTeam().getType().woolColor());
            }
        }

        if (colored.getItemMeta().isUnbreakable())
        {
            ItemMeta meta = colored.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            colored.setItemMeta(meta);
        }

        return colored;
    }
}
