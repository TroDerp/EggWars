package me.rosillogames.eggwars.commands.setup;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.SetupGUI;
import me.rosillogames.eggwars.commands.CommandArg;

public class GetGuiItem extends CommandArg
{
    public GetGuiItem()
    {
        super(true);
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args)
    {
        Player player = (Player)commandSender;

        if (Arena.checkEditArena(player) == null)
        {
            return false;
        }

        player.getInventory().addItem(SetupGUI.getSetupGUIItem().getTranslated(player));
        return true;
    }

    @Override
    public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
    {
        return new ArrayList();
    }
}
