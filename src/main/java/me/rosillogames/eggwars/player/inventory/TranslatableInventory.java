package me.rosillogames.eggwars.player.inventory;

import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.Maps;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.inventory.TranslatableItem.Translatable;

public class TranslatableInventory
{
    private Map<Integer, Function<Player, ItemStack>> contents = Maps.<Integer, Function<Player, ItemStack>>newHashMap();
    private int size;
    private Translatable<String> title;

    public TranslatableInventory(int sizeIn, String translatableTitleIn)
    {
        this.size = sizeIn;
        this.title = (player) -> TranslationUtils.getMessage(translatableTitleIn, player);
    }

    public TranslatableInventory(int sizeIn, Translatable titleIn)
    {
        this.size = sizeIn;
        this.title = titleIn;
    }

    public void setItem(int slot, Function<Player, ItemStack> item)
    {
        if (slot < 0 || slot >= this.size)
        {
            return;
        }

        if (item == null)
        {
            this.contents.remove(slot);
            return;
        }

        this.contents.put(slot, item);
    }

    @Nullable
    public Function<Player, ItemStack> getItem(int slot)
    {
        return this.contents.get(slot);
    }

    public int getSize()
    {
        return this.size;
    }

    public void clear()
    {
        this.contents.clear();
    }

    public Inventory getTranslatedInventory(Player player)
    {
        Inventory mcInventory = Bukkit.createInventory(null, this.size, this.title.translate(player));

        for (Map.Entry<Integer, Function<Player, ItemStack>> entry : this.contents.entrySet())
        {
            mcInventory.setItem(entry.getKey(), entry.getValue().apply(player));
        }

        return mcInventory;
    }

    public Map<Integer, Function<Player, ItemStack>> getContents()
    {
        return this.contents;
    }
}
