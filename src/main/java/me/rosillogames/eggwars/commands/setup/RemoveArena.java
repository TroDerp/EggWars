package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.ArenaSign;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.WorldController;

public class RemoveArena extends CommandArg
{
    public RemoveArena()
    {
        super(false);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        if (args.length != 2)
        {
            TranslationUtils.sendMessage("commands.removeArena.usage", commandSender);
            return false;
        }

        Arena arena = EggWars.getArenaManager().getArenaByName(args[1]);

        if (arena == null)
        {
            TranslationUtils.sendMessage("commands.error.arena_does_not_exist", commandSender, args[1]);
            return false;
        }

        if (arena.isSetup())
        {
            arena.getPlayers().forEach(ewplayer -> arena.leaveArena(ewplayer, true, true));
        }

        for (Entity entity : arena.getWorld().getEntities())
        {
            if (entity instanceof Player)
            {
                PlayerUtils.getEwPlayer((Player)entity).setSettingArena(null);
                PlayerUtils.tpToLobby(PlayerUtils.getEwPlayer((Player)entity), true);
            }
            else
            {
                entity.remove();
            }
        }

        EggWars.getArenaManager().removeArena(arena);

        for (ArenaSign ewsign : EggWars.signs)
        {
            if (ewsign.getArena().equals(arena))
            {
                EggWars.signs.remove(ewsign);

                if (ewsign.getLocation().getBlock().getState() instanceof Sign)
                {
                    Sign sign = (Sign)ewsign.getLocation().getBlock().getState();
                    sign.setLine(0, "");
                    sign.setLine(1, TranslationUtils.getMessage("setup.sign.arena.arena_removed"));
                    sign.setLine(2, "");
                    sign.setLine(3, "");
                }

                break;
            }
        }

        arena.setStatus(ArenaStatus.SETTING);
        WorldController.deleteFiles(arena.arenaFolder);
        WorldController.deleteWorld(WorldController.formatTmpWorldName(arena.getName()));
        TranslationUtils.sendMessage("commands.removeArena.success", commandSender, arena.getName());
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
