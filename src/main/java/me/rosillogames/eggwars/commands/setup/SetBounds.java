package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Bounds;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;

public class SetBounds extends CommandArg
{
    public SetBounds()
    {
        super(true);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        if (args.length != 7 && args.length != 2)
        {
            TranslationUtils.sendMessage("commands.setBounds.usage", commandSender);
            return false;
        }

        Player player = (Player)commandSender;
        Arena arena;

        if ((arena = Arena.checkEditArena(player)) == null)
        {
            return false;
        }

        if (args.length == 2 && args[1].equals("remove"))
        {
            arena.setBounds(new Bounds(null, null));
            TranslationUtils.sendMessage("commands.setBounds.success.removed", commandSender, arena.getName());
        }
        else
        {
            try
            {
                int sX = Integer.parseInt(args[1]);
                int sY = Integer.parseInt(args[2]);
                int sZ = Integer.parseInt(args[3]);
                int eX = Integer.parseInt(args[4]);
                int eY = Integer.parseInt(args[5]);
                int eZ = Integer.parseInt(args[6]);
                arena.setBounds(new Bounds(new Location(null, sX > eX ? eX : sX, sY > eY ? eY : sY, sZ > eZ ? eZ : sZ), new Location(null, sX > eX ? sX : eX, sY > eY ? sY : eY, sZ > eZ ? sZ : eZ)));
            }
            catch (NumberFormatException ex)
            {
                TranslationUtils.sendMessage("commands.error.invalid_number", commandSender);
                TranslationUtils.sendMessage("commands.setBounds.usage", commandSender);
                return false;
            }

            TranslationUtils.sendMessage("commands.setBounds.success", commandSender, arena.getName());
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
            if ("remove".toLowerCase().startsWith(args[1].toLowerCase()))
            {
                list.add("remove");
            }
        }

        return list;
    }
}
