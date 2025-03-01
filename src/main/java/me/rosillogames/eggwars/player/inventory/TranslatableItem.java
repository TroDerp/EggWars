package me.rosillogames.eggwars.player.inventory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.google.common.collect.Lists;
import me.rosillogames.eggwars.language.TranslationUtils;

public class TranslatableItem implements Function<Player, ItemStack>/* This is fruitin' genius! */
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

    public void setNameTranslatable(Translatable<String> nameIn)
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

    /**
     * Builds and translates this item to the given player argument language.
     *
     * @param player the bukkit player instance
     * @return the built item stack
     */
    @Override
    public ItemStack apply(Player player)
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

        ItemStack stack = this.item.translate(player);

        if (!this.resetLore)
        {
            List<String> fullLore = Lists.<String>newArrayList();
            ItemMeta meta = stack.getItemMeta();

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
        return stack.equals(this.apply(player));
    }

    public static TranslatableItem translatableNameLore(ItemStack rawIn, String lore, String name)
    {
        TranslatableItem translatableitem = new TranslatableItem(rawIn.clone());
        translatableitem.setNameTranslatable((player) -> TranslationUtils.getMessage(name, player));
        translatableitem.addLoreString(lore, true);
        return translatableitem;
    }

    public static TranslatableItem translatableNameLore(ItemStack rawIn, Translatable<String> lore, Translatable<String> name)
    {
        TranslatableItem translatableitem = new TranslatableItem(rawIn.clone());
        translatableitem.setNameTranslatable(name);
        translatableitem.addLoreTranslatable(lore);
        return translatableitem;
    }

    public static TranslatableItem fullTranslatable(Translatable<ItemStack> itemIn, Translatable<String> lore, Translatable<String> name)
    {
        TranslatableItem translatableitem = new TranslatableItem(itemIn);
        translatableitem.setNameTranslatable(name);
        translatableitem.addLoreTranslatable(lore);
        return translatableitem;
    }

    public static void setName(ItemStack stackIn, String nameIn)
    {
        ItemMeta meta = stackIn.getItemMeta();

        if (nameIn != null && !nameIn.isEmpty())
        {
            meta.setDisplayName(nameIn);
        }

        stackIn.setItemMeta(meta);
    }

    private static List<String> getLoreList(String... loreIn)
    {
        List<String> loreOut = Lists.<String>newArrayList();

        for (String line : loreIn)
        {
            if (line.isEmpty())
            {
                continue;
            }

            loreOut.addAll(Arrays.asList(line.split("\\n")));
        }

        return loreOut;
    }

    public static void setLore(ItemStack stackIn, String... loreIn)
    {
        ItemMeta meta = stackIn.getItemMeta();
        meta.setLore(getLoreList(loreIn));
        stackIn.setItemMeta(meta);
    }

    public static void addLoreNoReset(ItemStack stackIn, String... loreIn)
    {
        List<String> fullLore = Lists.<String>newArrayList();
        ItemMeta meta = stackIn.getItemMeta();

        if (meta.getLore() != null)
        {
            for (String lore0 : meta.getLore())
            {
                fullLore.add(lore0);
            }
        }

        fullLore.addAll(getLoreList(loreIn));
        meta.setLore(fullLore);
        stackIn.setItemMeta(meta);
    }

    public static ItemStack fullyTranslate(ItemStack rawIn, String name, String lore, Player player)
    {
        ItemStack clone = rawIn.clone();
        ItemMeta rawMeta = clone.getItemMeta();
        rawMeta.setDisplayName(TranslationUtils.getMessage(name, player));
        String tLore = TranslationUtils.getMessage(lore, player);

        if (!tLore.isEmpty())
        {
            rawMeta.setLore(Arrays.asList(TranslationUtils.getMessage(tLore, player).split("\\n")));
        }
        else
        {
            rawMeta.setLore(Lists.<String>newArrayList());//resets lore
        }

        clone.setItemMeta(rawMeta);
        return clone;
    }

    public static interface Translatable<T>
    {
        T translate(Player player);
    }
}
