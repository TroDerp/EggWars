package me.rosillogames.eggwars.arena.shop;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.AutoEquipEntry;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.objects.Token;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.Colorizer;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class Offer
{
    protected static final String INVENTORY_FULL_KEY = "shop.inventory_full";
    private final int slot;
    private final TradeResult result;
    protected final Price price;
    /**
     * This is used for classic shop, this can be set to true and it will not be displayed,
     * because the old GUI would be too large if we used too many ways to buy one same item
     * for different amounts.
     **/
    private final boolean noClassic;

    public Offer(int slotIn, TradeResult resultIn, Price priceIn, boolean noClassic)
    {
        this.slot = slotIn;
        this.result = resultIn;
        this.price = priceIn;
        this.noClassic = noClassic;
    }

    public ItemStack getDisplayItem(Player player)
    {
        return ItemUtils.makeMenuItem(this.getResultItem(player, this.result, true));
    }

    public boolean trade(Player player, boolean shift)
    {
        boolean hasBought = false;

        while (this.canAfford(player))
        {//stacks must be cloned one by one as well because they are "live" items from inventory
            ItemStack[] prev = ItemUtils.copyContents(player.getInventory().getContents());
            ItemStack stack = this.getResultItem(player, this.result, false);
            AutoEquipEntry autoEquip = this.result.getAutoEquip();
            EquipmentSlot slot;

            if (autoEquip != null && (slot = autoEquip.getTradeSlot(player)) != EquipmentSlot.HAND)
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
        return this.result.getResult().getMaxStackSize() > 1;
    }

    public boolean canAfford(Player player)
    {
        return Price.canAfford(player, this.price);
    }

    public int boughtAmount()
    {
        return this.result.getResult().getAmount();
    }

    public int affordingAmount(Player player)
    {
        return ItemUtils.countItems(player, this.price.getMaterial()) / this.price.getAmount();
    }

    public int priceOfFullAffording(int affording, Player player)
    {
        return affording * this.price.getAmount();
    }

    public Price getPrice()
    {
        return this.price;
    }

    public int getSlot()
    {
        return this.slot;
    }

    @Nullable
    public MerchantRecipe getAsRecipe(Player player)
    {
        MerchantRecipe recipe = null;

        if (!this.noClassic)
        {
            recipe = new MerchantRecipe(this.getResultItem(player, this.result, false), 0x7fffffff);
            recipe.setIngredients(Arrays.asList(new ItemStack(this.price.getMaterial(), this.price.getAmount())));
        }

        return recipe;
    }

    @SuppressWarnings("deprecation")
    protected ItemStack getResultItem(Player player, TradeResult result, boolean isDisplay)
    {
        ItemStack adjusted = result.getResult().clone();
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);

        if (ewplayer != null && result.isUsingTeamColor())
        {
            if (ewplayer.getArena() != null && ewplayer.getTeam() != null)
            {
                Colorizer.colorizeItem(adjusted, ewplayer.getTeam().getType().woolColor());
            }
        }

        if (adjusted.getItemMeta().isUnbreakable())
        {
            ItemMeta meta = adjusted.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            adjusted.setItemMeta(meta);
        }

        if (!isDisplay && !result.inheritsNameDesc())
        {
            return adjusted;
        }

        if (isDisplay)
        {
            if (EggWars.serverVersion.ordinal() >= Versions.V_1_20_R4.ordinal())
            {
                ReflectionUtils.setEnchantGlint(adjusted, this.canAfford(player), false);
            }
            else
            {
                List<String> list = ReflectionUtils.getEnchantmentsLore(adjusted);

                for (Enchantment ench : Enchantment.values())
                {
                    adjusted.removeEnchantment(ench);
                }

                ItemMeta meta = adjusted.getItemMeta();

                if (meta.hasLore())
                {
                    list.add("");
                    list.addAll(meta.getLore());
                }

                meta.setLore(list);
                adjusted.setItemMeta(meta);

                if (this.canAfford(player))
                {/* Works differently for older versions */
                    ReflectionUtils.setEnchantGlint(adjusted, true, false);
                }
            }
        }

        String descTranslation = result.getDescTranslation();

        if (!descTranslation.isEmpty() && TranslationUtils.messageExists(descTranslation, player))
        {
            TranslatableItem.addLoreNoReset(adjusted, TranslationUtils.getMessage(descTranslation, player));
        }

        String name = ReflectionUtils.getStackName(adjusted);
        String nameTranslation = result.getNameTranslation();

        if (!nameTranslation.isEmpty() && TranslationUtils.messageExists(nameTranslation, player))
        {
            name = TranslationUtils.getMessage(nameTranslation, player);
        }

        if (isDisplay)
        {
            Token priceToken = this.price.getToken();
            int first_param = this.boughtAmount();
            int second_param = this.price.getAmount();
            String third_param = TranslationUtils.getMessage(priceToken.getTypeName(), player);
            String fourth_param = this.canAfford(player) ? "§a✔" : "§c✘";
            int seventh_param = Price.amountOf(this.price, player);
            String eight_param = this.canAfford(player) ? "§6" : "§c";
            String ninth_param = priceToken.getColor().toString();

            if (this.stackable())
            {
                int i = this.affordingAmount(player);
                i = (i < 1 ? 1 : i);
                int fifth_param = this.boughtAmount() * i;
                int sixth_param = this.priceOfFullAffording(i, player);
                TranslatableItem.addLoreNoReset(adjusted, TranslationUtils.getMessage("shop.buy_item_stackable.desc", player, first_param, second_param, third_param, fourth_param, fifth_param, sixth_param, seventh_param, eight_param, ninth_param));
            }
            else
            {
                TranslatableItem.addLoreNoReset(adjusted, TranslationUtils.getMessage("shop.buy_item_unstackable.desc", player, first_param, second_param, third_param, fourth_param, seventh_param, eight_param, ninth_param));
            }

            TranslatableItem.setName(adjusted, TranslationUtils.getMessage("menu.item_title", player, name));
        }
        else
        {
            TranslatableItem.setName(adjusted, "§r" + name);
        }

        return adjusted;
    }
}
