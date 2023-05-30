package me.rosillogames.eggwars.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class CmdLeave implements TabExecutor
{
    public CmdLeave()
    {
    }

    public boolean onCommand(CommandSender commandSenderIn, Command commandIn, String s, String args[])
    {
        if (!(commandSenderIn instanceof Player))
        {
            TranslationUtils.sendMessage("commands.error.only_players", commandSenderIn);
            return true;
        }

        if (!commandSenderIn.hasPermission("eggwars.command.leave"))
        {
            TranslationUtils.sendMessage("commands.error.no_permission", commandSenderIn);
            return true;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)commandSenderIn);

        if (!ewplayer.isInArena())
        {
            TranslationUtils.sendMessage("commands.error.need_in_arena", commandSenderIn);
            return true;
        }

        ewplayer.getArena().leaveArena(ewplayer, true, false);
        return true;
    }

    public List onTabComplete(CommandSender commandSender, Command command, String s, String args[])
    {
        return new ArrayList();
    }
}
