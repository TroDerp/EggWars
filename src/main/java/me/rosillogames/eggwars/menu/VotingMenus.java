package me.rosillogames.eggwars.menu;

import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.HealthType;
import me.rosillogames.eggwars.enums.ItemType;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;

public class VotingMenus
{
    private static TranslatableItem invItem;
    private static TranslatableItem healthVoteItem;
    private static TranslatableItem tradesVoteItem;
    private static ItemStack[] tradesVoteItems = new ItemStack[3];
    private static ItemStack[] healthVoteItems = new ItemStack[4];

    //Move all this to Arena ?
    private final Function<ItemType, Integer> tradesVotes;//Getter of votes for specified trades type
    private final Function<HealthType, Integer> healthVotes;//Getter of votes for specified health type
    private final TranslatableMenu mainMenu;
    private final TranslatableMenu tradesMenu;
    private final TranslatableMenu healthMenu;

    public VotingMenus(Arena arenaIn)
    {
        this.tradesVotes = (type) -> arenaIn.getVotesForItem(type);
        this.healthVotes = (type) -> arenaIn.getVotesForHealth(type);
        this.mainMenu = new TranslatableMenu(MenuType.VOTING, this::clickInventory);
        this.tradesMenu = new TranslatableMenu(MenuType.ITEM_VOTING, this::clickInventory);
        this.tradesMenu.setParent(this.mainMenu);
        this.healthMenu = new TranslatableMenu(MenuType.HEALTH_VOTING, this::clickInventory);
        this.healthMenu.setParent(this.mainMenu);
    }

    public static void loadConfig()
    {
        ItemStack stack = ItemUtils.makeMenuItem(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting.item"), Material.END_CRYSTAL));
        ItemUtils.setOpensMenu(stack, MenuType.VOTING);
        invItem = TranslatableItem.translatableNameLore(stack, "gameplay.voting.item_lore", "gameplay.voting.item_name");

        ItemStack tradesItem = ItemUtils.makeMenuItem(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_items"), Material.VILLAGER_SPAWN_EGG));
        ItemUtils.setOpensMenu(tradesItem, MenuType.ITEM_VOTING);
        tradesVoteItem = TranslatableItem.translatableNameLore(tradesItem, "voting.items.item_lore", "voting.items.item_name");
        ItemStack healthItem = ItemUtils.makeMenuItem(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_health"), Material.POPPY));
        ItemUtils.setOpensMenu(healthItem, MenuType.HEALTH_VOTING);
        healthVoteItem = TranslatableItem.translatableNameLore(healthItem, "voting.health.item_lore", "voting.health.item_name");

        tradesVoteItems[0] = ItemUtils.makeMenuItem(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_hardcore_items"), Material.WOODEN_SWORD));
        SerializingItems.VOTE_ITEM_TYPE.setItemReference(tradesVoteItems[0], ItemType.HARDCORE);
        tradesVoteItems[1] = ItemUtils.makeMenuItem(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_normal_items"), Material.STONE_SWORD));
        SerializingItems.VOTE_ITEM_TYPE.setItemReference(tradesVoteItems[1], ItemType.NORMAL);
        tradesVoteItems[2] = ItemUtils.makeMenuItem(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_overpowered_items"), Material.DIAMOND_SWORD));
        SerializingItems.VOTE_ITEM_TYPE.setItemReference(tradesVoteItems[2], ItemType.OVERPOWERED);

        healthVoteItems[0] = ItemUtils.makeMenuItem(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_half_health"), Material.BOWL));
        SerializingItems.VOTE_HEALTH_TYPE.setItemReference(healthVoteItems[0], HealthType.HALF);
        healthVoteItems[1] = ItemUtils.makeMenuItem(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_normal_health"), Material.GLASS_BOTTLE));
        SerializingItems.VOTE_HEALTH_TYPE.setItemReference(healthVoteItems[1], HealthType.NORMAL);
        healthVoteItems[2] = ItemUtils.makeMenuItem(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_double_health"), Material.POTION));
        SerializingItems.VOTE_HEALTH_TYPE.setItemReference(healthVoteItems[2], HealthType.DOUBLE);
        healthVoteItems[3] = ItemUtils.makeMenuItem(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_triple_health"), Material.EXPERIENCE_BOTTLE));
        SerializingItems.VOTE_HEALTH_TYPE.setItemReference(healthVoteItems[3], HealthType.TRIPLE);
    }

    public static ItemStack getInvItem(Player player)
    {
        return invItem.apply(player);
    }

    private void clickInventory(InventoryClickEvent event, EwPlayer player, EwMenu menu)
    {
        ItemStack currItem = event.getCurrentItem();
        SerializingItems type = SerializingItems.getReferenceType(currItem);

        if (!player.isInArena() || (!player.getArena().getStatus().equals(ArenaStatus.STARTING_GAME) && !player.getArena().getStatus().isLobby()))
        {
            return;
        }

        if (SerializingItems.VOTE_ITEM_TYPE.equals(type))
        {
            ItemType iType = SerializingItems.VOTE_ITEM_TYPE.getItemReference(currItem);

            if (player.getArena().playerVoteItem(iType, player))
            {
                for (EwPlayer ewplayer1 : player.getArena().getPlayers())
                {
                    VotingMenus.sendItemVotedMessage(player, ewplayer1, iType);
                }

                menu.sendMenuUpdate(false);
            }

            return;
        }

        if (SerializingItems.VOTE_HEALTH_TYPE.equals(type))
        {
            HealthType hType = SerializingItems.VOTE_HEALTH_TYPE.getItemReference(currItem);

            if (player.getArena().playerVoteHealth(hType, player))
            {
                for (EwPlayer ewplayer1 : player.getArena().getPlayers())
                {
                    VotingMenus.sendHealthVotedMessage(player, ewplayer1, hType);
                }

                menu.sendMenuUpdate(false);
            }

            return;
        }

        //clickEvent.setCurrentItem(null);//When re-opening inventory, it's is clever to set item to null right BEFORE that happens, Important note, use this ONLY and ONLY when we are sure the inventory will FOR SURE, be RE-OPENED.
        MenuType opener = ItemUtils.getOpensMenu(currItem);

        if (MenuType.ITEM_VOTING.equals(opener))
        {
            this.openTradesMenu(player);
            return;
        }
        else if (MenuType.HEALTH_VOTING.equals(opener))
        {
            this.openHealthMenu(player);
            return;
        }

        return;
    }

    public static void sendItemVotedMessage(EwPlayer votingPlayer, EwPlayer ewPlayer, ItemType type)
    {
        String s = getVotesMessage(ewPlayer.getArena().getVotesForItem(type), ewPlayer.getPlayer());
        TranslationUtils.sendMessage("gameplay.voting.items.voted", ewPlayer.getPlayer(), votingPlayer.getPlayer().getName(), TranslationUtils.getMessage(type.getNameKey(), ewPlayer.getPlayer()), s);
    }

    public static void sendHealthVotedMessage(EwPlayer votingPlayer, EwPlayer ewPlayer, HealthType type)
    {
        String s = getVotesMessage(ewPlayer.getArena().getVotesForHealth(type), ewPlayer.getPlayer());
        TranslationUtils.sendMessage("gameplay.voting.health.voted", ewPlayer.getPlayer(), votingPlayer.getPlayer().getName(), TranslationUtils.getMessage(type.getNameKey(), ewPlayer.getPlayer()), s);
    }

    public static String getVotesMessage(int votes, Player player)
    {
        return TranslationUtils.getMessage(votes == 1 ? "voting.one_vote" : "voting.votes", player, votes);
    }

    public void updateMenus()
    {
        this.tradesMenu.sendMenuUpdate(false);
        this.healthMenu.sendMenuUpdate(false);
    }

    public void buildInventories()
    {
        this.mainMenu.clearPages();
        TranslatableInventory tInv1 = new TranslatableInventory(27, "voting.menu_title");
        tInv1.setItem(22, ProfileMenus.getCloseItem());
        tInv1.setItem(11, tradesVoteItem);
        tInv1.setItem(15, healthVoteItem);
        this.mainMenu.addPage(tInv1);
        this.tradesMenu.clearPages();
        TranslatableInventory tInv2 = new TranslatableInventory(27, "voting.items.menu_title");
        int slot = 10;

        for (int i = 0; i < ItemType.values().length; ++i)
        {
            tInv2.setItem(slot + (i * 3), this.getTradesVoteItem(ItemType.values()[i]));
        }

        tInv2.setItem(22, ProfileMenus.getCloseItem());
        this.tradesMenu.addPage(tInv2);
        this.healthMenu.clearPages();
        TranslatableInventory tInv3 = new TranslatableInventory(27, "voting.health.menu_title");

        for (int i = 0; i < HealthType.values().length; ++i)
        {
            tInv3.setItem(slot + (i * 2), this.getHealthVoteItem(HealthType.values()[i]));
        }

        tInv3.setItem(22, ProfileMenus.getCloseItem());
        this.healthMenu.addPage(tInv3);
    }

    public void openMainMenu(EwPlayer player)
    {
        this.mainMenu.addOpener(player);
    }

    public void openTradesMenu(EwPlayer player)
    {
        this.tradesMenu.addOpener(player);
    }

    public void openHealthMenu(EwPlayer player)
    {
        this.healthMenu.addOpener(player);
    }

    private TranslatableItem getTradesVoteItem(ItemType type)
    {
        return TranslatableItem.fullTranslatable((player) ->
        {
            int votes = this.tradesVotes.apply(type);
            ItemStack stack = tradesVoteItems[type.ordinal()].clone();
            stack.setAmount(votes < 1 ? 1 : votes);
            return stack;
        }, (player) ->
        {
            int votes = this.tradesVotes.apply(type);
            return TranslationUtils.getMessage(type.getNameKey() + ".item_lore", player, getVotesMessage(votes, player));
        }, (player) ->
        {
            return TranslationUtils.getMessage(type.getNameKey() + ".item_name", player);
        });
    }

    private TranslatableItem getHealthVoteItem(HealthType type)
    {
        return TranslatableItem.fullTranslatable((player) ->
        {
            int votes = this.healthVotes.apply(type);
            ItemStack stack = healthVoteItems[type.ordinal()].clone();
            stack.setAmount(votes < 1 ? 1 : votes);
            return stack;
        }, (player) ->
        {
            int votes = this.healthVotes.apply(type);
            return TranslationUtils.getMessage(type.getNameKey() + ".item_lore", player, getVotesMessage(votes, player));
        }, (player) ->
        {
            return TranslationUtils.getMessage(type.getNameKey() + ".item_name", player);
        });
    }
}
