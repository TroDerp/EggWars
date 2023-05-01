package me.rosillogames.eggwars.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.mojang.datafixers.util.Pair;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.loaders.KitLoader;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class KitsMenu
{
    private final List<Map<Integer, Kit>> kitsPerPage;
    private final List<TranslatableInventory> inventories;

    public KitsMenu()
    {
        this.kitsPerPage = new ArrayList();
        this.inventories = new ArrayList();
    }

    public void loadGui()
    {
        this.inventories.clear();
        this.kitsPerPage.clear();

        final List<Kit> kits = EggWars.getKitManager().getKits();
        int counter = 0;
        int pages = 0;
        int remaining = kits.size();

        while (remaining > 0)//count pages
        {
            remaining -= Math.min(remaining, 15);
            pages++;
        }

        for (int page = 0; page < pages; ++page)
        {
            TranslatableInventory translatableinv = new TranslatableInventory(EwPlayerMenu.MenuSize.FULL.getSlots(), "menu.kits.menu_title");
            Map<Integer, Kit> pageKits = new HashMap();
            boolean hasNineEntries = page == (pages - 1) && (kits.size() - counter) <= 9;
            int entries = hasNineEntries ? 9 : 15;

            for (int j = 0; j < entries && counter < kits.size() && j <= entries; ++j, ++counter)
            {
                Kit kit = kits.get(counter);
                int columns = entries / 3;
                int slotPos = 9 * ((j / columns) + 1) + (j % columns) + (hasNineEntries ? 3 : 2 /* this is for centering */);
                translatableinv.setItem(slotPos, getKitItem(kit));
                pageKits.put(slotPos, kit);
            }

            if (page < (pages - 1))
            {
                translatableinv.setItem(26, EwPlayerMenu.getNextItem());
            }

            if (page > 0)
            {
                translatableinv.setItem(18, EwPlayerMenu.getPreviousItem());
            }

            translatableinv.setItem(46, getDeselectItem());
            translatableinv.setItem(49, EwPlayerMenu.getCloseItem());
            translatableinv.setItem(52, getPointsItem());
            this.inventories.add(page, translatableinv);
            this.kitsPerPage.add(page, pageKits);
        }
    }

    @Nullable
    public Kit getKit(int page, int slot)
    {
        return this.kitsPerPage.get(page).get(slot);
    }

    public TranslatableInventory getInventory(int page)
    {
        return this.inventories.get(page);
    }

    public static TranslatableItem getDeselectItem()
    {
        return TranslatableItem.fullTranslatable((player) ->
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);
            ItemStack stack = new ItemStack(Material.BARRIER);

            if (ewplayer.getKit() == null)
            {
                ItemMeta meta = stack.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                stack.setItemMeta(meta);
                stack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);
            }

            return stack;
        }, (player) -> TranslationUtils.getMessage("menu.kits.deselect.item_lore", player), (player) -> TranslationUtils.getMessage("menu.kits.deselect.item_name", player));
    }

    private static TranslatableItem getKitItem(Kit kit)
    {
        return TranslatableItem.fullTranslatable((player) ->
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);
            ItemStack stack = kit.displayItem().clone();

            for (Enchantment ench : Enchantment.values())
            {
                stack.removeEnchantment(ench);
            }

            if (ewplayer.getKit() == kit)
            {
                ItemMeta meta = stack.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                stack.setItemMeta(meta);
                stack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);
            }

            return stack;
        }, (player) ->
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);
            String kit_desc = TranslationUtils.getMessage("menu.kits.kit_lore.desc", player, TranslationUtils.getMessage(kit.getDesc(), player));
            String lock;

            if (ewplayer.hasKit(kit))
            {
                lock = TranslationUtils.getMessage("menu.kits.kit_lore.unlocked", player, TranslationUtils.translateTime(player, EggWars.getDB().getPlayerData(player).timeSinceKit(kit.id()), false));
            }
            else
            {
                lock = TranslationUtils.getMessage("menu.kits.kit_lore.price", player, ((KitLoader.canBuy(ewplayer, kit) ? "&a" : "&c") + kit.price()));
            }

            String contents = "";

            for (Pair<EquipmentSlot, ItemStack> pair : kit.items())
            {
                String itemName = ReflectionUtils.getStackName(pair.getSecond());
                contents = contents + TranslationUtils.getMessage("menu.kits.kit_lore.content", player, pair.getSecond().getAmount(), itemName, (!pair.getSecond().getEnchantments().isEmpty() ? TranslationUtils.getMessage("menu.kits.kit_lore.enchanted", player) : ""));
            }

            String selection = TranslationUtils.getMessage("menu.kits.kit_lore." + (ewplayer.getKit() == kit ? "selected" : ewplayer.hasKit(kit) ? "select" : KitLoader.canBuy(ewplayer, kit) ? "buy" : "cant_afford"), player);
            return TranslationUtils.getMessage("menu.kits.kit_lore.full", player, kit_desc, lock, contents, selection);
        }, (player) -> TranslationUtils.getMessage("menu.item_title", player, TranslationUtils.getMessage(kit.getName(), player)));
    }

    private static TranslatableItem getPointsItem()
    {
        return TranslatableItem.translatableNameLore(new ItemStack(Material.SUNFLOWER), (player) ->
        {
            int points = PlayerUtils.getEwPlayer(player).getPoints();
            return TranslationUtils.getMessage("menu.kits.points.item_lore", player, points);
        }, (player) -> TranslationUtils.getMessage("menu.kits.points.item_name", player));
    }
}
