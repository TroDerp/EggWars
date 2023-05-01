package me.rosillogames.eggwars.commands.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.util.FileUtil;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;

public class CloneArena extends CommandArg
{
    public CloneArena()
    {
        super(false);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        if (args.length != 3)
        {
            TranslationUtils.sendMessage("commands.cloneArena.usage", commandSender);
            return false;
        }

        Arena arena = EggWars.getArenaManager().getArenaByName(args[1]);

        if (arena == null)
        {
            TranslationUtils.sendMessage("commands.error.arena_does_not_exist", commandSender, args[1]);
            return false;
        }

        if (!arena.isSetup())
        {
            TranslationUtils.sendMessage("commands.error.arena_not_set_up", commandSender, arena.getName());
            return false;
        }

        String cloneName = args[2];

        if (EggWars.getArenaManager().getArenaByName(cloneName) != null)
        {
            TranslationUtils.sendMessage("commands.error.arena_already_exists", commandSender, cloneName);
            return false;
        }
        else
        {
            TranslationUtils.sendMessage("commands.cloneArena.cloning", commandSender, arena.getName(), cloneName);
            File cloneFile = new File(EggWars.arenasFolder, cloneName);

            if (FileUtil.copy(arena.arenaFolder, cloneFile))
            {
                EggWars.getArenaManager().addArena(new Arena(cloneFile, cloneName));
                TranslationUtils.sendMessage("commands.cloneArena.success", commandSender, arena.getName(), cloneName);
            }
            else
            {
                TranslationUtils.sendMessage("commands.cloneArena.failed", commandSender, arena.getName(), cloneName);
            }

            return false;
        }
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        List<String> list = new ArrayList();

        if (args.length == 2)
        {
            for (Arena arena : EggWars.getArenaManager().getArenas())
            {
                if (arena.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                {
                    list.add(arena.getName());
                }
            }
        }
        else if (args.length == 3)
        {
            for (Arena arena : EggWars.getArenaManager().getArenas())
            {
                if (arena.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                {
                    list.add(arena.getName());
                }
            }
        }

        return list;
    }
}
