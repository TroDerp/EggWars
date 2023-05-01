package me.rosillogames.eggwars.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.language.Language;
import me.rosillogames.eggwars.language.LanguageManager;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.inventory.EwInvType;
import me.rosillogames.eggwars.player.inventory.InventoryController;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class EwPlayerMenu
{
    private TranslatableInventory mainInv;
    private TranslatableInventory statsInv;
    private TranslatableInventory settingsInv;
    private final List<TranslatableInventory> langInvs = new ArrayList<TranslatableInventory>();
    private final List<Map<Integer, Language>> langsPerPage = new ArrayList<Map<Integer, Language>>();
    private final EwPlayer player;

    public EwPlayerMenu(EwPlayer playerIn)
    {
        this.player = playerIn;
    }

    public void loadGuis()
    {
        this.mainInv = new TranslatableInventory(MenuSize.FULL.getSlots(), "menu.main.title");
        this.mainInv.setItem(11, this.getProfileItem());
        this.mainInv.setItem(15, getSettingsItem());
        this.mainInv.setItem(33, getKitsItem());
        this.mainInv.setItem(MenuSize.FULL.getSlots() - 5, getCloseItem());
        this.loadStatsGui();
        this.loadSettingsGui();
        this.loadLangGui();
    }

    public void loadStatsGui()
    {
        this.statsInv = new TranslatableInventory(MenuSize.FULL.getSlots(), "menu.stats.title");
        this.statsInv.setItem(11, TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(new ItemStack(Material.DRAGON_EGG)), (player) ->
        {
            int eggs = EggWars.getDB().getPlayerData(player).getTotalStat(StatType.EGGS_BROKEN);
            return TranslationUtils.getMessage("menu.stats.eggs.item_lore", player, eggs);
        }, (player) ->
        {
            int eggs = EggWars.getDB().getPlayerData(player).getTotalStat(StatType.EGGS_BROKEN);
            return TranslationUtils.getMessage("menu.stats.eggs.item_name", player, eggs);
        }));
        this.statsInv.setItem(13, TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(new ItemStack(Material.IRON_SWORD)), (player) ->
        {
            int kills = EggWars.getDB().getPlayerData(player).getTotalStat(StatType.KILLS);
            return TranslationUtils.getMessage("menu.stats.kills.item_lore", player, kills);
        }, (player) ->
        {
            int kills = EggWars.getDB().getPlayerData(player).getTotalStat(StatType.KILLS);
            return TranslationUtils.getMessage("menu.stats.kills.item_name", player, kills);
        }));
        this.statsInv.setItem(15, TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(new ItemStack(Material.ANVIL)), (player) ->
        {
            int deaths = EggWars.getDB().getPlayerData(player).getTotalStat(StatType.DEATHS);
            return TranslationUtils.getMessage("menu.stats.deaths.item_lore", player, deaths);
        }, (player) ->
        {
            int deaths = EggWars.getDB().getPlayerData(player).getTotalStat(StatType.DEATHS);
            return TranslationUtils.getMessage("menu.stats.deaths.item_name", player, deaths);
        }));
        this.statsInv.setItem(29, TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(new ItemStack(Material.APPLE)), (player) ->
        {
            int played = EggWars.getDB().getPlayerData(player).getTotalStat(StatType.GAMES_PLAYED);
            return TranslationUtils.getMessage("menu.stats.played.item_lore", player, played);
        }, (player) ->
        {
            int played = EggWars.getDB().getPlayerData(player).getTotalStat(StatType.GAMES_PLAYED);
            return TranslationUtils.getMessage("menu.stats.played.item_name", player, played);
        }));
        this.statsInv.setItem(31, TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(new ItemStack(Material.GOLDEN_APPLE)), (player) ->
        {
            int wins = EggWars.getDB().getPlayerData(player).getTotalStat(StatType.WINS);
            return TranslationUtils.getMessage("menu.stats.wins.item_lore", player, wins);
        }, (player) ->
        {
            int wins = EggWars.getDB().getPlayerData(player).getTotalStat(StatType.WINS);
            return TranslationUtils.getMessage("menu.stats.wins.item_name", player, wins);
        }));
        this.statsInv.setItem(33, TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(new ItemStack(Material.EMERALD)), (player) ->
        {
            int points = PlayerUtils.getEwPlayer(player).getPoints();
            return TranslationUtils.getMessage("menu.stats.points.item_lore", player, points);
        }, (player) ->
        {
            int points = PlayerUtils.getEwPlayer(player).getPoints();
            return TranslationUtils.getMessage("menu.stats.points.item_name", player, points);
        }));
        this.statsInv.setItem(this.statsInv.getSize() - 5, getCloseItem());
    }

    public TranslatableInventory loadSettingsGui()
    {
        this.settingsInv = new TranslatableInventory(MenuSize.BIG.getSlots(), "menu.settings.title");
        this.settingsInv.setItem(22, TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(new ItemStack(Material.BEACON)), "menu.settings.languages.item_lore", "menu.settings.languages.item_name"));
        this.settingsInv.setItem(MenuSize.BIG.getSlots() - 5, getCloseItem());
        return this.settingsInv;
    }

    public void loadLangGui()
    {
        this.langInvs.clear();
        this.langsPerPage.clear();
        List<Language> languages = new ArrayList();
        languages.add(LanguageManager.getDefaultLanguage());//add default language, and first
        languages.addAll(EggWars.languageManager().getLanguages().values());
        List<EwPlayerMenu.MenuSize> sizes = EwPlayerMenu.MenuSize.fromChestSize(languages.size());
        int counter = 0;
        int expected = 0;

        for (int pages = 0; pages < sizes.size(); ++pages)
        {
            MenuSize menusize = (MenuSize)sizes.get(pages);
            TranslatableInventory translatableinv = new TranslatableInventory(menusize.getSlots(), "menu.languages.title");
            Map<Integer, Language> pageLangs = new HashMap();
            expected += menusize.getFilledSlots();

            for (int i = 0; i < menusize.getFilledSlots() && counter < languages.size() && counter <= expected; ++i, ++counter)
            {
                int slot = (9 * ((i / 7) + 1)) + ((i % 7) + 1);
                Language language = languages.get(counter);
                translatableinv.setItem(slot, this.getLanguageItem(language));
                pageLangs.put(slot, language);
            }

            if (pages < sizes.size() - 1)
            {
                translatableinv.setItem(menusize.getSlots() - 1, EwPlayerMenu.getNextItem());
            }

            if (pages > 0)
            {
                translatableinv.setItem(menusize.getSlots() - 9, EwPlayerMenu.getPreviousItem());
            }

            translatableinv.setItem(menusize.getSlots() - 5, getCloseItem());
            this.langInvs.add(pages, translatableinv);
            this.langsPerPage.add(pages, pageLangs);
        }
    }

    @Nullable
    public Language getLang(int page, int slot)
    {
        return this.langsPerPage.get(page).get(slot);
    }

    public TranslatableItem getLanguageItem(Language language)
    {
        String currentlocale = EggWars.getDB().getPlayerData(this.player.getPlayer()).getLocale();
        ItemStack stack = ItemUtils.hideStackAttributes(new ItemStack(currentlocale.equals(language.getLocale()) ? Material.WRITTEN_BOOK : Material.WRITABLE_BOOK));

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
        InventoryController.openInventory(this.player.getPlayer(), this.mainInv, EwInvType.MENU);
    }

    public void openStatsInv()
    {
        InventoryController.openInventory(this.player.getPlayer(), this.statsInv, EwInvType.STATS);
    }

    public void openSettingsInv()
    {
        InventoryController.openInventory(this.player.getPlayer(), this.settingsInv, EwInvType.SETTINGS);
    }

    public void openLanguageInv(int page)
    {
        InventoryController.openInventory(this.player.getPlayer(), this.langInvs.get(page), EwInvType.LANGUAGES).setExtraData(Integer.valueOf(page));
    }

    public TranslatableItem getProfileItem()
    {
        ItemStack itemstack = ItemUtils.getItemOrDefault("{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:\"" + this.player.getPlayer().getName() + "\"}}", Material.SKELETON_SKULL);
        return TranslatableItem.translatableNameLore(itemstack, "menu.profile.item_lore", "menu.profile.item_name");
    }

    public static TranslatableItem getSettingsItem()
    {
        return TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(new ItemStack(Material.MUSIC_DISC_STAL)), "menu.settings.item_lore", "menu.settings.item_name");
    }

    public static TranslatableItem getKitsItem()
    {
        //TODO: translations
        return TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(new ItemStack(Material.PAPER)), "menu.kits.item_lore", "menu.kits.item_name");
    }

    public static TranslatableItem getCloseItem()
    {
        return TranslatableItem.translatableNameLore(new ItemStack(Material.BOOK), "menu.close.item_lore", "menu.close.item_name");
    }

    public static TranslatableItem getNextItem()
    {
        return TranslatableItem.translatableNameLore(new ItemStack(Material.LIME_STAINED_GLASS_PANE), "menu.next.item_lore", "menu.next.item_name");
    }

    public static TranslatableItem getPreviousItem()
    {
        return TranslatableItem.translatableNameLore(new ItemStack(Material.RED_STAINED_GLASS_PANE), "menu.previous.item_lore", "menu.previous.item_name");
    }

    public static TranslatableItem getClassicShopItem()
    {
        return TranslatableItem.translatableNameLore(new ItemStack(Material.COMPARATOR), (player) ->
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
