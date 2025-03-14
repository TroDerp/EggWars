package me.rosillogames.eggwars.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.language.Language;
import me.rosillogames.eggwars.language.LanguageManager;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.MenuPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class ProfileMenus
{
    private final TranslatableMenu mainMenu;
    private final TranslatableMenu statsMenu;
    private final TranslatableMenu settingsMenu;
    private final TranslatableMenu langsMenu;
    private final EwPlayer player;

    public ProfileMenus(EwPlayer playerIn)
    {
        this.player = playerIn;
        this.mainMenu = new TranslatableMenu(MenuType.MENU);
        this.statsMenu = new TranslatableMenu(MenuType.STATS);
        this.settingsMenu = new TranslatableMenu(MenuType.SETTINGS);
        this.langsMenu = new TranslatableMenu(MenuType.LANGUAGES);
    }

    public void loadGuis()
    {
        this.mainMenu.clearPages();
        TranslatableInventory tInv = new TranslatableInventory(MenuSize.FULL.getSlots(), "menu.main.title");
        tInv.setItem(11, this.getProfileItem());
        tInv.setItem(15, getSettingsItem());
        tInv.setItem(33, getKitsItem());
        tInv.setItem(tInv.getSize() - 5, ProfileMenus::getCloseItem);
        this.mainMenu.addPage(tInv);
        this.mainMenu.sendMenuUpdate(true);
        this.loadStatsGui();
        this.loadSettingsGui();
        this.loadLangGui();
    }

    public void loadStatsGui()
    {
        this.statsMenu.clearPages();
        TranslatableInventory tInv = new TranslatableInventory(MenuSize.FULL.getSlots(), "menu.stats.title");
        tInv.setItem(11, getStatItem(Material.DRAGON_EGG, StatType.EGGS_BROKEN));
        tInv.setItem(13, getStatItem(Material.IRON_SWORD, StatType.KILLS));
        tInv.setItem(15, getStatItem(Material.ANVIL, StatType.DEATHS));
        tInv.setItem(29, getStatItem(Material.APPLE, StatType.GAMES_PLAYED));
        tInv.setItem(31, getStatItem(Material.GOLDEN_APPLE, StatType.WINS));
        tInv.setItem(33, TranslatableItem.translatableNameLore(ItemUtils.makeMenuItem(new ItemStack(Material.EMERALD)), (player) ->
        {
            int points = PlayerUtils.getPoints(player);
            return TranslationUtils.getMessage("menu.stats.points.item_lore", player, points);
        }, (player) ->
        {
            int points = PlayerUtils.getPoints(player);
            return TranslationUtils.getMessage("menu.stats.points.item_name", player, points);
        }));
        tInv.setItem(tInv.getSize() - 5, ProfileMenus::getCloseItem);
        this.statsMenu.addPage(tInv);
        this.statsMenu.sendMenuUpdate(true);
    }

    public void loadSettingsGui()
    {
        this.settingsMenu.clearPages();
        TranslatableInventory tInv = new TranslatableInventory(MenuSize.BIG.getSlots(), "menu.settings.title");
        ItemStack stack = new ItemStack(Material.BEACON);
        ItemUtils.setOpensMenu(stack, MenuType.LANGUAGES);
        tInv.setItem(22, TranslatableItem.translatableNameLore(ItemUtils.makeMenuItem(stack), "menu.settings.languages.item_lore", "menu.settings.languages.item_name"));
        tInv.setItem(tInv.getSize() - 5, ProfileMenus::getCloseItem);
        this.settingsMenu.addPage(tInv);
        this.settingsMenu.sendMenuUpdate(true);
    }

    public void loadLangGui()
    {
        this.langsMenu.clearPages();
        List<Language> languages = new ArrayList();
        languages.add(LanguageManager.getDefaultLanguage());//add default language, and first
        languages.addAll(EggWars.languageManager().getLanguages().values());
        List<ProfileMenus.MenuSize> sizes = ProfileMenus.MenuSize.fromChestSize(languages.size());
        int counter = 0;
        int expected = 0;

        for (int pages = 0; pages < sizes.size(); ++pages)
        {
            MenuSize menusize = (MenuSize)sizes.get(pages);
            TranslatableInventory translatableinv = new TranslatableInventory(menusize.getSlots(), "menu.languages.title");
            Map<Integer, String> pageLangs = new HashMap();
            expected += menusize.getFilledSlots();

            for (int i = 0; i < menusize.getFilledSlots() && counter < languages.size() && counter <= expected; ++i, ++counter)
            {
                int slot = (9 * ((i / 7) + 1)) + ((i % 7) + 1);
                Language language = languages.get(counter);
                translatableinv.setItem(slot, this.getLanguageItem(language));
                pageLangs.put(slot, language.getLocale());
            }

            if (pages < sizes.size() - 1)
            {
                translatableinv.setItem(menusize.getSlots() - 1, ProfileMenus.getNextItem());
            }

            if (pages > 0)
            {
                translatableinv.setItem(menusize.getSlots() - 9, ProfileMenus.getPreviousItem());
            }

            translatableinv.setItem(menusize.getSlots() - 5, ProfileMenus::getCloseItem);
            this.langsMenu.addPage(translatableinv);
        }

        this.langsMenu.sendMenuUpdate(true);
    }

    private static Function<Player, ItemStack> getStatItem(Material mat, StatType type)
    {
        return (player) ->
        {
            ItemStack stack = ItemUtils.makeMenuItem(new ItemStack(mat));
            int stat = EggWars.getDB().getPlayerData(player).getTotalStat(type);
            TranslatableItem.setName(stack, TranslationUtils.getMessage("menu.stats." + type.name().toLowerCase() + ".item_name", player, stat));
            TranslatableItem.setLore(stack, TranslationUtils.getMessage("menu.stats." + type.name().toLowerCase() + ".item_lore", player, stat));
            return stack;
        };
    }

    public TranslatableItem getLanguageItem(Language language)
    {
        ItemStack stack = ItemUtils.makeMenuItem(new ItemStack(PlayerUtils.getLangId(this.player.getPlayer()).equals(language.getLocale()) ? Material.WRITTEN_BOOK : Material.WRITABLE_BOOK));
        SerializingItems.SELECT_LANGUAGE.setItemReference(stack, language.getLocale());

        if (language.equals(LanguageManager.getDefaultLanguage()))
        {
            return TranslatableItem.translatableNameLore(stack, "menu.languages.default.item_lore", "menu.languages.default.item_name");
        }

        return TranslatableItem.translatableNameLore(stack, (player) ->
        {
            return TranslationUtils.getMessage("language.region", language);
        }, (player) ->
        {
            return TranslationUtils.getMessage("language.name", language);
        });
    }

    public void openMainInv()
    {
        this.player.getMenuPlayer().openMenu(this.mainMenu);
    }

    public void openStatsInv()
    {
        this.player.getMenuPlayer().openMenu(this.statsMenu);
    }

    public void openSettingsInv()
    {
        this.player.getMenuPlayer().openMenu(this.settingsMenu);
    }

    public void openLanguageInv()
    {
        this.player.getMenuPlayer().openMenu(this.langsMenu);
    }

    public TranslatableItem getProfileItem()
    {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta meta = stack.getItemMeta();
        ((SkullMeta)meta).setOwningPlayer(this.player.getPlayer());
        stack.setItemMeta(meta);
        ItemUtils.setOpensMenu(stack, MenuType.STATS);
        return TranslatableItem.translatableNameLore(stack, "menu.profile.item_lore", "menu.profile.item_name");
    }

    public static TranslatableItem getSettingsItem()
    {
        ItemStack stack = new ItemStack(Material.MUSIC_DISC_STAL, 1);
        ItemUtils.setOpensMenu(stack, MenuType.LANGUAGES);
        return TranslatableItem.translatableNameLore(ItemUtils.makeMenuItem(stack), "menu.settings.item_lore", "menu.settings.item_name");
    }

    public static TranslatableItem getKitsItem()
    {
        ItemStack stack = new ItemStack(Material.PAPER, 1);
        ItemUtils.setOpensMenu(stack, MenuType.KIT_SELECTION);
        return TranslatableItem.translatableNameLore(ItemUtils.makeMenuItem(stack), "menu.kits.item_lore", "menu.kits.item_name");
    }

    //TODO When MenuPlayer gets added, the translation to player will detect if this is a "close" or "go back" button.
    public static ItemStack getCloseItem(Player player)
    {
        ItemStack stack = new ItemStack(Material.BOOK);
        SerializingItems.CLOSE_OR_BACK.setItemReference(stack, null);
        MenuPlayer menuply = PlayerUtils.getEwPlayer(player).getMenuPlayer();
        String key = menuply != null && !menuply.getParentTree().isEmpty() ? "back" : "close";
        TranslatableItem.setName(stack, TranslationUtils.getMessage("menu." + key + ".item_name", player));
        TranslatableItem.setLore(stack, TranslationUtils.getMessage("menu." + key + ".item_lore", player));
        return stack;
    }

    public static TranslatableItem getNextItem()
    {
        ItemStack stack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        SerializingItems.NEXT_PAGE.setItemReference(stack, null);
        return TranslatableItem.translatableNameLore(stack, "menu.next.item_lore", "menu.next.item_name");
    }

    public static TranslatableItem getPreviousItem()
    {
        ItemStack stack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        SerializingItems.PREVIOUS_PAGE.setItemReference(stack, null);
        return TranslatableItem.translatableNameLore(stack, "menu.previous.item_lore", "menu.previous.item_name");
    }

    public static TranslatableItem getClassicShopItem()
    {
        ItemStack stack = new ItemStack(Material.COMPARATOR);
        SerializingItems.CLASSIC_SHOP.setItemReference(stack, null);
        return TranslatableItem.translatableNameLore(stack, (player) ->
        {
            String s = TranslationUtils.getMessage(EggWars.getDB().getPlayerData(player).isClassicShop() ? "menu.settings.enabled" : "menu.settings.disabled", player);
            return TranslationUtils.getMessage("shop.classic_shop.desc", player, s);
        }, (player) ->
        {
            return TranslationUtils.getMessage("shop.classic_shop.name", player);
        });
    }

    public enum MenuSize
    {
        NORMAL("normal", 27, 7),
        MEDIUM("medium", 36, 14),
        BIG("big", 45, 21),
        FULL("full", 54, 28);

        private final String nameSpace;
        private final int slots;
        private final int filledSlots;

        private MenuSize(String nameSpaceIn, int slotsIn, int fillingSlotsIn)
        {
            this.nameSpace = nameSpaceIn;
            this.slots = slotsIn;
            this.filledSlots = fillingSlotsIn;
        }

        public int getSlots()
        {
            return this.slots;
        }

        public int getFilledSlots()
        {
            return this.filledSlots;
        }

        public static MenuSize fromSingleChestSize(int size)
        {
            for (MenuSize menusize : MenuSize.values())
            {
                if (size <= menusize.filledSlots)
                {
                    return menusize;
                }
            }

            return null;
        }

        public static List<MenuSize> fromChestSize(int size)
        {
            List<MenuSize> list = new ArrayList();

            if (size <= FULL.filledSlots)
            {
                list.add(fromSingleChestSize(size));
                return list;
            }

            int remaining = size;

            while (remaining > 0)
            {
                MenuSize menusize = fromSingleChestSize(Math.min(remaining, 28));
                remaining -= menusize.filledSlots;
                list.add(menusize);
            }

            return list;
        }

        public static MenuSize parse(String name, MenuSize fallback)
        {
            for (MenuSize menusize : MenuSize.values())
            {
                if (menusize.nameSpace.equals(name))
                {
                    return menusize;
                }
            }

            return fallback;
        }
    }
}
