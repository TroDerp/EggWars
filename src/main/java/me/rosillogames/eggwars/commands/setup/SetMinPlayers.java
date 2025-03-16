package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.utils.NumericUtils;

public class SetMinPlayers extends CommandArg
{
    public SetMinPlayers()
    {
        super("setMinPlayers", true);
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

        if (!NumericUtils.isInteger(args[1]) || Integer.parseInt(args[1]) < 1)
        {
            TranslationUtils.sendMessage("commands.error.invalid_number", sender);
            return false;
        }

        arena.setMinPlayers(Integer.parseInt(args[1]));
        TranslationUtils.sendMessage("commands.setMinPlayers.success", sender, arena.getName());
        arena.sendToDo(player);
        return true;
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        return new ArrayList();
    }
}
