package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;

public class SetCenter extends CommandArg
{
    public SetCenter()
    {
        super("setCenter", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        if (args.length != 1)
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

        arena.setCenter(player.getLocation());
        TranslationUtils.sendMessage("commands.setCenter.success", sender, arena.getName());
        arena.getSetupGUI().updateBasicsMenu();
        arena.sendToDo(player);
        return true;
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        return new ArrayList();
    }
}
