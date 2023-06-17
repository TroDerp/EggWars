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
        super(true);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        if (args.length != 2)
        {
            TranslationUtils.sendMessage("commands.addTeam.usage", commandSender);
            return false;
        }

        Player player = (Player)commandSender;
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
        catch (IllegalArgumentException illegalArgumentException)
        {
            TranslationUtils.sendMessage("commands.error.team_does_not_exist", commandSender, args[1]);
            return false;
        }

        if (arena.getTeams().containsKey(teamType))
        {
            TranslationUtils.sendMessage("commands.error.team_already_exists", commandSender, teamType.id());
            return false;
        }

        arena.addTeam(teamType);
        TranslationUtils.sendMessage("commands.addTeam.success", commandSender, teamType.id());
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
