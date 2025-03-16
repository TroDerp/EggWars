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
import me.rosillogames.eggwars.commands.setup.SetVoidHeight;
import me.rosillogames.eggwars.commands.setup.TeamList;
import me.rosillogames.eggwars.commands.setup.ToggleEditMode;
import me.rosillogames.eggwars.commands.setup.TpArena;
import me.rosillogames.eggwars.language.TranslationUtils;

public class CmdSetup implements TabExecutor
{
    private final Map<String, CommandArg> mainArgs = new HashMap();

    public CmdSetup()
    {
        this.addArg(new Help());
        this.addArg(new CreateArena());
        this.addArg(new CloneArena());
        this.addArg(new RemoveArena());
        this.addArg(new AddTeam());
        this.addArg(new RemoveTeam());
        this.addArg(new SetBounds());
        this.addArg(new SetCenter());
        this.addArg(new SetCountdownStart());
        this.addArg(new SetCountdownFull());
        this.addArg(new SetCountdownRelease());
        this.addArg(new SetWaitingLobby());
        this.addArg(new SetMaxPlayersPerTeam());
        this.addArg(new SetMinPlayers());
        this.addArg(new SetVoidHeight());
        this.addArg(new MoveTeam());
        this.addArg(new SetTeamEgg());
        this.addArg(new SetTeamRespawn());
        this.addArg(new SetTeamVillager());
        this.addArg(new SetTeamCageAdd());
        this.addArg(new SetTeamCageRemove());
        this.addArg(new SetMainLobby());
        this.addArg(new TpArena());
        this.addArg(new TeamList());
        this.addArg(new ToggleEditMode());
        this.addArg(new SetBungeeLobby());
        this.addArg(new GetGuiItem());
    }

    private void addArg(CommandArg argument)
    {
        this.mainArgs.put(argument.getName(), argument);
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

    public class Help extends CommandArg
    {
        public Help()
        {
            super("help", false);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            StringBuilder builder = new StringBuilder();

            if (args.length == 2 && CmdSetup.this.fixArg(args[1]) != null)
            {
                builder.append("\n" + CmdSetup.this.mainArgs.get(CmdSetup.this.fixArg(args[1])).getInfo(sender));
            }
            else
            {
                for (CommandArg arg : CmdSetup.this.mainArgs.values())
                {
                    builder.append("\n" + arg.getInfo(sender));
                }
            }

            TranslationUtils.sendMessage("commands.setup.help", sender, builder.toString());
            return true;
        }

        @Override
        public List<String> getCompleteArgs(CommandSender sender, String[] args)
        {
            if (args.length == 2)
            {
                List<String> list = new ArrayList();

                for (String key : CmdSetup.this.mainArgs.keySet())
                {
                    if (key.toLowerCase().startsWith(args[1].toLowerCase()))
                    {
                        list.add(key);
                    }
                }

                return list;
            }

            return new ArrayList();
        }

        public String getInfo(CommandSender sender)
        {
            return TranslationUtils.getMessage("commands.setup.help.info", sender, TranslationUtils.getMessage(this.getSyntaxTKey(), sender));
        }

        public String getSyntaxTKey()
        {
            return "commands.setup.help.syntax";
        }
    }
}
