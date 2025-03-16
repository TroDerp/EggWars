package me.rosillogames.eggwars.commands;

import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.command.CommandSender;

import me.rosillogames.eggwars.language.TranslationUtils;

public abstract class CommandArg
{
    private final String name;
    private final boolean isPlayersOnly;

    public CommandArg(String argName, boolean playersOnly)
    {
        this.name = argName;
        this.isPlayersOnly = playersOnly;
    }

    public boolean isPlayersOnly()
    {
        return this.isPlayersOnly;
    }

    public String getName()
    {
        return this.name;
    }

    public void sendUsage(CommandSender sender)
    {
        TranslationUtils.sendMessage("commands.usage", sender, TranslationUtils.getMessage(this.getSyntaxTKey(), sender));
    }

    public String getInfo(CommandSender sender)
    {
        return TranslationUtils.getMessage("commands." + this.name + ".info", sender, TranslationUtils.getMessage(this.getSyntaxTKey(), sender));
    }

    public String getSyntaxTKey()
    {
        return "commands." + this.name + ".syntax";
    }

    @Nullable
    public String getPermission()
    {//Only used in /ew commands by now 
        return null;
    }

    public abstract boolean execute(CommandSender commandSender, String args[]);

    public abstract List<String> getCompleteArgs(CommandSender commandSender, String args[]);
}
