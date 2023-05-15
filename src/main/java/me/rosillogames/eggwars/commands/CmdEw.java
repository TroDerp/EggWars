package me.rosillogames.eggwars.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.ReloadType;
import me.rosillogames.eggwars.events.EwPluginReloadEvent;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class CmdEw implements TabExecutor
{
    private final Map<String, CommandArg> mainArgs = new HashMap();

    public CmdEw()
    {
        this.mainArgs.put("forceStart", new ForceStart());
        this.mainArgs.put("help", new Help());
        this.mainArgs.put("join", new Join());
        this.mainArgs.put("lobby", new Lobby());
        this.mainArgs.put("menu", new Menu());
        this.mainArgs.put("randomJoin", new RandomJoin());
        this.mainArgs.put("reload", new Reload());
    }

    @Override
    public boolean onCommand(CommandSender senderIn, Command commandIn, String s, String args[])
    {
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

        TranslationUtils.sendMessage("commands.ew.unknown", senderIn);
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

        if (args.length != 0 && this.fixArg(args[0]) != null)
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

    public static class ForceStart extends CommandArg
    {
        public ForceStart()
        {
            super(false);
        }

        @Override
        public boolean execute(CommandSender commandSender, String[] args)
        {
            if (!commandSender.hasPermission("eggwars.command.forcestart"))
            {
                TranslationUtils.sendMessage("commands.error.no_permission", commandSender);
                return false;
            }

            Arena arena = commandSender instanceof Player ? PlayerUtils.getEwPlayer((Player)commandSender).getArena() : null;

            if (args.length == 2)
            {
                arena = EggWars.getArenaManager().getArenaByName(args[1]);

                if (arena == null)
                {
                    TranslationUtils.sendMessage("commands.error.arena_does_not_exist", commandSender, args[1]);
                    return false;
                }
            }

            if (arena == null)
            {
                TranslationUtils.sendMessage("commands.force_start.usage", commandSender);
                return false;
            }

            if (!arena.isSetup())
            {
                TranslationUtils.sendMessage("commands.error.arena_not_set_up", commandSender);
                return false;
            }

            if (arena.forceStart())
            {
                TranslationUtils.sendMessage("commands.force_start.success", commandSender, arena.getName());
                return true;
            }

            if (!arena.getStatus().isLobby())
            {
                TranslationUtils.sendMessage("commands.force_start.failed.not_in_lobby", commandSender);
                return false;
            }

            TranslationUtils.sendMessage("commands.force_start.failed.not_enough_players", commandSender);
            return false;
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

    public static class Help extends CommandArg
    {
        public Help()
        {
            super(false);
        }

        @Override
        public boolean execute(CommandSender commandSender, String[] args)
        {
            TranslationUtils.sendMessage("commands.ew.help", commandSender);
            return true;
        }

        @Override
        public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
        {
            return new ArrayList();
        }
    }

    public static class Join extends CommandArg
    {
        public Join()
        {
            super(true);
        }

        @Override
        public boolean execute(CommandSender commandSender, String[] args)
        {
            if (!commandSender.hasPermission("eggwars.command.join"))
            {
                TranslationUtils.sendMessage("commands.error.no_permission", commandSender);
                return false;
            }

            if (args.length == 1)
            {
                TranslationUtils.sendMessage("commands.join.usage", commandSender);
                return false;
            }

            Player player = (Player)commandSender;

            if (PlayerUtils.getEwPlayer(player).isInArena())
            {
                TranslationUtils.sendMessage("commands.error.in_arena", commandSender);
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
                TranslationUtils.sendMessage("commands.error.arena_not_set_up", player, arena.getName());
                return false;
            }

            if (arena.getStatus().equals(ArenaStatus.SETTING))
            {
                TranslationUtils.sendMessage("commands.error.arena_in_edit_mode", player);
                return false;
            }

            if (arena.getStatus().equals(ArenaStatus.LOBBY) || arena.getStatus().equals(ArenaStatus.STARTING))
            {
                if (arena.isFull())
                {
                    TranslationUtils.sendMessage("gameplay.lobby.cant_join.full", player.getPlayer());
                    return false;
                }
                else
                {
                    arena.joinArena(PlayerUtils.getEwPlayer(player), false, false);
                    return true;
                }
            }
            else if (arena.getStatus().equals(ArenaStatus.FINISHING) || !EggWars.config.canSpectJoin)
            {
                TranslationUtils.sendMessage("gameplay.lobby.cant_join.ingame", player.getPlayer());
                return false;
            }

            arena.joinArena(PlayerUtils.getEwPlayer(player), true, true);
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

    public static class Lobby extends CommandArg
    {
        public Lobby()
        {
            super(true);
        }

        @Override
        public boolean execute(CommandSender commandSender, String[] args)
        {
            if (!commandSender.hasPermission("eggwars.command.lobby"))
            {
                TranslationUtils.sendMessage("commands.error.no_permission", commandSender);
                return false;
            }

            EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)commandSender);

            if (ewplayer.isInArena())
            {
                ewplayer.getArena().leaveArena(ewplayer, true, false);
                return true;
            }

            PlayerUtils.tpToLobby(ewplayer, true);
            Bukkit.getPluginManager().callEvent(new PlayerMoveEvent(ewplayer.getPlayer(), ewplayer.getPlayer().getLocation(), ewplayer.getPlayer().getLocation()));
            return true;
        }

        @Override
        public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
        {
            return new ArrayList();
        }
    }

    public static class Menu extends CommandArg
    {
        public Menu()
        {
            super(true);
        }

        @Override
        public boolean execute(CommandSender commandSender, String[] args)
        {
            if (!commandSender.hasPermission("eggwars.command.menu"))
            {
                TranslationUtils.sendMessage("commands.error.no_permission", commandSender);
                return false;
            }

            Player player = (Player)commandSender;
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);

            if (ewplayer.isInArena())
            {
                TranslationUtils.sendMessage("commands.error.in_arena", commandSender);
                return false;
            }

            ewplayer.getMenu().openMainInv();
            return true;
        }

        @Override
        public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
        {
            return new ArrayList();
        }
    }

    public static class RandomJoin extends CommandArg
    {
        public RandomJoin()
        {
            super(true);
        }

        @Override
        public boolean execute(CommandSender commandSender, String[] args)
        {
            if (!commandSender.hasPermission("eggwars.command.join"))
            {
                TranslationUtils.sendMessage("commands.error.no_permission", commandSender);
                return false;
            }

            if (PlayerUtils.getEwPlayer((Player)commandSender).isInArena())
            {
                TranslationUtils.sendMessage("commands.error.in_arena", commandSender);
                return false;
            }

            List<Arena> list = new ArrayList();

            for (Arena arena : EggWars.getArenaManager().getArenas())
            {
                if (arena.getStatus().isLobby() && arena.isSetup())
                {
                    list.add(arena);
                }
            }

            Collections.shuffle(list);

            if (list.isEmpty())
            {
                TranslationUtils.sendMessage("commands.error.no_arenas", commandSender);
                return false;
            }

            for (Arena randomarena : list)
            {
                if (!randomarena.isFull() && (randomarena.getStatus().equals(ArenaStatus.LOBBY) || randomarena.getStatus().equals(ArenaStatus.STARTING)))
                {
                    randomarena.joinArena(PlayerUtils.getEwPlayer((Player)commandSender), false, false);
                    return true;
                }
                else if (randomarena.getStatus().equals(ArenaStatus.IN_GAME) && EggWars.config.canSpectJoin)
                {
                    randomarena.joinArena(PlayerUtils.getEwPlayer((Player)commandSender), true, true);
                    return true;
                }
            }

            TranslationUtils.sendMessage("commands.error.no_arenas", commandSender);
            return false;
        }

        @Override
        public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
        {
            return new ArrayList();
        }
    }

    public static class Reload extends CommandArg
    {
        public Reload()
        {
            super(false);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            if (!sender.hasPermission("eggwars.command.reload"))
            {
                TranslationUtils.sendMessage("commands.error.no_permission", sender);
                return false;
            }

            if (args.length == 1)
            {
                TranslationUtils.sendMessage("commands.reload.usage", sender);
                return false;
            }

            ReloadType type = ReloadType.parse(args[1]);

            if (type != null)
            {
                type.reload(sender);
                TranslationUtils.sendMessage("commands.reload.success", sender);
                Bukkit.getPluginManager().callEvent(new EwPluginReloadEvent(type));
                return true;
            }

            TranslationUtils.sendMessage("commands.reload.usage", sender);
            return false;
        }

        @Override
        public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
        {
            List<String> list = new ArrayList();

            if (args.length == 2)
            {
                for (int i = 0; i < ReloadType.values().length; i++)
                {
                    String s1 = ReloadType.values()[i].getNameKey();

                    if (s1.startsWith(args[1].toLowerCase()))
                    {
                        list.add(s1);
                    }
                }
            }

            return list;
        }
    }
}
