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
        super("moveTeam", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        if (args.length != 3)
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

        TeamType team1 = TeamUtils.typeByIdAndValidateForArena(arena, args[1], sender);

        if (team1 == null)
        {
            return false;
        }

        TeamType team2;

        try
        {
            team2 = TeamType.byId(args[2]);
        }
        catch (IllegalArgumentException exc)
        {
            TranslationUtils.sendMessage("commands.error.team_does_not_exist", sender, args[2]);
            return false;
        }

        boolean switched = arena.moveTeam(team1, team2);
        TranslationUtils.sendMessage("commands.moveTeam.success", sender, team1.id(), team2.id());

        if (switched)
        {
            TranslationUtils.sendMessage("commands.moveTeam.success", sender, team2.id(), team1.id());
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
