package me.rosillogames.eggwars.commands;

import java.util.List;
import org.bukkit.command.CommandSender;

public abstract class CommandArg
{
    private final boolean isPlayersOnly;

    public CommandArg(boolean playersOnly)
    {
        this.isPlayersOnly = playersOnly;
    }

    public boolean isPlayersOnly()
    {
        return this.isPlayersOnly;
    }

    public abstract boolean execute(CommandSender commandSender, String args[]);

    public abstract List<String> getCompleteArgs(CommandSender commandSender, String args[]);
}
