package me.rosillogames.eggwars.player.inventory;

import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import com.google.common.collect.Maps;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.inventory.TranslatableItem.Translatable;

public class TranslatableInventory
{
    private Map<Integer, TranslatableItem> contents = Maps.<Integer, TranslatableItem>newHashMap();
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

    public void setItem(int slot, TranslatableItem item)
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
    public TranslatableItem getItem(int slot)
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

        for (Map.Entry<Integer, TranslatableItem> entry : this.contents.entrySet())
        {
            mcInventory.setItem(entry.getKey(), entry.getValue().getTranslated(player));
        }

        return mcInventory;
    }

    public Map<Integer, TranslatableItem> getContents()
    {
        return this.contents;
    }
}
