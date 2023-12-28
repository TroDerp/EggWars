package me.rosillogames.eggwars.player.inventory;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.google.common.collect.Lists;
import me.rosillogames.eggwars.language.TranslationUtils;

public class TranslatableItem
{
    private final Translatable<ItemStack> item;
    @Nullable
    private Translatable<String> name;
    private boolean resetLore = true;
    private List<Translatable<String>> lore = Lists.<Translatable<String>>newArrayList();

    public TranslatableItem(ItemStack rawIn)
    {
        this.item = (player) -> rawIn;
        this.name = null;
    }

    public TranslatableItem(Translatable<ItemStack> itemIn)
    {
        this.item = itemIn;
        this.name = null;
    }

    public ItemStack getRaw()
    {
        return this.item.translate(null);
    }

    public void dontResetLore()
    {
        this.resetLore = false;
    }

    public void setName(Translatable<String> nameIn)
    {
        this.name = nameIn;
    }

    public void addLoreString(String lore, boolean translate)
    {
        if (translate)
        {
            this.lore.add((player) -> TranslationUtils.getMessage(lore, player));
        }
        else
        {
            this.lore.add((player) -> lore);
        }
    }

    public void addLoreTranslatable(Translatable<String> translatable)
    {
        this.lore.add(translatable);
    }

    public ItemStack getTranslated(Player player)
    {
        List<String> translatedLore = Lists.<String>newArrayList();

        for (Translatable<String> translatable : this.lore)
        {
            String translated = translatable.translate(player);

            if (translated.isEmpty())
            {
                continue;
            }

            translatedLore.addAll(Arrays.asList(translated.split("\\n")));
        }

        if (!this.resetLore)
        {
            List<String> fullLore = Lists.<String>newArrayList();
            ItemMeta meta = this.item.translate(player).getItemMeta();

            if (meta.getLore() != null)
            {
                for (String lore0 : meta.getLore())
                {
                    fullLore.add(lore0);
                }
            }

            for (String lore1 : translatedLore)
            {
                fullLore.add(lore1);
            }

            translatedLore = fullLore;
        }

        ItemStack stack = this.item.translate(player);
        ItemMeta meta = stack.getItemMeta();

        if (this.name != null)
        {
            meta.setDisplayName(this.name.translate(player));
        }

        meta.setLore(translatedLore);
        stack.setItemMeta(meta);
        return stack;
    }

    public TranslatableItem withRaw(ItemStack rawIn)
    {
        TranslatableItem translatableitem = new TranslatableItem(rawIn.clone());
        translatableitem.name = this.name;
        translatableitem.lore = this.lore;
        return translatableitem;
    }

    public boolean equalsItem(ItemStack stack, Player player)
    {
        return stack.equals(this.getTranslated(player));
    }

    public static TranslatableItem translatableName(ItemStack rawIn, String name)
    {
        TranslatableItem translatableitem = new TranslatableItem(rawIn.clone());
        translatableitem.setName((player) -> TranslationUtils.getMessage(name, player));
        return translatableitem;
    }

    public static TranslatableItem translatableNameLore(ItemStack rawIn, String lore, String name)
    {
        TranslatableItem translatableitem = new TranslatableItem(rawIn.clone());
        translatableitem.setName((player) -> TranslationUtils.getMessage(name, player));
        translatableitem.addLoreString(lore, true);
        return translatableitem;
    }

    public static TranslatableItem translatableNameLore(ItemStack rawIn, Translatable<String> lore, Translatable<String> name)
    {
        TranslatableItem translatableitem = new TranslatableItem(rawIn.clone());
        translatableitem.setName(name);
        translatableitem.addLoreTranslatable(lore);
        return translatableitem;
    }

    public static TranslatableItem fullTranslatable(Translatable<ItemStack> itemIn, Translatable<String> lore, Translatable<String> name)
    {
        TranslatableItem translatableitem = new TranslatableItem(itemIn);
        translatableitem.setName(name);
        translatableitem.addLoreTranslatable(lore);
        return translatableitem;
    }

    public static interface Translatable<T>
    {
        T translate(Player player);
    }
}
