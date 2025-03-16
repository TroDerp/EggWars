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
        super("setBounds", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        if (args.length != 7 && args.length != 2)
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

        Bounds bounds = arena.getBounds();

        if (args.length == 2 && args[1].equalsIgnoreCase("remove"))
        {
            bounds.setBounds(null, null);
            TranslationUtils.sendMessage("commands.setBounds.success.removed", sender, arena.getName());
        }
        else
        {
            try
            {
                bounds.setBounds(new Location(null, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])), new Location(null, Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6])));
            }
            catch (NumberFormatException ex)
            {
                TranslationUtils.sendMessage("commands.error.invalid_number", sender);
                this.sendUsage(sender);
                return false;
            }

            TranslationUtils.sendMessage("commands.setBounds.success", sender, arena.getName());
        }

        arena.getSetupGUI().updateBasicsMenu();
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
