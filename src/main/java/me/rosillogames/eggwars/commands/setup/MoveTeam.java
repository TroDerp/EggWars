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
import me.rosillogames.eggwars.utils.TeamUtils;

public class MoveTeam extends CommandArg
{
    public MoveTeam()
    {
        super(true);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        if (args.length != 3)
        {
            TranslationUtils.sendMessage("commands.moveTeam.usage", commandSender);
            return false;
        }

        Player player = (Player)commandSender;
        Arena arena;

        if ((arena = Arena.checkEditArena(player)) == null)
        {
            return false;
        }

        TeamType team1 = TeamUtils.typeByIdAndValidateForArena(arena, args[1], commandSender);

        if (team1 == null)
        {
            return false;
        }

        TeamType team2;

        try
        {
            team2 = TeamType.byId(args[2]);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            TranslationUtils.sendMessage("commands.error.team_does_not_exist", commandSender, args[2]);
            return false;
        }

        boolean flag = arena.moveTeam(team1, team2);
        TranslationUtils.sendMessage("commands.moveTeam.success", commandSender, team1.id(), team2.id());

        if (flag)
        {
            TranslationUtils.sendMessage("commands.moveTeam.success", commandSender, team2.id(), team1.id());
        }

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
                for (TeamType teamtypes : arena.getTeams().keySet())
                {
                    if (teamtypes.id().startsWith(args[1]))
                    {
                        list.add(teamtypes.id());
                    }
                }
            }
        }
        else if (args.length == 3)
        {
            Arena arena = EggWars.getArenaManager().getArenaByWorld(((Player)commandSender).getWorld());

            if (arena != null)
            {
                for (TeamType teamtype : TeamType.values())
                {
                    list.add(teamtype.id());
                }
            }
        }

        return list;
    }
}
