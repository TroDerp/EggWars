package me.rosillogames.eggwars.menu;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.managers.KitManager;
import me.rosillogames.eggwars.objects.Kit;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.Pair;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class KitsMenu
{
    private final TranslatableMenu menu;

    public KitsMenu()
    {
        this.menu = new TranslatableMenu(MenuType.KIT_SELECTION);
    }

    public void loadGui()
    {
        this.menu.clearPages();

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
            TranslatableInventory translatableinv = new TranslatableInventory(ProfileMenus.MenuSize.FULL.getSlots(), "menu.kits.menu_title");
            boolean hasNineEntries = page == (pages - 1) && (kits.size() - counter) <= 9;
            int entries = hasNineEntries ? 9 : 15;

            for (int j = 0; j < entries && counter < kits.size() && j <= entries; ++j, ++counter)
            {
                Kit kit = kits.get(counter);
                int columns = entries / 3;
                int slotPos = 9 * ((j / columns) + 1) + (j % columns) + (hasNineEntries ? 3 : 2 /* this is for centering */);
                translatableinv.setItem(slotPos, getKitItem(kit));
            }

            if (page < (pages - 1))
            {
                translatableinv.setItem(26, ProfileMenus.getNextItem());
            }

            if (page > 0)
            {
                translatableinv.setItem(18, ProfileMenus.getPreviousItem());
            }

            translatableinv.setItem(46, getDeselectItem());
            translatableinv.setItem(49, ProfileMenus.getCloseItem());
            translatableinv.setItem(52, getPointsItem());
            this.menu.addPage(translatableinv);
        }

        this.menu.sendMenuUpdate(true);
    }

    public void openKitsMenu(EwPlayer player)
    {
        this.menu.addOpener(player);
    }

    public static TranslatableItem getDeselectItem()
    {
        return TranslatableItem.fullTranslatable((player) ->
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);
            ItemStack stack = new ItemStack(Material.BARRIER);

            if (ewplayer.getKit() == null)
            {
                ReflectionUtils.setEnchantGlint(stack, true, false);
            }

            SerializingItems.SELECT_KIT.setItemReference(stack, null);
            return stack;
        }, (player) -> TranslationUtils.getMessage("menu.kits.deselect.item_lore", player), (player) -> TranslationUtils.getMessage("menu.kits.deselect.item_name", player));
    }

    private static TranslatableItem getKitItem(Kit kit)
    {
        return TranslatableItem.fullTranslatable((player) ->
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);
            ItemStack stack = kit.displayItem().clone();

            for (Enchantment ench : stack.getEnchantments().keySet())
            {
                stack.removeEnchantment(ench);
            }

            if (ewplayer.getKit() == kit)
            {
                ReflectionUtils.setEnchantGlint(stack, true, false);
            }

            SerializingItems.SELECT_KIT.setItemReference(stack, kit);
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
                lock = TranslationUtils.getMessage("menu.kits.kit_lore.price", player, ((KitManager.canBuy(ewplayer, kit) ? "&a" : "&c") + kit.price()));
            }

            String contents = "";

            for (Pair<EquipmentSlot, ItemStack> pair : kit.items())
            {
                String itemName = ReflectionUtils.getStackName(pair.getRight());
                contents = contents + TranslationUtils.getMessage("menu.kits.kit_lore.content", player, pair.getRight().getAmount(), itemName, (!pair.getRight().getEnchantments().isEmpty() ? TranslationUtils.getMessage("menu.kits.kit_lore.enchanted", player) : ""));
            }

            String selection = TranslationUtils.getMessage("menu.kits.kit_lore." + (ewplayer.getKit() == kit ? "selected" : ewplayer.hasKit(kit) ? "select" : KitManager.canBuy(ewplayer, kit) ? "buy" : "cant_afford"), player);
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
