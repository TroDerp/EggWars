package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class ToggleEditMode extends CommandArg
{
    public ToggleEditMode()
    {
        super(true);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        if (args.length != 2)
        {
            TranslationUtils.sendMessage("commands.toggleEditMode.usage", commandSender);
            return false;
        }

        Player player = (Player)commandSender;
        Arena arena = EggWars.getArenaManager().getArenaByName(args[1]);

        if (arena == null)
        {
            TranslationUtils.sendMessage("commands.error.arena_does_not_exist", commandSender, args[1]);
            return false;
        }

        if (arena.getStatus().equals(ArenaStatus.SETTING))
        {
            if (!arena.isSetup())
            {
                TranslationUtils.sendMessage("commands.error.arena_not_set_up", commandSender, arena.getName());
                return false;
            }

            TranslationUtils.sendMessage("commands.toggleEditMode.success.saving", commandSender, new Object[] {arena.getName()});
            (new BukkitRunnable()
            {
                public void run()
                {
                    arena.saveArena();
                }
            }).runTaskLater(EggWars.instance, 50L);
            (new BukkitRunnable()
            {
                public void run()
                {
                    arena.reset(false);
                    TranslationUtils.sendMessage("commands.toggleEditMode.success.saved", commandSender, arena.getName());
                }
            }).runTaskLater(EggWars.instance, 100L);
            PlayerUtils.getEwPlayer(player).setSettingArena(null);
        }
        else
        {
            TranslationUtils.sendMessage("commands.toggleEditMode.success.preparing", commandSender, arena.getName());
            arena.reset(true);
            player.teleport(arena.getLobby() != null ? arena.getLobby() : arena.getWorld().getSpawnLocation());
            TranslationUtils.sendMessage("commands.toggleEditMode.success", commandSender, arena.getName());
            PlayerUtils.getEwPlayer(player).setSettingArena(arena);
        }

        return true;
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

        return list;
    }
}
