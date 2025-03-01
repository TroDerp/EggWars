package me.rosillogames.eggwars.arena.shop;

import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.AutoEquipEntry;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.utils.ItemUtils;

public class MultiOffer extends Offer
{
    protected final List<TradeResult> results;

    public MultiOffer(int slot, TradeResult displayItem, List<TradeResult> results, Price price)
    {
        super(slot, displayItem, price, true);
        this.results = results;
    }

    @Override
    public boolean trade(Player player, boolean shift)
    {
        if (!this.canAfford(player))
        {
            TranslationUtils.sendMessage("shop.not_enough_items", player, Price.leftFor(this.price, player), this.price.getToken().getFormattedName(player));
            return false;
        }

        ItemStack[] prev = ItemUtils.copyContents(player.getInventory().getContents());

        for (TradeResult result : this.results)
        {
            ItemStack stack = this.getResultItem(player, result, false);
            AutoEquipEntry autoEquip = result.getAutoEquip();
            EquipmentSlot slot;

            if (autoEquip != null && (slot = autoEquip.getTradeSlot(player)) != EquipmentSlot.HAND)
            {
                ItemStack slotItem = ItemUtils.getSlot(player, slot);

                if (slotItem != null)
                {
                    //slotItem is the item that already was on slot
                    Map<Integer, ItemStack> map = player.getInventory().addItem(slotItem);

                    if (!map.isEmpty())
                    {
                        player.getInventory().setContents(prev);
                        TranslationUtils.sendMessage(INVENTORY_FULL_KEY, player);
                        return false;
                    }
                }

                ItemUtils.setToSlot(player, slot, stack);
                continue;
            }

            Map<Integer, ItemStack> map = player.getInventory().addItem(stack);

            if (!map.isEmpty())
            {
                player.getInventory().setContents(prev);
                TranslationUtils.sendMessage(INVENTORY_FULL_KEY, player);
                return false;
            }
        }

        Price.sellItems(player, this.price);
        return true;
    }

    @Override
    public boolean stackable()
    {
        return false;
    }
}
