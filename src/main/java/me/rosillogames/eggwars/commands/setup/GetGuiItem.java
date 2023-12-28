package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.commands.CommandArg;
import me.rosillogames.eggwars.language.TranslationUtils;

public class GetGuiItem extends CommandArg
{
    public GetGuiItem()
    {
        super(true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        if (args.length < 2)
        {
            TranslationUtils.sendMessage("commands.getGuiItem.usage", sender);
            return false;
        }

        Arena arena = EggWars.getArenaManager().cmdArenaByIdOrName(sender, args, 1);

        if (arena == null)
        {
            return false;
        }

        Player player = (Player)sender;
        player.getInventory().addItem(arena.getSetupGUI().getItem(player));
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
                    list.add(arena.getName());
                    list.add(arena.getId());
                }
            }
        }

        return list;
    }
}
