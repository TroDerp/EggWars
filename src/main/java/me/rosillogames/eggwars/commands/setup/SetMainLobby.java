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
        super("setMainLobby", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        Player player = (Player)sender;
        EggWars.config.setMainLobby(player.getLocation());
        TranslationUtils.sendMessage("commands.setMainLobby.success", sender);
        return true;
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        return new ArrayList();
    }
}
