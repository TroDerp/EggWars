package me.rosillogames.eggwars.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.commands.setup.AddTeam;
import me.rosillogames.eggwars.commands.setup.CloneArena;
import me.rosillogames.eggwars.commands.setup.CreateArena;
import me.rosillogames.eggwars.commands.setup.GetGuiItem;
import me.rosillogames.eggwars.commands.setup.Help;
import me.rosillogames.eggwars.commands.setup.MoveTeam;
import me.rosillogames.eggwars.commands.setup.RemoveArena;
import me.rosillogames.eggwars.commands.setup.RemoveTeam;
import me.rosillogames.eggwars.commands.setup.SetWaitingLobby;
import me.rosillogames.eggwars.commands.setup.SetBounds;
import me.rosillogames.eggwars.commands.setup.SetBungeeLobby;
import me.rosillogames.eggwars.commands.setup.SetCenter;
import me.rosillogames.eggwars.commands.setup.SetCountdownStart;
import me.rosillogames.eggwars.commands.setup.SetCountdownFull;
import me.rosillogames.eggwars.commands.setup.SetCountdownRelease;
import me.rosillogames.eggwars.commands.setup.SetMainLobby;
import me.rosillogames.eggwars.commands.setup.SetMaxPlayersPerTeam;
import me.rosillogames.eggwars.commands.setup.SetMinPlayers;
import me.rosillogames.eggwars.commands.setup.SetTeamEgg;
import me.rosillogames.eggwars.commands.setup.SetTeamCageAdd;
import me.rosillogames.eggwars.commands.setup.SetTeamCageRemove;
import me.rosillogames.eggwars.commands.setup.SetTeamRespawn;
import me.rosillogames.eggwars.commands.setup.SetTeamVillager;
import me.rosillogames.eggwars.commands.setup.TeamList;
import me.rosillogames.eggwars.commands.setup.ToggleEditMode;
import me.rosillogames.eggwars.commands.setup.TpArena;
import me.rosillogames.eggwars.language.TranslationUtils;

public class CmdSetup implements TabExecutor
{
    private final Map<String, CommandArg> mainArgs = new HashMap();

    public CmdSetup()
    {
        this.mainArgs.put("help", new Help());
        this.mainArgs.put("createArena", new CreateArena());
        this.mainArgs.put("cloneArena", new CloneArena());
        this.mainArgs.put("removeArena", new RemoveArena());
        this.mainArgs.put("addTeam", new AddTeam());
        this.mainArgs.put("removeTeam", new RemoveTeam());
        this.mainArgs.put("setBounds", new SetBounds());
        this.mainArgs.put("setCenter", new SetCenter());
        this.mainArgs.put("setStartCountdown", new SetCountdownStart());
        this.mainArgs.put("setFullCountdown", new SetCountdownFull());
        this.mainArgs.put("setReleaseCountdown", new SetCountdownRelease());
        this.mainArgs.put("setWaitingLobby", new SetWaitingLobby());
        this.mainArgs.put("setMaxPlayersPerTeam", new SetMaxPlayersPerTeam());
        this.mainArgs.put("setMinPlayers", new SetMinPlayers());
        this.mainArgs.put("moveTeam", new MoveTeam());
        this.mainArgs.put("setTeamEgg", new SetTeamEgg());
        this.mainArgs.put("setTeamRespawn", new SetTeamRespawn());
        this.mainArgs.put("setTeamVillager", new SetTeamVillager());
        this.mainArgs.put("addTeamCage", new SetTeamCageAdd());
        this.mainArgs.put("removeTeamCage", new SetTeamCageRemove());
        this.mainArgs.put("setMainLobby", new SetMainLobby());
        this.mainArgs.put("tpArena", new TpArena());
        this.mainArgs.put("teamList", new TeamList());
        this.mainArgs.put("toggleEditMode", new ToggleEditMode());
        this.mainArgs.put("setBungeeLobby", new SetBungeeLobby());
        this.mainArgs.put("getGuiItem", new GetGuiItem());
    }

    @Override
    public boolean onCommand(CommandSender senderIn, Command commandIn, String s, String args[])
    {
        if (!senderIn.hasPermission("eggwars.setup"))
        {
            TranslationUtils.sendMessage("commands.error.no_permission", senderIn);
            return true;
        }

        if (args.length != 0 && this.fixArg(args[0]) != null)
        {
            CommandArg arg = this.mainArgs.get(this.fixArg(args[0]));

            if (arg.isPlayersOnly() && !(senderIn instanceof Player))
            {
                TranslationUtils.sendMessage("commands.error.only_players", senderIn);
                return true;
            }

            arg.execute(senderIn, args);
            return true;
        }

        TranslationUtils.sendMessage("commands.setup.unknown", senderIn);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String args[])
    {
        if (args.length == 1)
        {
            List list = new ArrayList();

            for (String mainArg : this.mainArgs.keySet())
            {
                if (mainArg.toLowerCase().startsWith(args[0].toLowerCase()))
                {
                    list.add(mainArg);
                }
            }

            return list;
        }

        if (commandSender instanceof Player && args.length != 0 && this.fixArg(args[0]) != null)
        {
            CommandArg arg = this.mainArgs.get(this.fixArg(args[0]));
            return arg.getCompleteArgs(commandSender, args);
        }

        return new ArrayList();
    }

    private String fixArg(String arg)
    {
        for (String s : this.mainArgs.keySet())
        {
            if (arg.equalsIgnoreCase(s))
            {
                return s;
            }
        }

        return null;
    }
}
