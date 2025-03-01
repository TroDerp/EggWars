package me.rosillogames.eggwars.utils;

import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;

public class TeamUtils implements Listener
{
    private static TranslatableItem invItem;
    public static EwNamespace<String> teamTypeKey;

    public static void loadConfig()
    {
        ItemStack stack = ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.team_selection.item"), Material.NETHER_STAR);
        ItemUtils.makeMenuItem(stack);
        ItemUtils.setOpensMenu(stack, MenuType.TEAM_SELECTION);
        invItem = TranslatableItem.translatableNameLore(stack, "gameplay.teams.item_lore", "gameplay.teams.item_name");
    }

    public static ItemStack getInvItem(Player player)
    {
        return invItem.apply(player);
    }

    public static String translateTeamType(TeamType type, Player player, boolean allowBold)
    {
        return translateTeamType(type, player, allowBold, false);
    }

    public static String teamPrefix(TeamType type, Player player)
    {
        EwPlayer ewply = PlayerUtils.getEwPlayer(player);

        if (ewply != null && ewply.getTeam() != null && ewply.getTeam().getType() == type)
        {
            return type.color() + "§l[" + TeamUtils.translateTeamType(type, player, true, true) + type.color() + "§l]";
        }
        else
        {
            return type.color() + "[" + TeamUtils.translateTeamType(type, player, true, true) + type.color() + "]";
        }
    }

    public static String translateTeamType(TeamType type, Player player, boolean allowBold, boolean shortv)
    {
        if (type == null)
        {
            return "§cnull";
        }

        EwPlayer ewply = PlayerUtils.getEwPlayer(player);

        if (allowBold && ewply != null && ewply.getTeam() != null && ewply.getTeam().getType() == type)
        {
            return TranslationUtils.getMessage("team." + type.id() + (shortv ? ".short" : ""), player, "§l");
        }

        return TranslationUtils.getMessage("team." + type.id() + (shortv ? ".short" : ""), player);
    }

    public static String colorizePlayerName(EwPlayer ewpl)
    {
        return colorizePlayerName(ewpl.getPlayer(), ewpl.getTeam().getType());
    }

    public static String colorizePlayerName(Player pl, TeamType tmtype)
    {
        return tmtype.color() + pl.getName();
    }

    public static Team getTeamByEggLocation(Arena arena, Location location)
    {
        for (Team team : arena.getTeams().values())
        {
            if (team.getEgg().getBlock().getLocation().equals(location.getBlock().getLocation()))
            {
                return team;
            }
        }

        return null;
    }

    public static TeamType byOrdinalInArena(Arena arena, int i) throws IllegalArgumentException
    {
        int j = 0;

        for (TeamType teamtype : TeamType.values())
        {
            Team team = (Team)arena.getTeams().get(teamtype);

            if (team == null)
            {
                continue;
            }

            if (j == i)
            {
                return teamtype;
            }

            j++;
        }

        return null;
    }

    @Nullable
    public static TeamType typeByIdAndValidateForArena(Arena arena, String string, CommandSender commandSender)
    {
        for (TeamType teamColor : TeamType.values())
        {
            if (teamColor.id().equals(string))
            {
                if (!arena.getTeams().containsKey(teamColor))
                {
                    TranslationUtils.sendMessage("commands.error.team_does_not_exist", commandSender, teamColor.id());
                    return null;
                }

                return teamColor;
            }
        }

        TranslationUtils.sendMessage("commands.error.team_does_not_exist", commandSender, string);
        return null;
    }
}
