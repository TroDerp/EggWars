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
        super(false);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        if (args.length < 2)
        {
            TranslationUtils.sendMessage("commands.toggleEditMode.usage", sender);
            return false;
        }

        Arena arena = EggWars.getArenaManager().cmdArenaByIdOrName(sender, args, 1);

        if (arena == null)
        {
            return false;
        }

        if (sender instanceof Player && (PlayerUtils.getEwPlayer((Player)sender)).isInArena())
        {
            TranslationUtils.sendMessage("commands.error.in_arena", sender);
            return false;
        }

        if (arena.getStatus().equals(ArenaStatus.SETTING))
        {
            if (!arena.isSetup())
            {
                TranslationUtils.sendMessage("commands.error.arena_not_set_up", sender, arena.getName());
                return false;
            }

            TranslationUtils.sendMessage("commands.toggleEditMode.success.saving", sender, new Object[] {arena.getName()});
            (new BukkitRunnable()
            {
                public void run()
                {
                    arena.saveArena();
                }
            }).runTaskLater(EggWars.instance, 40L);
            (new BukkitRunnable()
            {
                public void run()
                {
                    if (!arena.isSaving())
                    {
                        arena.reset(false);
                        TranslationUtils.sendMessage("commands.toggleEditMode.success.saved", sender, arena.getName());
                        this.cancel();
                    }
                }
            }).runTaskTimer(EggWars.instance, 50L, 10L);
        }
        else
        {
            TranslationUtils.sendMessage("commands.toggleEditMode.success.preparing", sender, arena.getName());
            arena.reset(true);//TODO can sometimes fail!?

            if (sender instanceof Player)
            {
                ((Player)sender).teleport(arena.getLobby() != null ? arena.getLobby() : arena.getWorld().getSpawnLocation());
            }

            TranslationUtils.sendMessage("commands.toggleEditMode.success", sender, arena.getName());
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
                if (arena.getId().toLowerCase().startsWith(args[1].toLowerCase()))
                {
                    list.add(arena.getId());
                }
            }
        }

        return list;
    }
}
