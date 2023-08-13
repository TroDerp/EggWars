package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.TeamUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class TeamList extends CommandArg
{
    public TeamList()
    {
        super(true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        Player player = (Player)sender;
        Arena arena = EggWars.getArenaManager().getArenaByWorld(player.getWorld());

        if (args.length >= 2 && (arena = EggWars.getArenaManager().cmdArenaByIdOrName(sender, args, 1)) == null)
        {
            return false;
        }

        if (arena == null)
        {
            TranslationUtils.sendMessage("commands.error.not_in_arena_world", sender);
            return false;
        }

        if (arena.getTeams().isEmpty())
        {
            TranslationUtils.sendMessage("commands.teamList.failed", player, arena.getName());
            return false;
        }

        TranslationUtils.sendMessage("commands.teamList.success", player, arena.getName());
        player.sendMessage(" ");

        for (TeamType teamType : arena.getTeams().keySet())
        {
            Team team = (Team)arena.getTeams().get(teamType);
            TextComponent teamComponent = new TextComponent(TeamUtils.translateTeamType(team.getType(), player, false));

            //Use middled respawn location for teleporting to team
            if (team.getRespawn() != null)
            {
                TextComponent tpComponent = new TextComponent(TranslationUtils.getMessage("commands.teamList.teleport", player));
                Location respawnLoc = Locations.toMiddle(team.getRespawn());
                String cmd = "tp " + player.getName() + " " + respawnLoc.getX() + " " + respawnLoc.getY() + " " + respawnLoc.getZ();
                tpComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + "/" + cmd)));
                tpComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minecraft:execute in " + respawnLoc.getWorld().getName() + " run " + cmd + " " + respawnLoc.getYaw() + " " + respawnLoc.getPitch()));
                teamComponent.addExtra(tpComponent);
            }

            player.spigot().sendMessage(teamComponent);
            player.sendMessage(" ");
        }

        return true;
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        List<String> list = new ArrayList();

        if (args.length == 2)
        {
            for (Arena arena : EggWars.getArenaManager().getArenas())
            {
                if (arena.getId().toLowerCase().startsWith(args[1].toLowerCase()))
                {
                    list.add(arena.getName());
                    list.add(arena.getId());
                }
            }
        }

        return list;
    }
}
