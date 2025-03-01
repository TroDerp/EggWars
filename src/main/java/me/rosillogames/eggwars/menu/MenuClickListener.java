package me.rosillogames.eggwars.menu;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.managers.KitManager;
import me.rosillogames.eggwars.objects.Kit;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.ItemUtils;

public interface MenuClickListener
{
    public void listenClick(InventoryClickEvent event, EwPlayer player, EwMenu menu);

    public static boolean listenGeneric(InventoryClickEvent event, EwPlayer player, EwMenu menu)
    {//What if every serialized item is its own listener? with context param (MenuContext, UseContext...)
        ItemStack currItem = event.getCurrentItem();
        SerializingItems type = SerializingItems.getReferenceType(currItem);

        if (SerializingItems.CLOSE_OR_BACK.equals(type))
        {
            event.setCurrentItem(null);
            menu.closeForOpener(player);
            return true;
        }

        if (menu.isPagedMenu())
        {
            if (SerializingItems.PREVIOUS_PAGE.equals(type))
            {
                player.setMenuPage(player.getMenuPage() - 1);
                event.setCurrentItem(null);
                menu.sendUpdateTo(player, true);
                return true;
            }

            if (SerializingItems.NEXT_PAGE.equals(type))
            {
                player.setMenuPage(player.getMenuPage() + 1);
                event.setCurrentItem(null);
                menu.sendUpdateTo(player, true);
                return true;
            }
        }

        if (SerializingItems.OPEN_MENU.equals(type))
        {
            MenuType opensMenu = ItemUtils.getOpensMenu(currItem);

            if (opensMenu == MenuType.MENU)
            {
                player.getProfile().openMainInv();
                return true;
            }

            if (opensMenu == MenuType.STATS)
            {
                player.getProfile().openStatsInv();
                return true;
            }

            if (opensMenu == MenuType.SETTINGS)
            {
                player.getProfile().openSettingsInv();
                return true;
            }

            if (opensMenu == MenuType.LANGUAGES)
            {
                player.getProfile().openLanguageInv();
                return true;
            }

            if (opensMenu == MenuType.KIT_SELECTION)
            {
                EggWars.getKitManager().getKitsMenu().openKitsMenu(player);
                return true;
            }
        }

        if (player.isInArena() && SerializingItems.LEAVE_ARENA.equals(type))
        {//TODO this is a test
            player.getPlayer().closeInventory();
            player.getArena().leaveArena(player, true, player.isEliminated());
            return true;
        }

        return false;
    }

    public static void defaultListener(InventoryClickEvent event, EwPlayer player, EwMenu menu)
    {
        MenuType mType = menu.getMenuType();
        ItemStack currItem = event.getCurrentItem();
        SerializingItems type = SerializingItems.getReferenceType(currItem);

        if (mType == MenuType.KIT_SELECTION)
        {
            if (SerializingItems.SELECT_KIT.equals(type))
            {
                Kit kit = SerializingItems.SELECT_KIT.getItemReference(currItem);
                Player pl = player.getPlayer();

                if (kit != null)
                {
                    String kitname = TranslationUtils.getMessage(kit.getName(), pl);

                    if (!pl.hasPermission("eggwars.kits"))
                    {
                        TranslationUtils.sendMessage("gameplay.kits.no_permission", pl, kitname);
                        return;
                    }

                    if (!player.hasKit(kit))
                    {
                        if (KitManager.buyKit(player, kit))
                        {
                            TranslationUtils.sendMessage("gameplay.kits.bought", pl, kitname);
                            TranslationUtils.sendMessage("gameplay.kits.selected", pl, kitname);
                            pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100F, 1000F);
                            EggWars.getDB().getPlayerData(pl).setKit(kit.id());
                            menu.sendUpdateTo(player, false);
                            return;
                        }
                        else
                        {
                            TranslationUtils.sendMessage("gameplay.kits.no_money", pl);
                            return;
                        }
                    }
                    else
                    {
                        if (EggWars.getDB().getPlayerData(pl).setKit(kit.id()))
                        {
                            TranslationUtils.sendMessage("gameplay.kits.selected", pl, kitname);
                            pl.playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100F, 100F);
                            menu.sendUpdateTo(player, false);
                            return;
                        }
                        else
                        {
                            TranslationUtils.sendMessage("gameplay.kits.already_selected", pl);
                            return;
                        }
                    }
                }
                else if (EggWars.getDB().getPlayerData(pl).setKit(""))
                {
                    TranslationUtils.sendMessage("gameplay.kits.deselected", pl);
                    pl.playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100F, 100F);
                    menu.sendUpdateTo(player, false);
                    return;
                }
            }

            return;
        }

        if (mType == MenuType.LANGUAGES)
        {
            if (SerializingItems.SELECT_LANGUAGE.equals(type))
            {
                String langId = SerializingItems.SELECT_LANGUAGE.getItemReference(currItem);

                if (langId != null)
                {
                    player.setLangId(langId);
                    return;
                }
            }
        }
    }
}
