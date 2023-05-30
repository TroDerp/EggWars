package me.rosillogames.eggwars.utils;

import java.util.Iterator;
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
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;

public class TeamUtils implements Listener
{
    private static TranslatableItem invItem;

    public static void loadConfig()
    {
        invItem = TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(ItemUtils.getItemOrDefault(EggWars.instance.getConfig().getString("inventory.team_selection.item"), Material.NETHER_STAR)), "gameplay.teams.item_lore", "gameplay.teams.item_name");
    }

    public static ItemStack getInvItem(Player player)
    {
        return invItem.getTranslated(player);
    }

    public static String translateTeamType(TeamTypes type, Player player, boolean allowBold)
    {
        return translateTeamType(type, player, allowBold, false);
    }

    public static String teamPrefix(TeamTypes type, Player player)
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

    public static String translateTeamType(TeamTypes type, Player player, boolean allowBold, boolean shortv)
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
        return ewpl.getTeam().getType().color() + ewpl.getPlayer().getName();
    }

    public static Team getTeamByEggLocation(Arena arena, Location location)
    {
        for (Iterator iterator = arena.getTeams().values().iterator(); iterator.hasNext();)
        {
            Team team = (Team)iterator.next();

            if (team.getEgg().getBlock().getLocation().equals(location.getBlock().getLocation()))
            {
                return team;
            }
        }

        return null;
    }

    public static TeamTypes byOrdinalInArena(Arena arena, int i) throws IllegalArgumentException
    {
        int j = 0;

        for (TeamTypes teamtype : TeamTypes.values())
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
    public static TeamTypes typeByIdAndValidateForArena(Arena arena, String string, CommandSender commandSender)
    {
        for (TeamTypes teamColor : TeamTypes.values())
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
