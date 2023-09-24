package me.rosillogames.eggwars.listeners;

import java.util.Map;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.arena.shop.Category;
import me.rosillogames.eggwars.arena.shop.Offer;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.HealthType;
import me.rosillogames.eggwars.enums.ItemType;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.loaders.KitLoader;
import me.rosillogames.eggwars.objects.Kit;
import me.rosillogames.eggwars.objects.KitsMenu;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.player.inventory.EwInventory;
import me.rosillogames.eggwars.player.inventory.InventoryController;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.VoteUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class InventoryListener implements Listener
{
    @EventHandler
    public void cancelIllegalInventoryChanges(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.isInArena() && clickEvent.getClickedInventory() != null)
        {
            if (clickEvent.getClickedInventory().getType() == InventoryType.PLAYER && !ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME))
            {
                clickEvent.setCancelled(true);
                return;
            }

            //TODO: Store original contents of containers modified during the game to fix a bug
            //where containers are saved between games (also happens to chests), in new arena class
            //if (clickEvent.getClickedInventory().getType() == InventoryType.FURNACE)
            //{
                //clickEvent.setCancelled(true);
                //return;
            //}
        }
    }

    @EventHandler
    public void cancelIllegalItemSwap(PlayerSwapHandItemsEvent event)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (ewplayer.isInArena() && !ewplayer.getArena().getStatus().equals(ArenaStatus.IN_GAME))
        {
            event.setCancelled(true);
            return;
        }
        else
        {
            return;
        }
    }

    @EventHandler
    public void openTeamEC(InventoryOpenEvent openEvent)
    {
        if (!(openEvent.getPlayer() instanceof Player) || openEvent.getPlayer() == null)
        {
            return;
        }

        EwPlayer ewply = PlayerUtils.getEwPlayer((Player)openEvent.getPlayer());

        if (ewply != null && ewply.getTeam() != null && openEvent.getInventory().getType() == InventoryType.ENDER_CHEST && ewply.getInv() == null && EggWars.config.shareTeamEC)
        {
            openEvent.setCancelled(true);
            EwInventory ewInv = new EwInventory(ewply, ewply.getTeam().getEnderChest(), MenuType.TEAM_ENDER_CHEST);
            Block block = ReflectionUtils.getEndChestBlock(ewply.getPlayer());

            if (block != null && block.getState() instanceof EnderChest)
            {
                ((EnderChest)block.getState()).open();
                ewInv.setExtraData(block);
            }

            ewply.setInv(ewInv);
            ewply.getPlayer().openInventory(ewInv.getInventory());
        }

        return;
    }

    @EventHandler
    public void updatePlayerInvWhenClosing(InventoryCloseEvent closeEvent)
    {
        if (!(closeEvent.getPlayer() instanceof Player) || closeEvent.getPlayer() == null || !((Player)closeEvent.getPlayer()).isOnline())
        {
            return;
        }

        EwPlayer ewply = PlayerUtils.getEwPlayer((Player)closeEvent.getPlayer());

        if (ewply == null || ewply.getInv() == null)
        {
            return;
        }

        if (ewply.getInv().getInventoryType() == MenuType.TEAM_ENDER_CHEST && ewply.getInv().getExtraData() != null)
        {
            BlockState state = ((Block)ewply.getInv().getExtraData()).getLocation().getBlock().getState();

            if (state instanceof EnderChest)
            {
                ((EnderChest)state).close();
            }
        }

        if (ewply.getInv().getInventory() == closeEvent.getInventory())
        {
            ewply.setInv(null);
        }

        return;
    }

    @EventHandler
    public void shopDrag(InventoryDragEvent dragEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)dragEvent.getWhoClicked());

        if (ewplayer.getInv() == null || dragEvent.isCancelled())
        {
            return;
        }

        if (!ewplayer.isInArena())
        {
            return;
        }

        if (ewplayer.getInv().getInventoryType() == MenuType.VILLAGER_MENU || ewplayer.getInv().getInventoryType() == MenuType.VILLAGER_TRADING)
        {//this controls whether if drag action is invalid in the current slot (inside villager container)
            for (Integer integer : dragEvent.getRawSlots())
            {
                if (integer.intValue() < dragEvent.getInventory().getSize())
                {
                    dragEvent.setCancelled(true);
                    return;
                }
            }

            return;
        }
    }

    @EventHandler
    public void villagerShop(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.getInv() == null || clickEvent.isCancelled() || !ewplayer.isInArena())
        {
            return;
        }

        if (ewplayer.getInv().getInventoryType() != MenuType.VILLAGER_MENU && ewplayer.getInv().getInventoryType() != MenuType.VILLAGER_TRADING)
        {
            return;
        }

        if (clickEvent.getRawSlot() > (clickEvent.getInventory().getSize() - 1))
        {//this controls whether if shift click is invalid in the current slot (inside villager container)
            if (clickEvent.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
            {
                clickEvent.setCancelled(true);
                return;
            }

            return;
        }

        clickEvent.setCancelled(true);

        if (clickEvent.getCurrentItem() == null || useCloseMenu(clickEvent))
        {
            return;
        }

        if (ewplayer.getInv().getInventoryType() == MenuType.VILLAGER_MENU)
        {
            int page = ((Integer)ewplayer.getInv().getExtraData()).intValue();
            Category cat = ewplayer.getArena().getShopSlots(page, clickEvent.getRawSlot());

            if (cat != null)
            {
                cat.openTrading(ewplayer.getPlayer(), ewplayer.getArena().getItemType());
                clickEvent.setCurrentItem(null);
                return;
            }

            if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getClassicShopItem().getTranslated(ewplayer.getPlayer())))
            {
                EggWars.getDB().getPlayerData(ewplayer.getPlayer()).setClassicShop(!EggWars.getDB().getPlayerData(ewplayer.getPlayer()).isClassicShop());
                InventoryController.updateInventory(ewplayer, null, false);
                return;
            }

            if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getNextItem().getTranslated(ewplayer.getPlayer())))
            {
                ewplayer.getArena().openVillagerInv(ewplayer.getPlayer(), page + 1);
                clickEvent.setCurrentItem(null);
                return;
            }

            if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getPreviousItem().getTranslated(ewplayer.getPlayer())))
            {
                ewplayer.getArena().openVillagerInv(ewplayer.getPlayer(), page - 1);
                clickEvent.setCurrentItem(null);
                return;
            }
        }

        if (ewplayer.getInv().getInventoryType() == MenuType.VILLAGER_TRADING)
        {
            Map<Integer, Offer> map = (Map<Integer, Offer>)ewplayer.getInv().getExtraData();
            Offer trade = map.get(clickEvent.getRawSlot());

            if (trade != null)
            {
                boolean traded = trade.trade(ewplayer.getPlayer(), clickEvent.getClick().isShiftClick());
                ewplayer.getPlayer().playSound(ewplayer.getPlayer().getLocation(), traded ? Sound.UI_BUTTON_CLICK : Sound.BLOCK_ANVIL_LAND, 1.0F, 2.0F);
                InventoryController.updateInventory(ewplayer, null, false);
                return;
            }
        }
    }

    @EventHandler
    public void votes(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.getInv() == null)
        {
            return;
        }

        boolean main = ewplayer.getInv().getInventoryType() == MenuType.VOTING;
        boolean trades = ewplayer.getInv().getInventoryType() == MenuType.ITEM_VOTING;
        boolean health = ewplayer.getInv().getInventoryType() == MenuType.HEALTH_VOTING;

        if (!main && !trades && !health)
        {
            return;
        }

        clickEvent.setCancelled(true);

        if (clickEvent.getCurrentItem() == null || useCloseMenu(clickEvent))
        {
            return;
        }

        if (!ewplayer.getArena().getStatus().equals(ArenaStatus.STARTING_GAME) && !ewplayer.getArena().getStatus().equals(ArenaStatus.STARTING) && !ewplayer.getArena().getStatus().equals(ArenaStatus.WAITING))
        {
            return;
        }

        if (main)
        {
            if (clickEvent.getRawSlot() == 11)
            {
                ewplayer.getArena().openItemVoteInv(ewplayer.getPlayer());
            }
            else if (clickEvent.getRawSlot() == 15)
            {
                ewplayer.getArena().openHealthVoteInv(ewplayer.getPlayer());
            }
        }
        else
        {
            final int slot = clickEvent.getRawSlot();

            if (trades)
            {
                ItemType iType = slot == 10 ? ItemType.HARDCORE : slot == 16 ? ItemType.OVERPOWERED : ItemType.NORMAL;

                if (iType != ItemType.NORMAL || slot == 13)
                {
                    if (ewplayer.getArena().playerVoteItem(iType, ewplayer))
                    {
                        for (EwPlayer ewplayer1 : ewplayer.getArena().getPlayers())
                        {
                            VoteUtils.sendItemVotedMessage(ewplayer, ewplayer1, iType);
                        }

                        ewplayer.getArena().updateInvs();
                    }

                    return;
                }

            }
            else if (health)
            {
                HealthType hType = slot == 10 ? HealthType.HALF : slot == 16 ? HealthType.TRIPLE : slot == 14 ? HealthType.DOUBLE : HealthType.NORMAL;

                if (hType != HealthType.NORMAL || slot == 12)
                {
                    if (ewplayer.getArena().playerVoteHealth(hType, ewplayer))
                    {
                        for (EwPlayer ewplayer1 : ewplayer.getArena().getPlayers())
                        {
                            VoteUtils.sendHealthVotedMessage(ewplayer, ewplayer1, hType);
                        }

                        ewplayer.getArena().updateInvs();
                    }

                    return;
                }

            }
        }
    }

    @EventHandler
    public void generator(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.getInv() == null || ewplayer.getInv().getInventoryType() != MenuType.GENERATOR_INFO)
        {
            return;
        }

        clickEvent.setCancelled(true);
        Generator gen = (Generator)ewplayer.getInv().getExtraData();

        if (clickEvent.getCurrentItem() == null || useCloseMenu(clickEvent))
        {
            return;
        }

        if (clickEvent.getRawSlot() == 15 && gen.hasCachedType())
        {
            gen.tryUpgrade(ewplayer.getPlayer());
            return;
        }
    }

    @EventHandler
    public void kits(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.getInv() == null || ewplayer.getInv().getInventoryType() != MenuType.KIT_SELECTION)
        {
            return;
        }

        clickEvent.setCancelled(true);

        if (clickEvent.getCurrentItem() == null || useCloseMenu(clickEvent))
        {
            return;
        }

        Player player = (Player)clickEvent.getWhoClicked();
        int page = ((Integer)ewplayer.getInv().getExtraData()).intValue();
        Kit kit;

        if ((kit = EggWars.getKitManager().getKitsMenu().getKit(page, clickEvent.getRawSlot())) != null)
        {
            String kitname = TranslationUtils.getMessage(kit.getName(), player);

            if (!player.hasPermission("eggwars.kits"))
            {
                TranslationUtils.sendMessage("gameplay.kits.no_permission", player, kitname);
                return;
            }

            if (!ewplayer.hasKit(kit))
            {
                if (KitLoader.buyKit(ewplayer, kit))
                {
                    TranslationUtils.sendMessage("gameplay.kits.bought", player, kitname);
                    TranslationUtils.sendMessage("gameplay.kits.selected", player, kitname);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100F, 1000F);
                    EggWars.getDB().getPlayerData(player).setKit(kit.id());
                    InventoryController.updateInventory(ewplayer, null, false);
                    return;
                }
                else
                {
                    TranslationUtils.sendMessage("gameplay.kits.no_money", player);
                    return;
                }
            }
            else
            {
                if (EggWars.getDB().getPlayerData(player).setKit(kit.id()))
                {
                    TranslationUtils.sendMessage("gameplay.kits.selected", player, kitname);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100F, 100F);
                    InventoryController.updateInventory(ewplayer, null, false);
                    return;
                }
                else
                {
                    TranslationUtils.sendMessage("gameplay.kits.already_selected", player);
                    return;
                }
            }
        }

        if (clickEvent.getCurrentItem().equals(KitsMenu.getDeselectItem().getTranslated(player)) && EggWars.getDB().getPlayerData(player).setKit(""))
        {
            TranslationUtils.sendMessage("gameplay.kits.deselected", player);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100F, 100F);
            InventoryController.updateInventory(ewplayer, null, false);
            return;
        }

        if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getNextItem().getTranslated(player)))
        {
            EggWars.getKitManager().openKitsInv(player, page + 1);
            clickEvent.setCurrentItem(null);
            return;
        }

        if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getPreviousItem().getTranslated(player)))
        {
            EggWars.getKitManager().openKitsInv(player, page - 1);
            clickEvent.setCurrentItem(null);
            return;
        }
    }

    @EventHandler
    public void teams(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.getInv() == null || ewplayer.getInv().getInventoryType() != MenuType.TEAM_SELECTION)
        {
            return;
        }

        clickEvent.setCancelled(true);

        if (clickEvent.getCurrentItem() == null)
        {
            return;
        }

        if (!ewplayer.getArena().getStatus().equals(ArenaStatus.STARTING) && !ewplayer.getArena().getStatus().equals(ArenaStatus.WAITING))
        {
            return;
        }

        Team team = (Team)ewplayer.getArena().getTeams().get(TeamUtils.byOrdinalInArena(ewplayer.getArena(), clickEvent.getRawSlot()));

        if (team == null)
        {
            if (clickEvent.getRawSlot() == (clickEvent.getClickedInventory().getSize() - 1) && ewplayer.getTeam() != null)
            {
                TranslationUtils.sendMessage("gameplay.teams.random", ewplayer.getPlayer());
                ewplayer.getPlayer().playSound(ewplayer.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100F, 100F);
                ewplayer.getTeam().removePlayer(ewplayer);
                ewplayer.getArena().updateInvs();
            }

            return;
        }

        if (ewplayer.getTeam() != null && ewplayer.getTeam().equals(team))
        {
            TranslationUtils.sendMessage("gameplay.teams.already_in", ewplayer.getPlayer());
            return;
        }

        if (!team.canJoin())
        {
            TranslationUtils.sendMessage("gameplay.teams.full", ewplayer.getPlayer());
            return;
        }

        String teamName = TeamUtils.translateTeamType(team.getType(), ewplayer.getPlayer(), false);
        TranslationUtils.sendMessage("gameplay.teams.joined", ewplayer.getPlayer(), teamName);
        ewplayer.getPlayer().playSound(ewplayer.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100F, 100F);

        if (ewplayer.getTeam() != null)
        {
            ewplayer.getTeam().removePlayer(ewplayer);
        }

        team.addPlayer(ewplayer);
        ewplayer.getArena().updateInvs();
    }

    @EventHandler
    public void menu(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.getInv() == null || ewplayer.getInv().getInventoryType() != MenuType.MENU)
        {
            return;
        }

        clickEvent.setCancelled(true);

        if (clickEvent.getCurrentItem() == null || useCloseMenu(clickEvent))
        {
            return;
        }

        if (clickEvent.getRawSlot() == 11)
        {
            ewplayer.getMenu().openStatsInv();
        }
        else if (clickEvent.getRawSlot() == 15)
        {
            ewplayer.getMenu().openSettingsInv();
        }
        else if (clickEvent.getRawSlot() == 33)
        {
            EggWars.getKitManager().openKitsInv(ewplayer.getPlayer(), 0);
        }
    }

    @EventHandler
    public void stats(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.getInv() == null || ewplayer.getInv().getInventoryType() != MenuType.STATS)
        {
            return;
        }

        clickEvent.setCancelled(true);

        if (clickEvent.getCurrentItem() == null || useCloseMenu(clickEvent))
        {
            return;
        }
    }

    @EventHandler
    public void settings(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.getInv() == null || ewplayer.getInv().getInventoryType() != MenuType.SETTINGS)
        {
            return;
        }

        clickEvent.setCancelled(true);

        if (clickEvent.getCurrentItem() == null || useCloseMenu(clickEvent))
        {
            return;
        }

        if (clickEvent.getRawSlot() == 22)
        {
            ewplayer.getMenu().openLanguageInv(0);
        }
    }

    @EventHandler
    public void languages(InventoryClickEvent clickEvent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

        if (ewplayer.getInv() == null || ewplayer.getInv().getInventoryType() != MenuType.LANGUAGES)
        {
            return;
        }

        clickEvent.setCancelled(true);

        if (clickEvent.getCurrentItem() == null || useCloseMenu(clickEvent))
        {
            return;
        }

        int page = ((Integer)ewplayer.getInv().getExtraData()).intValue();
        String langId;

        if ((langId = ewplayer.getMenu().getLangId(page, clickEvent.getRawSlot())) != null)
        {
            ewplayer.setLangId(langId);
            ewplayer.getMenu().openLanguageInv(page);
            return;
        }

        if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getNextItem().getTranslated(ewplayer.getPlayer())))
        {
            ewplayer.getMenu().openLanguageInv(page + 1);
            clickEvent.setCurrentItem(null);
            return;
        }

        if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getPreviousItem().getTranslated(ewplayer.getPlayer())))
        {
            ewplayer.getMenu().openLanguageInv(page - 1);
            clickEvent.setCurrentItem(null);
            return;
        }
    }

    public static boolean useCloseMenu(InventoryClickEvent clickEvent)
    {
        if (EwPlayerMenu.getCloseItem().getTranslated((Player)clickEvent.getWhoClicked()).equals(clickEvent.getCurrentItem()))
        {
            InventoryController.closeInventory((Player)clickEvent.getWhoClicked(), true);
            clickEvent.setCurrentItem(null);
            return true;
        }

        return false;
    }
}
