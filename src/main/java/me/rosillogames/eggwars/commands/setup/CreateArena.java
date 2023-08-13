package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.loaders.ArenaLoader;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.WorldController;

public class CreateArena extends CommandArg
{
    public CreateArena()
    {
        super(true);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        if (EggWars.bungee.isEnabled())
        {
            TranslationUtils.sendMessage("commands.error.bungee_mode", commandSender);
            return false;
        }

        if (args.length < 2)
        {
            TranslationUtils.sendMessage("commands.createArena.usage", commandSender);
            return false;
        }

        Player player = (Player)commandSender;
        String name = ArenaLoader.formulateName(args, 1);

        if (EggWars.getArenaManager().getArenaByName(name) != null)
        {
            TranslationUtils.sendMessage("commands.error.arena_already_exists", commandSender, name);
            return false;
        }
        else
        {
            boolean flag = WorldController.worldFolderExists(name);
            Arena arena = new Arena(name);

            if (!flag)
            {
                arena.getWorld().setSpawnLocation(new Location(arena.getWorld(), 0.5, 100.0, 0.5));

                if (arena.getWorld().getBlockAt(0, 99, 0).getType() == Material.AIR)
                {
                    arena.getWorld().getBlockAt(0, 99, 0).setType(Material.STONE);
                }
            }

            player.teleport(arena.getWorld().getSpawnLocation());
            player.setGameMode(GameMode.CREATIVE);
            TranslationUtils.sendMessage("commands.createArena.success", commandSender, arena.getName());
            EggWars.getArenaManager().addArena(arena);
            PlayerUtils.getEwPlayer(player).setSettingArena(arena);
            arena.sendToDo(player);
            return false;
        }
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        List<String> list = new ArrayList();

        if (args.length >= 2)
        {
            for (Arena arena : EggWars.getArenaManager().getArenas())
            {
                if (arena.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                {
                    list.add(arena.getName());
                }
            }
        }

        return list;
    }
}
