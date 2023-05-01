package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;

public class SetMainLobby extends CommandArg
{
    public SetMainLobby()
    {
        super(true);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        Player player = (Player)commandSender;
        EggWars.config.setMainLobby(player.getLocation());
        TranslationUtils.sendMessage("commands.setMainLobby.success", commandSender);
        return true;
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        return new ArrayList();
    }
}
