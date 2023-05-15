package me.rosillogames.eggwars.arena.shop;

import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import com.mojang.datafixers.util.Pair;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.utils.ItemUtils;

public class MultiOffer extends Offer
{
    private final String name;
    protected final List<Pair<Boolean, ItemStack>> results;

    public MultiOffer(int slot, String name, List<Pair<Boolean, ItemStack>> results, ItemStack itemResult, Price price)
    {
        super(slot, itemResult, price, true);
        this.name = name;
        this.results = results;
    }

    public String getName()
    {
        return this.name;
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

        for (Pair<Boolean, ItemStack> result : this.results)
        {
            ItemStack stack = Offer.adjustForRecipe(player, result.getSecond(), result.getFirst());
            EquipmentSlot slot = ItemUtils.getTradeSlot(player, stack);

            if (slot != EquipmentSlot.HAND)
            {
                ItemStack slotItem;

                if ((slotItem = ItemUtils.getSlot(player, slot)) != null)
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
