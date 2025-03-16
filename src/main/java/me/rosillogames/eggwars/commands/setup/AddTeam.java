package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.language.TranslationUtils;

public class AddTeam extends CommandArg
{
    public AddTeam()
    {
        super("addTeam", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        if (args.length != 2)
        {
            this.sendUsage(sender);
            return false;
        }

        Player player = (Player)sender;
        Arena arena;

        if ((arena = Arena.checkEditArena(player)) == null)
        {
            return false;
        }

        TeamType teamType;

        try
        {
            teamType = TeamType.byId(args[1]);
        }
        catch (IllegalArgumentException exc)
        {
            TranslationUtils.sendMessage("commands.error.team_does_not_exist", sender, args[1]);
            return false;
        }

        if (arena.getTeams().containsKey(teamType))
        {
            TranslationUtils.sendMessage("commands.error.team_already_exists", sender, teamType.id());
            return false;
        }

        arena.addTeam(teamType);
        TranslationUtils.sendMessage("commands.addTeam.success", sender, teamType.id());
        arena.updateSetupTeam(teamType);
        arena.sendToDo(player);
        return true;
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        List<String> list = new ArrayList();

        if (args.length == 2)
        {
            Arena arena = EggWars.getArenaManager().getArenaByWorld(((Player)commandSender).getWorld());

            if (arena != null)
            {
                for (TeamType teamtype : TeamType.values())
                {
                    if (!arena.getTeams().containsKey(teamtype))
                    {
                        list.add(teamtype.id());
                    }
                }
            }
        }

        return list;
    }
}
