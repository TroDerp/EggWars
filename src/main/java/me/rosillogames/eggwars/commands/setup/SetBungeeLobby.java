package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;

public class SetBungeeLobby extends CommandArg
{
    public SetBungeeLobby()
    {
        super(false);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        if (args.length != 2)
        {
            TranslationUtils.sendMessage("commands.setBungeeLobby.usage", commandSender);
            return false;
        }

        EggWars.bungee.setLobby(args[1]);
        TranslationUtils.sendMessage("commands.setBungeeLobby.success", commandSender, args[1]);
        return true;
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        return new ArrayList();
    }
}
