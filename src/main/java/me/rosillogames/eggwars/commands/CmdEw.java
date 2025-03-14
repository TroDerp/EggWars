package me.rosillogames.eggwars.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import me.rosillogames.eggwars.language.Language;
import me.rosillogames.eggwars.language.LanguageManager;
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
        this.mainArgs.put("lang", new Lang());
        this.mainArgs.put("lobby", new Lobby());
        this.mainArgs.put("menu", new Menu());
        this.mainArgs.put("randomJoin", new RandomJoin());
        this.mainArgs.put("reload", new Reload());
    }

    @Override
    public boolean onCommand(CommandSender senderIn, Command cmdIn, String s, String args[])
    {
        if (args.length != 0 && this.fixArg(args[0]) != null)
        {
            CommandArg arg = this.mainArgs.get(this.fixArg(args[0]));

            if (arg.isPlayersOnly() && !(senderIn instanceof Player))
            {
                TranslationUtils.sendMessage("commands.error.only_players", senderIn);
                return true;
            }

            if (arg.getPermission() != null && !senderIn.hasPermission(arg.getPermission()))
            {
                TranslationUtils.sendMessage("commands.error.no_permission", senderIn);
                return true;
            }

            arg.execute(senderIn, args);
            return true;
        }

        TranslationUtils.sendMessage("commands.ew.unknown", senderIn);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender senderIn, Command cmdIn, String s, String args[])
    {
        if (args.length == 1)
        {
            List<String> list = new ArrayList();

            for (Entry<String, CommandArg> entry : this.mainArgs.entrySet())
            {
                String key = entry.getKey();
                CommandArg arg = entry.getValue();

                if (key.toLowerCase().startsWith(args[0].toLowerCase()) && (arg.getPermission() == null || senderIn.hasPermission(arg.getPermission())))
                {
                    list.add(key);
                }
            }

            return list;
        }

        if (args.length != 0 && this.fixArg(args[0]) != null)
        {
            CommandArg arg = this.mainArgs.get(this.fixArg(args[0]));
            return arg.getCompleteArgs(senderIn, args);
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
        public boolean execute(CommandSender sender, String[] args)
        {
            Arena arena = sender instanceof Player ? PlayerUtils.getEwPlayer((Player)sender).getArena() : null;

            if (args.length >= 2 && (arena = EggWars.getArenaManager().cmdArenaByIdOrName(sender, args, 1)) == null)
            {
                return false;
            }

            if (arena == null)
            {
                TranslationUtils.sendMessage("commands.force_start.usage", sender);
                return false;
            }

            if (!arena.isSetup())
            {
                TranslationUtils.sendMessage("commands.error.arena_not_set_up", sender);
                return false;
            }

            if (arena.forceStart())
            {
                TranslationUtils.sendMessage("commands.force_start.success", sender, arena.getName());
                return true;
            }

            if (!arena.getStatus().isLobby())
            {
                TranslationUtils.sendMessage("commands.force_start.failed.not_in_lobby", sender);
                return false;
            }

            TranslationUtils.sendMessage("commands.force_start.failed.not_enough_players", sender);
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
                    if (arena.getId().toLowerCase().startsWith(args[1].toLowerCase()))
                    {
                        list.add(arena.getId());
                    }
                }
            }

            return list;
        }

        @Override
        public String getPermission()
        {
            return "eggwars.command.forcestart";
        }
    }

    public static class Help extends CommandArg
    {
        public Help()
        {
            super(false);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            TranslationUtils.sendMessage("commands.ew.help", sender);
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
        public boolean execute(CommandSender sender, String[] args)
        {
            if (args.length <= 1)
            {
                TranslationUtils.sendMessage("commands.join.usage", sender);
                return false;
            }

            EwPlayer player = PlayerUtils.getEwPlayer((Player)sender);

            if (player.isInArena())
            {
                TranslationUtils.sendMessage("commands.error.in_arena", sender);
                return false;
            }

            Arena arena = EggWars.getArenaManager().cmdArenaByIdOrName(sender, args, 1);

            if (arena == null)
            {
                return false;
            }

            if (!arena.isSetup())
            {
                TranslationUtils.sendMessage("commands.error.arena_not_set_up", sender, arena.getName());
                return false;
            }

            if (arena.getStatus().equals(ArenaStatus.SETTING))
            {
                TranslationUtils.sendMessage("commands.error.arena_in_edit_mode", sender);
                return false;
            }

            if (arena.getStatus().isLobby())
            {
                if (arena.isFull())
                {
                    TranslationUtils.sendMessage("gameplay.lobby.cant_join.full", sender);
                    return false;
                }
                else
                {
                    arena.joinArena(player, false, false);
                    return true;
                }
            }
            else if (arena.getStatus().equals(ArenaStatus.FINISHING) || !EggWars.config.canSpectJoin)
            {
                TranslationUtils.sendMessage("gameplay.lobby.cant_join.ingame", sender);
                return false;
            }

            arena.joinArena(player, false, true);
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

        @Override
        public String getPermission()
        {
            return "eggwars.command.join";
        }
    }

    public static class Lang extends CommandArg
    {
        public Lang()
        {
            super(true);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            if (args.length <= 1)
            {
                TranslationUtils.sendMessage("commands.lang.usage", sender);
                return false;
            }

            Language lang = EggWars.languageManager().getLanguages().get(args[1]);

            if (args[1].equals(LanguageManager.DEFAULT_NAME))
            {
                lang = LanguageManager.getDefaultLanguage();
            }

            if (lang == null)
            {
                TranslationUtils.sendMessage("commands.lang.failed.invalid", sender);
                return false;
            }

            String prevLang = PlayerUtils.getLangId((Player)sender);
            String newLang = lang.getLocale();

            if (prevLang.equals(newLang))
            {
                TranslationUtils.sendMessage("commands.lang.failed.no_change", sender);
                return false;
            }

            PlayerUtils.setLangId((Player)sender, newLang);
            TranslationUtils.sendMessage("commands.lang.success", sender, prevLang, newLang);
            return true;
        }

        @Override
        public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
        {
            List<String> list = new ArrayList<>();

            if (args.length == 2)
            {
                for (String s : EggWars.languageManager().getLanguages().keySet())
                {
                    if (s.toLowerCase().startsWith(args[1].toLowerCase()))
                    {
                        list.add(s);
                    }
                }

                if (LanguageManager.DEFAULT_NAME.toLowerCase().startsWith(args[1].toLowerCase()))
                {
                    list.add(LanguageManager.DEFAULT_NAME);
                }
            }

            return list;
        }

        @Override
        public String getPermission()
        {
            return "eggwars.command.lang";
        }
    }

    public static class Lobby extends CommandArg
    {
        public Lobby()
        {
            super(true);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)sender);

            if (ewplayer.isInArena())
            {
                ewplayer.getArena().leaveArena(ewplayer, true, false);
                return true;
            }

            Player pl = ewplayer.getPlayer();
            PlayerUtils.tpToLobby(pl, true);
            Bukkit.getPluginManager().callEvent(new PlayerMoveEvent(pl, pl.getLocation(), pl.getLocation()));
            return true;
        }

        @Override
        public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
        {
            return new ArrayList();
        }

        @Override
        public String getPermission()
        {
            return "eggwars.command.lobby";
        }
    }

    public static class Menu extends CommandArg
    {
        public Menu()
        {
            super(true);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            Player player = (Player)sender;
            EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);

            if (ewplayer.isInArena())
            {
                TranslationUtils.sendMessage("commands.error.in_arena", sender);
                return false;
            }

            ewplayer.getProfile().openMainInv();
            return true;
        }

        @Override
        public List<String> getCompleteArgs(CommandSender commandSender, String[] args)
        {
            return new ArrayList();
        }

        @Override
        public String getPermission()
        {
            return "eggwars.command.menu";
        }
    }

    public static class RandomJoin extends Join //same join permission
    {
        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            if (PlayerUtils.getEwPlayer((Player)sender).isInArena())
            {
                TranslationUtils.sendMessage("commands.error.in_arena", sender);
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
                TranslationUtils.sendMessage("commands.error.no_arenas", sender);
                return false;
            }

            for (Arena randomarena : list)
            {
                if (!randomarena.isFull() && randomarena.getStatus().isLobby())
                {
                    randomarena.joinArena(PlayerUtils.getEwPlayer((Player)sender), false, false);
                    return true;
                }
                else if (randomarena.getStatus().equals(ArenaStatus.IN_GAME) && EggWars.config.canSpectJoin)
                {
                    randomarena.joinArena(PlayerUtils.getEwPlayer((Player)sender), false, true);
                    return true;
                }
            }

            TranslationUtils.sendMessage("commands.error.no_arenas", sender);
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

        @Override
        public String getPermission()
        {
            return "eggwars.command.reload";
        }
    }
}
