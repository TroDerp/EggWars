package me.rosillogames.eggwars.commands.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.managers.ArenaManager;
import me.rosillogames.eggwars.utils.WorldController;

public class CloneArena extends CommandArg
{
    public CloneArena()
    {
        super("cloneArena", false);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        if (args.length < 3)
        {
            this.sendUsage(sender);
            return false;
        }

        Arena arena = EggWars.getArenaManager().getArenaById(args[1]);

        if (arena == null)
        {
            TranslationUtils.sendMessage("commands.error.arena_does_not_exist", sender, args[1]);
            return false;
        }

        if (!arena.isSetup())
        {
            TranslationUtils.sendMessage("commands.error.arena_not_set_up", sender, arena.getName());
            return false;
        }

        String cloneName = ArenaManager.formulateName(args, 2);

        if (EggWars.getArenaManager().getArenaById(ArenaManager.getValidArenaID(cloneName)) != null)
        {
            TranslationUtils.sendMessage("commands.error.arena_already_exists", sender, cloneName);
            return false;
        }
        else
        {
            TranslationUtils.sendMessage("commands.cloneArena.cloning", sender, arena.getName(), cloneName);
            File cloneFile = new File(EggWars.arenasFolder, ArenaManager.getValidArenaID(cloneName));

            if (WorldController.copyFiles(arena.arenaFolder, cloneFile))
            {
                EggWars.getArenaManager().addArena(new Arena(cloneFile, cloneName));
                TranslationUtils.sendMessage("commands.cloneArena.success", sender, arena.getName(), cloneName);
            }
            else
            {
                TranslationUtils.sendMessage("commands.cloneArena.failed", sender, arena.getName(), cloneName);
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
                if (arena.getId().toLowerCase().startsWith(args[1].toLowerCase()))
                {
                    list.add(arena.getId());
                }
            }
        }
        else if (args.length >= 3)
        {
            for (Arena arena : EggWars.getArenaManager().getArenas())
            {
                if (arena.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                {//list names because the input will become the new arena's name
                    list.add(arena.getName());
                }
            }
        }

        return list;
    }
}
