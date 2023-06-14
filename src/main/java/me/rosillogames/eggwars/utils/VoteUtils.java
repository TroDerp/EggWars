package me.rosillogames.eggwars.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.HealthType;
import me.rosillogames.eggwars.enums.ItemType;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;

public class VoteUtils
{
    private static TranslatableItem invItem;
    public static TranslatableItem healthVoteItem;
    public static TranslatableItem itemVoteItem;
    private static ItemStack[] tradesVoteItems = new ItemStack[3];
    private static ItemStack[] healthVoteItems = new ItemStack[4];

    public static void loadConfig()
    {
        ItemStack stack = ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting.item"), Material.END_CRYSTAL);
        ItemUtils.hideStackAttributes(stack);
        ItemUtils.setOpensMenu(stack, MenuType.VOTING);
        invItem = TranslatableItem.translatableNameLore(stack, "gameplay.voting.item_lore", "gameplay.voting.item_name");

        itemVoteItem = TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_items"), Material.VILLAGER_SPAWN_EGG)), "voting.items.item_lore", "voting.items.item_name");
        tradesVoteItems[0] = ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_hardcore_items"), Material.WOODEN_SWORD));
        tradesVoteItems[1] = ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_normal_items"), Material.STONE_SWORD));
        tradesVoteItems[2] = ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_overpowered_items"), Material.DIAMOND_SWORD));

        healthVoteItem = TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_health"), Material.POPPY)), "voting.health.item_lore", "voting.health.item_name");
        healthVoteItems[0] = ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_half_health"), Material.BOWL));
        healthVoteItems[1] = ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_normal_health"), Material.GLASS_BOTTLE));
        healthVoteItems[2] = ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_double_health"), Material.POTION));
        healthVoteItems[3] = ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.voting_triple_health"), Material.EXPERIENCE_BOTTLE));
    }

    public static ItemStack getInvItem(Player player)
    {
        return invItem.getTranslated(player);
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

    public static TranslatableItem getTradesVoteItem(ItemType type, Arena arena)
    {
        int votes = arena.getVotesForItem(type);
        return TranslatableItem.fullTranslatable((player) ->
        {
            ItemStack stack = tradesVoteItems[type.ordinal()].clone();
            stack.setAmount(votes < 1 ? 1 : votes);
            return stack;
        }, (player) ->
        {
            return TranslationUtils.getMessage(type.getNameKey() + ".item_lore", player, getVotesMessage(votes, player));
        }, (player) ->
        {
            return TranslationUtils.getMessage(type.getNameKey() + ".item_name", player);
        });
    }

    public static TranslatableItem getHealthVoteItem(HealthType type, Arena arena)
    {
        int votes = arena.getVotesForHealth(type);
        return TranslatableItem.fullTranslatable((player) ->
        {
            ItemStack stack = healthVoteItems[type.ordinal()].clone();
            stack.setAmount(votes < 1 ? 1 : votes);
            return stack;
        }, (player) ->
        {
            return TranslationUtils.getMessage(type.getNameKey() + ".item_lore", player, getVotesMessage(votes, player));
        }, (player) ->
        {
            return TranslationUtils.getMessage(type.getNameKey() + ".item_name", player);
        });
    }

    public static String getVotesMessage(int votes, Player player)
    {
        return TranslationUtils.getMessage(votes == 1 ? "voting.one_vote" : "voting.votes", player, votes);
    }
}
