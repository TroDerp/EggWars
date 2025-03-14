package me.rosillogames.eggwars.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.shop.Category;
import me.rosillogames.eggwars.enums.ItemType;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.menu.ProfileMenus.MenuSize;
import me.rosillogames.eggwars.player.MenuPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;

public class ShopMenu extends EwMenu
{
    private final Map<String, Category> categories = new HashMap();
    /* Category names are properly sorted here */
    private final List<String> categoryOrdinal = new ArrayList();

    public ShopMenu(ItemType type)
    {
        super(MenuType.VILLAGER_MENU);
        this.setUsable(true);
    }

    /** Clears all categories and closes all menus related to this shop **/
    public void resetClear()
    {
        this.closeForEveryone(false);

        for (Category c : this.categories.values())
        {
            c.closeForEveryone(false);
        }

        this.categories.clear();
    }

    public void addCategory(String name, Category categ)
    {
        categ.buildMenu();
        this.categories.put(name, categ);
        this.categoryOrdinal.add(name);
    }

    public void openShopCategory(MenuPlayer ply, String name)
    {
        Category categ = this.categories.get(name);

        if (categ != null)
        {
            categ.openTrading(ply);
        }
        else
        {
            //TODO: send invalid message?
        }
    }

    @Nullable
    @Override
    public Inventory translateToPlayer(MenuPlayer ply, boolean reopen)
    {
        List<MenuSize> sizes = MenuSize.fromChestSize(categories.size());
        int page = Math.min(ply.getCurrentPage(), sizes.size() - 1);//limit page by available ones
        MenuSize size = (MenuSize)sizes.get(page);

        Inventory mcInventory;

        if (ply.getMenu() == this && !reopen)
        {
            mcInventory = ply.getCurrentInventory();
        }
        else
        {
            mcInventory = Bukkit.createInventory(null, size.getSlots(), TranslationUtils.getMessage("shop.title", ply.getPlayer()));
        }

        int counter = 0;
        int expected = size.getFilledSlots();
        Player player = ply.getPlayer();

        for (int j = 0; j < size.getFilledSlots() && counter < this.categoryOrdinal.size() && counter <= expected; ++counter)
        {
            int slot = 9 * (j / 7 + 1) + j % 7 + 1;
            mcInventory.setItem(slot, this.createCategoryItem(this.categoryOrdinal.get(counter)).apply(player));
            ++j;
        }

        if (page < sizes.size() - 1)
        {
            mcInventory.setItem(8, ProfileMenus.getNextItem().apply(player));
        }

        if (page > 0)
        {
            mcInventory.setItem(0, ProfileMenus.getPreviousItem().apply(player));
        }

        mcInventory.setItem(size.getSlots() - 5, ProfileMenus.getCloseItem(player));
        mcInventory.setItem(size.getSlots() - 1, ProfileMenus.getClassicShopItem().apply(player));
        Category specific = ply.getEwPlayer().getArena().getSpecificShop();

        if (specific != null && specific.isEmpty())
        {
            ItemStack stack = ItemUtils.makeMenuItem(specific.getDisplayItem().clone());
            SerializingItems.OPEN_CATEGORY.setItemReference(stack, "special");
            mcInventory.setItem(4, TranslatableItem.translatableNameLore(stack, specific.getTranslation() + ".desc", specific.getTranslation() + ".name").apply(player));
        }

        return mcInventory;
    }

    private TranslatableItem createCategoryItem(String catName)
    {
        Category category = this.categories.get(catName);
        ItemStack stack = ItemUtils.makeMenuItem(category.getDisplayItem().clone());
        SerializingItems.OPEN_CATEGORY.setItemReference(stack, catName);
        return TranslatableItem.translatableNameLore(stack, category.getTranslation() + ".desc", category.getTranslation() + ".name");
    }

    @Override
    public void clickInventory(InventoryClickEvent clickEvent, MenuPlayer player)
    {
        if (MenuClickListener.listenGeneric(clickEvent, player, this))
        {
            return;
        }

        ItemStack stack = clickEvent.getCurrentItem();

        if (SerializingItems.OPEN_CATEGORY.equals(SerializingItems.getReferenceType(stack)))
        {
            this.openShopCategory(player, SerializingItems.OPEN_CATEGORY.getItemReference(stack));
            return;
        }

        if (SerializingItems.CLASSIC_SHOP.equals(SerializingItems.getReferenceType(stack)))
        {
            EggWars.getDB().getPlayerData(player.getPlayer()).setClassicShop(!EggWars.getDB().getPlayerData(player.getPlayer()).isClassicShop());
            this.sendUpdateTo(player, false);
            return;
        }
    }
}
