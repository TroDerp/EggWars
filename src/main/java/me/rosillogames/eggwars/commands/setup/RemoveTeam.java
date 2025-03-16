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

public class RemoveTeam extends CommandArg
{
    public RemoveTeam()
    {
        super("removeTeam", true);
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

        TeamType teamtype = TeamUtils.typeByIdAndValidateForArena(arena, args[1], sender);

        if (teamtype == null)
        {
            return false;
        }

        arena.removeTeam(teamtype);
        TranslationUtils.sendMessage("commands.removeTeam.success", sender, teamtype.id());
        arena.updateSetupTeam(teamtype);
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

        return list;
    }
}
