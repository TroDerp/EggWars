package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;

public class Help extends CommandArg
{
    public Help()
    {
        super(false);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        TranslationUtils.sendMessage("commands.setup.help", commandSender);
        return true;
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        return new ArrayList();
    }
}
