package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.utils.NumericUtils;

public class SetVoidHeight extends CommandArg
{
    public SetVoidHeight()
    {
        super(true);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        if (args.length != 2)
        {
            TranslationUtils.sendMessage("commands.setVoidHeight.usage", commandSender);
            return false;
        }

        Player player = (Player)commandSender;
        Arena arena;

        if ((arena = Arena.checkEditArena(player)) == null)
        {
            return false;
        }

        Integer value;

        if (args[1].equalsIgnoreCase("remove"))
        {
            value = null;
        }
        else if (!NumericUtils.isInteger(args[1]) || Integer.parseInt(args[1]) < 1)
        {
            TranslationUtils.sendMessage("commands.error.invalid_number", commandSender);
            return false;
        }
        else
        {
            value = Integer.parseInt(args[1]);
        }

        arena.setVoidHeight(value);
        TranslationUtils.sendMessage("commands.setVoidHeight.success", commandSender, arena.getName());
        arena.sendToDo(player);
        return true;
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        List<String> list = new ArrayList();

        if (args.length == 2 && "remove".startsWith(args[1].toLowerCase()))
        {
            list.add("remove");
        }

        return list;
    }
}
