package me.rosillogames.eggwars.arena;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.game.Finish;
import me.rosillogames.eggwars.arena.game.Lobby;
import me.rosillogames.eggwars.arena.shop.Category;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.HealthType;
import me.rosillogames.eggwars.enums.ItemType;
import me.rosillogames.eggwars.enums.Mode;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.events.EwPlayerJoinArenaEvent;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.managers.ArenaManager;
import me.rosillogames.eggwars.managers.TradingManager;
import me.rosillogames.eggwars.menu.TeamsMenu;
import me.rosillogames.eggwars.menu.VotingMenus;
import me.rosillogames.eggwars.objects.ArenaSign;
import me.rosillogames.eggwars.objects.Cage;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.ConfigAccessor;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.WorldController;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class Arena
{
    //Universal
    public final File arenaFolder;
    private final String name;
    private final String identifier;
    private final Map<TeamType, Team> teams = Maps.newEnumMap(TeamType.class);
    //Use vector instead of location to skip an issue with worlds when used loc.equals(other)
    private final Map<Vector, Generator> generators = Maps.newHashMap();
    //Arena status will now always be "Setup" at first, before completing init or when arena is newly created
    private ArenaStatus status = ArenaStatus.SETTING;

    //For game
    private World world;
    private final Set<EwPlayer> players = Sets.newHashSet();
    private final Map<Location, BlockState> replacedBlocks = Maps.<Location, BlockState>newHashMap();
    private ItemType itemType = ItemType.NORMAL;
    private HealthType healthType = HealthType.NORMAL;
    private final Map<EwPlayer, ItemType> itemsVotes = new HashMap();
    private final Map<EwPlayer, HealthType> healthVotes = new HashMap();
    //TODO: setup new menus when arena does init, depending if it's setup it will load SetupGUI or this one with voting, etc...
    private TeamsMenu teamSelectMenu;
    private VotingMenus votingMenus;
    @Nullable
    public Category specialTrades;//TODO
    private boolean forced = false;
    private boolean saving = false;
    private int currCountdown;

    //Constant stored settings
    public /*TODO: was private */ boolean customTrades = false;
    private Location lobby;
    private Location center;
    private Bounds boundaries;
    private int maxTeamPlayers;
    /** Min players required to start game */
    private int minPlayers;
    /** Start count down (in seconds), when waiting for the game to start (and players can still join). **/
    private int startCountdown;
    /** Full count down (in seconds), when the arena is full the counter changes to this.
     ** It is optional in the configuration files. If not set, the counter will not change. **/
    private int fullCountdown;
    /** Release count down (in seconds). It is used when waiting on cages, after teleport to cages. **/
    private int releaseCountdown;
    private final Scoreboards scores = new Scoreboards(this);
    private final SetupGUI setupGUI;

    public Arena(String name)
    {
        this.name = name;
        this.identifier = ArenaManager.getValidArenaID(name);
        this.arenaFolder = new File(EggWars.arenasFolder, this.identifier);

        if (!this.arenaFolder.exists())
        {
            this.arenaFolder.mkdirs();
        }
        else if (!this.arenaFolder.isDirectory())
        {
            this.arenaFolder.delete();
            this.arenaFolder.mkdirs();
        }

        this.world = WorldController.createArenaInitWorld(this.identifier);
        this.lobby = null;
        this.center = null;
        this.boundaries = new Bounds(null, null);
        this.maxTeamPlayers = 0;
        this.minPlayers = 0;
        this.startCountdown = -1;
        this.fullCountdown = -1;
        this.releaseCountdown = -1;
        this.setupGUI = new SetupGUI(this);
    }

    /** Only created when an arena is loaded or cloned.
     ** @param fldrIn is the arena folder
     ** @param newName is the override name for the arena, it is used when cloning
     **/
    public Arena(File fldrIn, @Nullable String newName)
    {
        this.arenaFolder = fldrIn;
        this.identifier = fldrIn.getName();
        ConfigAccessor configaccessor = new ConfigAccessor(EggWars.instance, new File(fldrIn, "arena.yml"));
        FileConfiguration fileconf = configaccessor.getConfig();

        if (newName != null)
        {
            fileconf.set("Name", newName);
            configaccessor.saveConfig();
        }

        this.name = fileconf.getString("Name");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loading arena " + this.name + ".");
        World world = WorldController.regenArena(this);
        loadIfPresent(fileconf, "Lobby", this::setLobby);
        loadIfPresent(fileconf, "Center", this::setCenter);
        this.boundaries = Bounds.deserialize(fileconf.getString("Bounds"));
        this.maxTeamPlayers = fileconf.getInt("MaxPlayersPerTeam");
        this.minPlayers = fileconf.getInt("MinPlayers");
        this.startCountdown = fileconf.getInt("StartCountdown", -1);
        this.fullCountdown = fileconf.getInt("FullCountdown", -1);
        this.releaseCountdown = fileconf.getInt("ReleaseCountdown", -1);
        this.customTrades = fileconf.getBoolean("ArenaSpecificTrades", false);

        if (this.customTrades)
        {
            EggWars.instance.saveCustomResource("custom/" + TradingManager.SPEC_TRADES_FILE, new File(this.arenaFolder, TradingManager.SPEC_TRADES_FILE), false);
        }

        for (TeamType teamtype : TeamType.values())
        {
            String teamtypeid = "Team." + teamtype.id();

            if (!fileconf.contains(teamtypeid))
            {
                continue;
            }

            Team team = new Team(this, teamtype);
            loadSection(fileconf, teamtypeid + ".Cages", (key) ->
            {
                team.addCage(Locations.fromString(fileconf.getString(key)));
            });
            loadIfPresent(fileconf, teamtypeid + ".Respawn", team::setRespawn);
            loadIfPresent(fileconf, teamtypeid + ".Villager", team::setVillager);
            loadIfPresent(fileconf, teamtypeid + ".Egg", team::setEgg);
            this.teams.put(teamtype, team);
        }

        loadSection(fileconf, "Generator", (key) ->
        {
            Generator generator = new Generator(Locations.fromString(fileconf.getString(key + ".Loc")), fileconf.getInt(key + ".DefLevel"), fileconf.getString(key + ".Type"), this);
            this.generators.put(generator.getBlock().toVector(), generator);
        });
        this.setWorld(world);
        this.setupGUI = new SetupGUI(this);
        this.reset(!this.isSetup());
    }

    private static void loadIfPresent(FileConfiguration config, String key, Consumer<Location> cons)
    {
        if (config.contains(key))
        {
            cons.accept(Locations.fromString(config.getString(key)));
        }
    }

    private static void loadSection(FileConfiguration config, String key, Consumer<String> cons)
    {
        ConfigurationSection section;

        if ((section = config.getConfigurationSection(key)) != null)
        {
            for (String subKey : section.getKeys(false))
            {
                cons.accept(section.getParent().getCurrentPath() + "." + section.getName() + "." + subKey);
            }
        }
    }

    public Mode getMode()
    {
        return this.maxTeamPlayers > 1 ? Mode.TEAM : Mode.SOLO;
    }

    public void loadShop()
    {
        this.specialTrades = null;

        if (this.customTrades)
        {
            EggWars.instance.getLogger().log(Level.INFO, "Loading data for arena \"" + this.getName() + "\" specific trades...");

            try
            {
                this.specialTrades = EggWars.getTradingManager().loadSpecialCategory(this.arenaFolder, this.itemType);
                this.specialTrades.buildMenu();
            }
            catch (Exception ex)
            {
                EggWars.instance.getLogger().log(Level.WARNING, "Error loading special shop category for arena \"" + this.getName() + "\": ", ex);
            }
        }
    }

    public Map<Location, BlockState> getReplacedBlocks()
    {
        return new HashMap(this.replacedBlocks);
    }

    public void addReplacedBlock(BlockState block)
    {
        if (!this.replacedBlocks.containsKey(block.getLocation()))
        {//check is very important for locations that had changed blocks multiple times
            this.replacedBlocks.put(block.getLocation(), block);
        }
    }

    public boolean canBreakOrReplace(BlockState block)
    {
        return this.replacedBlocks.containsKey(block.getLocation()) || EggWars.config.breakableBlocks.contains(block.getType()) || block.getType().isAir();
    }

    public Map<TeamType, Team> getTeams()
    {
        return new HashMap(this.teams);
    }

    public void removeTeam(TeamType teamTypes)
    {
        this.teams.remove(teamTypes);
    }

    public void addTeam(TeamType teamTypes)
    {
        this.teams.put(teamTypes, new Team(this, teamTypes));
    }

    public boolean moveTeam(TeamType oldTeam, TeamType newTeam)
    {//There are a lot of "updateTeam" to solve a problem where the setupGui used the wrong team color
        Team team = this.teams.remove(oldTeam);
        this.setupGUI.updateTeamInv(oldTeam, false);
        boolean flag = false;

        if (this.teams.containsKey(newTeam))
        {
            Team team1 = this.teams.remove(newTeam);
            this.setupGUI.updateTeamInv(newTeam, false);
            team1.setType(oldTeam);
            this.teams.put(oldTeam, team1);
            this.updateSetupTeam(oldTeam);
            flag = true;
        }

        team.setType(newTeam);
        this.teams.put(newTeam, team);
        this.updateSetupTeam(newTeam);
        return flag;
    }

    public Map<Vector, Generator> getGenerators()
    {
        return new HashMap(this.generators);
    }

    @Nullable
    public Generator removeGenerator(Vector loc)
    {
        return this.generators.remove(loc);
    }

    /**
     * Puts the specified generator to its location on genMap. Returns the previous generator
     * that was on the same position, if there was already one.
     */
    @Nullable
    public Generator putGenerator(Generator gen)
    {
        return this.generators.put(gen.getBlock().toVector(), gen);
    }

    public String getName()
    {
        return this.name;
    }

    public String getId()
    {
        return this.identifier;
    }

    public ArenaStatus getStatus()
    {
        return this.status;
    }

    public void setStatus(ArenaStatus status)
    {
        this.status = status;
    }

    public World getWorld()
    {
        return this.world;
    }

    public void setWorld(World worldIn)
    {
        this.world = worldIn;

        if (this.lobby != null)
        {
            this.lobby.setWorld(worldIn);
        }

        if (this.center != null)
        {
            this.center.setWorld(worldIn);
        }

        this.boundaries.setWorld(worldIn);
        this.getTeams().values().forEach(Team::setArenaWorld);
        this.getGenerators().values().forEach(Generator::setArenaWorld);
    }

    public int getMaxTeamPlayers()
    {
        return this.maxTeamPlayers;
    }

    public void setMaxTeamPlayers(int i)
    {
        this.maxTeamPlayers = i;
    }

    public int getMaxPlayers()
    {
        return this.teams.size() * this.maxTeamPlayers;
    }

    public Set<EwPlayer> getPlayers()
    {
        return new HashSet(this.players);
    }

    public int getFullCountdown()
    {
        return this.fullCountdown;
    }

    public void setFullCountdown(int i)
    {
        this.fullCountdown = i;
    }

    public Bounds getBounds()
    {
        return this.boundaries;
    }

    /**
     * @deprecated Should be called only from Arena.leaveArena()
     * @param ewplayer - The eggwars player to remove
     */
    @Deprecated
    public void removePlayer(EwPlayer ewplayer)
    {
        this.players.remove(ewplayer);
    }

    public boolean addPlayer(EwPlayer ewplayer)
    {
        try
        {
            EwPlayerJoinArenaEvent evnt = new EwPlayerJoinArenaEvent(ewplayer, this);
            Bukkit.getPluginManager().callEvent(evnt);

            if (evnt.isCancelled())
            {
                return false;
            }
        }
        catch (LinkageError err)
        {
        }

        ewplayer.setJoining(true);
        this.players.add(ewplayer);
        return true;
    }

    /**
     * Logs the player into the arena.
     * @param player The player that will be teleported
     * @param fromBungee WIP - upcoming improvements
     * @param toSpectate Wheter the player comes to spectate. If true, no "joined" message will be sent.
     */
    public void joinArena(EwPlayer player, boolean fromBungee, boolean toSpectate)
    {
        if (!this.addPlayer(player))
        {
            return;
        }

        this.hidePlayers(player, false);
        player.setArena(this);
        player.storeGameData();
        player.getPlayer().getInventory().clear();
        player.getPlayer().getEnderChest().clear();
        player.getPlayer().setFlying(false);
        player.getPlayer().setAllowFlight(false);
        this.setPlayerMaxHealth(player);
        player.getPlayer().setFoodLevel(20);
        player.getPlayer().setLevel(0);
        player.getPlayer().setExp(0.0f);
        player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        PlayerUtils.removePotionEffects(player.getPlayer());

        if (!toSpectate && this.status.isLobby())
        {
            this.sendBroadcast("gameplay.lobby.player_joined", player.getPlayer().getDisplayName(), Integer.valueOf(this.players.size()), Integer.valueOf(this.getTeams().size() * this.maxTeamPlayers));
        }

        if (toSpectate)
        {
            player.getPlayer().teleport(this.center.clone().add(0.0D, 0.5D, 0.0D));
            player.getPlayer().setGameMode(GameMode.SPECTATOR);
            player.setEliminated(true);
            this.scores.updateScores(false);
            this.scores.setTeamScores(player);
        }
        else
        {
            player.getPlayer().teleport(this.lobby.clone().add(0.0D, 0.5D, 0.0D));
            player.getPlayer().setGameMode(GameMode.SURVIVAL);
            Lobby.onEnter(this, player);
            this.scores.updateScores(true);
        }

        if (this.hasEnoughPlayers() && this.status.equals(ArenaStatus.WAITING))
        {
            Lobby.doStartingPhase(this);
        }

        player.setJoining(false);
    }

    public void leaveArena(EwPlayer player, boolean sendBungee, boolean silent)
    {
        Team team = player.getTeam();

        if (team != null)
        {
            team.removePlayer(player);

            if (this.status.isLobby() && !this.skipsLobby())
            {
                this.teamSelectMenu.updateTeamItem(team);
                this.teamSelectMenu.sendMenuUpdate(false);
            }
        }

        this.removePlayer(player);
        this.scores.clearScoreboard(player.getPlayer());

        if (!this.equals(player.getArena()))
        {
            this.scores.updateScores(true);
            return;
        }

        this.hidePlayers(player, true);
        player.getIngameStats().saveAndReset(this.getMode());
        player.getPlayer().getInventory().clear();
        PlayerUtils.removePotionEffects(player.getPlayer());
        player.restoreGameData();

        if (!silent && !player.isEliminated())
        {
            this.sendBroadcast("gameplay." + (this.status.isLobby() ? "lobby" : "ingame") + ".player_left", player.getPlayer().getDisplayName(), Integer.valueOf(this.players.size()), Integer.valueOf(this.getTeams().size() * this.maxTeamPlayers));
        }

        if (team != null && this.status.isGame() && !player.isEliminated())
        {
            if (!silent)
            {
                player.getArena().sendBroadcast("gameplay.ingame.player_eliminated", player.getPlayer().getCustomName());
            }

            if (team.isEliminated())//player is already removed from team, due to arena.removePlayer, and player may not be eliminated yet
            {
                if (team.canRespawn() && EggWars.config.keepTeamEgg <= 0)
                {
                    team.getEgg().getBlock().setType(Material.AIR);
                }

                if (!silent && this.getMode().isTeam())
                {
                    team.broadcastEliminated();
                }
            }
        }

        this.scores.updateScores(true);

        if (this.status.isLobby())
        {
            this.itemsVotes.remove(player);
            this.healthVotes.remove(player);
            this.votingMenus.updateMenus();
        }

        player.setArena(null);
        PlayerUtils.tpToLobby(player.getPlayer(), sendBungee);
        player.setEliminated(false);

        if (this.status.isGame())
        {
            Team winner = this.getWinner();

            if ((!this.forced && winner != null) || this.getAliveTeams().isEmpty())
            {
                Finish.finish(this, winner);
            }
        }

        player.setJoining(false);
    }

    private void hidePlayers(EwPlayer pl1, boolean leave)
    {
        if (EggWars.config.hidePlayers)
        {
            for (EwPlayer pl2 : EggWars.players)
            {
                if ((!leave && this.equals(pl2.getArena())) || (leave && !pl2.isInArena()))
                {
                    pl1.getPlayer().showPlayer(EggWars.instance, pl2.getPlayer());
                    pl2.getPlayer().showPlayer(EggWars.instance, pl1.getPlayer());
                }
                else
                {
                    pl1.getPlayer().hidePlayer(EggWars.instance, pl2.getPlayer());
                    pl2.getPlayer().hidePlayer(EggWars.instance, pl1.getPlayer());
                }
            }
        }
    }

    public Set<EwPlayer> getAlivePlayers()
    {
        Set<EwPlayer> set = new HashSet();

        for (EwPlayer player : this.getPlayers())
        {
            if (!player.isEliminated())
            {
                set.add(player);
            }
        }

        return set;
    }

    public Set<Team> getAliveTeams()
    {
        Set<Team> set = new HashSet();

        for (Team team : this.getTeams().values())
        {
            if (!team.isEliminated())
            {
                set.add(team);
            }
        }

        return set;
    }

    public void setMinPlayers(int i)
    {
        this.minPlayers = i;
    }

    public int getMinPlayers()
    {
        return this.minPlayers;
    }

    public boolean hasEnoughPlayers()
    {
        return this.minPlayers <= this.getAlivePlayers().size();
    }

    public int getStartCountdown()
    {
        return this.startCountdown;
    }

    public void setStartCountdown(int i)
    {
        this.startCountdown = i;
    }

    public Location getLobby()
    {
        if (this.lobby != null)
        {
            return this.lobby.clone();
        }

        return null;
    }

    public void setLobby(Location loc)
    {
        this.lobby = loc;
    }

    public Location getCenter()
    {
        if (this.center != null)
        {
            return this.center.clone();
        }

        return null;
    }

    public void setCenter(Location loc)
    {
        this.center = loc;
    }

    public int getReleaseCountdown()
    {
        return this.releaseCountdown;
    }

    public void setReleaseCountdown(int i)
    {
        this.releaseCountdown = i;
    }

    public Scoreboards getScores()
    {
        return this.scores;
    }

    public SetupGUI getSetupGUI()
    {
        return this.setupGUI;
    }

    public void updateSetupTeam(TeamType team)
    { 
        this.setupGUI.updateTeamInv(team, true);
    }

    /** Calculates the winner team of the game from the last remaining alive team **/
    public Team getWinner()
    {
        Team team = null;

        for (Team aliveTeam : this.getAliveTeams())
        {
            if (team != null)
            {
                return null;
            }

            team = aliveTeam;
        }

        return team;
    }

    public void reset(boolean enterSetup)
    {
        this.getPlayers().forEach(ewplayer -> this.leaveArena(ewplayer, true, true));

        for (Entity entity : this.world.getEntities())
        {
            if (entity instanceof Player)
            {
                PlayerUtils.tpToLobby((Player)entity, true);
            }
            else
            {
                entity.remove();
            }
        }

        this.forced = false;
        this.saving = false;
        this.itemsVotes.clear();
        this.itemType = ItemType.NORMAL;
        this.healthVotes.clear();
        this.healthType = HealthType.NORMAL;
        this.players.clear();
        this.getTeams().values().forEach(Team::reset);
        this.getGenerators().values().forEach(Generator::reset);
        this.getWorld().setGameRule(GameRule.NATURAL_REGENERATION, Boolean.valueOf(true));

        //If it is entering setup it will regen the world, and if it is exiting it will not.
        //In other words, when the game was not in setup mode, if it is entering setup it will regen the world, if not then the config option must be enabled
        //The status check serves to fix an internal issue when an incomplete arena is loaded, be able to set enterSetup=true to correctly set the world to edit mode without regenerating the world again on server startup
        if (this.status != ArenaStatus.SETTING && (enterSetup || EggWars.instance.getConfig().getBoolean("plugin.regenerate_worlds")))
        {
            this.replacedBlocks.clear();
            this.world.removePluginChunkTickets(EggWars.instance);
            this.setWorld(WorldController.regenArena(this));
        }
        else
        {
            for (BlockState state : this.replacedBlocks.values())
            {
                //Get location and then get block from that location in order to get the proper block
                state.getLocation().getBlock().setType(state.getType());
                state.getLocation().getBlock().setBlockData(state.getBlockData());
            }

            this.replacedBlocks.clear();
            this.world.removePluginChunkTickets(EggWars.instance);
        }

        if (!enterSetup)
        {
            this.getWorld().setGameRule(GameRule.RANDOM_TICK_SPEED, Integer.valueOf(0));
        }

        this.itemType = ItemType.NORMAL;
        this.healthType = HealthType.NORMAL;
        this.setStatus(enterSetup ? ArenaStatus.SETTING : ArenaStatus.WAITING);

        if (this.teamSelectMenu == null)
        {
            this.teamSelectMenu = new TeamsMenu();
        }

        this.teamSelectMenu.reset(this);

        if (this.votingMenus == null)
        {
            this.votingMenus = new VotingMenus(this);
        }

        this.votingMenus.buildInventories();

        for (ArenaSign ewsign : EggWars.signs)
        {
            if (this.equals(ewsign.getArena()))
            {
                ewsign.update();
            }
        }
    }

    public void sendBroadcast(String s, Object... objects)
    {
        for (EwPlayer ewplayer : this.getPlayers())
        {
            TranslationUtils.sendMessage(s, ewplayer.getPlayer(), objects);
        }
    }

    public void broadcastSound(Sound sound, float volume, float pitch)
    {
        for (EwPlayer ewplayer : this.getPlayers())
        {
            ewplayer.getPlayer().playSound(ewplayer.getPlayer().getLocation(), sound, volume, pitch);
        }
    }

    public boolean isSetup()
    {
        if (this.lobby == null)
        {
            return false;
        }

        if (this.minPlayers == 0)
        {
            return false;
        }

        if (this.maxTeamPlayers == 0)
        {
            return false;
        }

        if (this.startCountdown < 0)
        {
            return false;
        }

        if (this.releaseCountdown < 0)
        {
            return false;
        }

        if (this.teams.size() < 2)
        {
            return false;
        }

        if (this.center == null)
        {
            return false;
        }

        for (Team team : this.teams.values())
        {
            if (team.getVillager() == null)
            {
                return false;
            }

            if (team.getCages() == null || team.getCages().size() < this.maxTeamPlayers)
            {
                return false;
            }

            if (team.getRespawn() == null)
            {
                return false;
            }

            if (team.getEgg() == null)
            {
                return false;
            }
        }

        return true;
    }

    public boolean isFull()
    {
        int i = this.getTeams().size() * this.maxTeamPlayers;
        return this.getPlayers().size() >= i;
    }

    public void sendToDo(Player player)
    {
        List<String> todoList = new ArrayList();
        //count of optional ToDos
        int optCount = 0;

        if (this.lobby == null)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_waiting_lobby", player, new Object[] {this.getName()}));
        }

        if (!this.boundaries.areComplete())
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_boundaries", player, new Object[] {this.getName()}));
            optCount++;
        }

        if (this.minPlayers == 0)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_min_players", player, new Object[] {this.getName()}));
        }

        if (this.maxTeamPlayers == 0)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_max_team_players", player, new Object[] {this.getName()}));
        }

        if (this.startCountdown < 0)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_start_countdown", player, new Object[] {this.getName()}));
        }

        /* Will keep fullCountdown here even if it is now fully optional, because
         * the message will mention it, and I prefer this to be still used */
        if (this.fullCountdown < 0)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_full_countdown", player, new Object[] {this.getName()}));
            optCount++;
        }

        if (this.releaseCountdown < 0)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_release_countdown", player, new Object[] {this.getName()}));
        }

        if (this.teams.size() < 2)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.add_teams", player, new Object[] {this.getName()}));
        }

        if (this.center == null)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_center", player, new Object[] {this.getName()}));
        }

        for (Team team : this.teams.values())
        {
            if (team.getVillager() == null)
            {
                todoList.add(TranslationUtils.getMessage("setup.todo.team.set_villager", player, new Object[] {team.getType().id(), this.getName()}));
            }

            if (team.getCages() == null || team.getCages().size() < this.maxTeamPlayers)
            {
                todoList.add(TranslationUtils.getMessage("setup.todo.team.set_cages", player, new Object[] {team.getType().id(), this.getName()}));
            }

            if (team.getRespawn() == null)
            {
                todoList.add(TranslationUtils.getMessage("setup.todo.team.set_respawn", player, new Object[] {team.getType().id(), this.getName()}));
            }

            if (team.getEgg() == null)
            {
                todoList.add(TranslationUtils.getMessage("setup.todo.team.set_egg", player, new Object[] {team.getType().id(), this.getName()}));
            }
        }

        if (!todoList.isEmpty())
        {
            TranslationUtils.sendMessage("setup.todo.list", player);

            for (String s : todoList)
            {
                player.sendMessage(s);
            }
        }

        //if optCount equals the size of the ToDo list means that the arena can now be saved
        //because optional ToDos are not necessary
        if (optCount == todoList.size())
        {
            TranslationUtils.sendMessage("setup.todo.save", player, this.getName());
        }
    }

    public boolean saveArena()
    {
        this.saving = true;
        ConfigAccessor accessor = new ConfigAccessor(EggWars.instance, new File(this.arenaFolder, "arena.yml"));
        accessor.createNewConfig();
        FileConfiguration fconfig = accessor.getConfig();
        fconfig.set("Name", this.getName());
        fconfig.set("MaxPlayersPerTeam", Integer.valueOf(this.maxTeamPlayers));
        fconfig.set("MinPlayers", Integer.valueOf(this.minPlayers));
        fconfig.set("StartCountdown", Integer.valueOf(this.startCountdown));
        fconfig.set("FullCountdown", Integer.valueOf(this.fullCountdown));
        fconfig.set("ReleaseCountdown", Integer.valueOf(this.releaseCountdown));
        fconfig.set("Bounds", Bounds.serialize(this.boundaries));
        fconfig.set("Lobby", Locations.toString(this.lobby, true));
        fconfig.set("Center", Locations.toString(this.center, true));
        fconfig.set("ArenaSpecificTrades", Boolean.valueOf(this.customTrades));

        for (Map.Entry<TeamType, Team> entry : this.teams.entrySet())
        {
            String id = entry.getKey().id();
            Team team = entry.getValue();
            int i = 0;//Cages will be saved with in the order of the list their locations are in

            for (Cage cage : team.getCages())
            {
                fconfig.set("Team." + id + ".Cages." + i, Locations.toString(cage.getLocation(), true));
                i++;
            }

            fconfig.set("Team." + id + ".Villager", Locations.toString(team.getVillager(), true));
            fconfig.set("Team." + id + ".Egg", Locations.toString(team.getEgg(), false));
            fconfig.set("Team." + id + ".Respawn", Locations.toString(team.getRespawn(), true));
        }

        int c = 1;
        fconfig.set("Generator", null);

        for (Generator generator : this.generators.values())
        {
            fconfig.set("Generator." + c + ".Type", generator.getType());
            fconfig.set("Generator." + c + ".Loc", Locations.toString(generator.getBlock(), false));
            fconfig.set("Generator." + c + ".DefLevel", Integer.valueOf(generator.getDefLevel()));
            c++;
        }

        accessor.saveConfig();
        WorldController.saveArenaWorld(this);
        this.saving = false;
        return true;
    }

    public void closeArena()
    {
        for (EwPlayer player : this.getPlayers())
        {
            player.getIngameStats().saveAndReset(this.getMode());
            player.getPlayer().getInventory().clear();
            PlayerUtils.removePotionEffects(player.getPlayer());
            player.restoreGameData();
        }

        if (this.status == ArenaStatus.SETTING)
        {
            this.saveArena();
        }
    }

    public void openTeamInv(EwPlayer player)
    {
        this.teamSelectMenu.addOpener(player);
    }

    public VotingMenus getVotingMenus()
    {
        return this.votingMenus;
    }

    public ItemType getItemType()
    {
        return this.itemType;
    }

    public void setupVotedResults()
    {
        ItemType items = ItemType.NORMAL;

        if (!this.itemsVotes.isEmpty())
        {
            float[] weights = new float[ItemType.values().length];

            for (EwPlayer ewplayer : this.players)
            {
                ItemType itemtype = this.itemsVotes.get(ewplayer);

                if (itemtype != null)
                {
                    weights[itemtype.getNumericalId()] += ewplayer.getVotePower();
                }
            }

            float hw = weights[0];
            float nw = weights[1];
            float ow = weights[2];

            if (hw > nw && hw > ow)
            {
                items = ItemType.HARDCORE;
            }
            else if (nw > hw && nw > ow)
            {
                items = ItemType.NORMAL;
            }
            else if (ow > hw && ow > nw)
            {
                items = ItemType.OVERPOWERED;
            }
        }

        HealthType health = HealthType.NORMAL;

        if (!this.healthVotes.isEmpty())
        {
            float[] weights = new float[HealthType.values().length];

            for (EwPlayer ewplayer : this.players)
            {
                HealthType healthtype = this.healthVotes.get(ewplayer);

                if (healthtype != null)
                {
                    weights[healthtype.getNumericalId()] += ewplayer.getVotePower();
                }
            }

            float hw = weights[0];
            float nw = weights[1];
            float dw = weights[2];
            float tw = weights[3];

            if (hw > nw && hw > dw && hw > tw)
            {
                health = HealthType.HALF;
            }
            else if (nw > hw && nw > dw && nw > tw)
            {
                health = HealthType.NORMAL;
            }
            else if (dw > hw && dw > nw && dw > tw)
            {
                health = HealthType.DOUBLE;
            }
            else if (tw > hw && tw > nw && tw > dw)
            {
                health = HealthType.TRIPLE;
            }
        }

        this.itemType = items;
        this.healthType = health;

        if (this.itemType == ItemType.HARDCORE)
        {
            this.getWorld().setGameRule(GameRule.NATURAL_REGENERATION, Boolean.valueOf(false));
        }

        for (EwPlayer ewplayer : this.getPlayers())
        {
            Player player = ewplayer.getPlayer();
            TranslationUtils.sendMessage("gameplay.voting.items.result", player, TranslationUtils.getMessage(this.itemType.getNameKey(), player));
            TranslationUtils.sendMessage("gameplay.voting.health.result", player, TranslationUtils.getMessage(this.healthType.getNameKey(), player));
        }
    }

    public boolean playerVoteItem(ItemType itemtype, EwPlayer ewplayer)
    {
        if (this.itemsVotes.containsKey(ewplayer) && this.itemsVotes.get(ewplayer) == itemtype)
        {
            return false;
        }
        else if (this.status.isLobby() || this.status == ArenaStatus.STARTING_GAME)
        {
            this.itemsVotes.put(ewplayer, itemtype);
            return true;
        }

        return false;
    }

    public int getVotesForItem(ItemType itemtype)
    {
        int[] aint0 = new int[ItemType.values().length];

        for (EwPlayer ewplayer : this.players)
        {
            ItemType itemtype1 = this.itemsVotes.get(ewplayer);

            if (itemtype1 != null)
            {
                aint0[itemtype1.getNumericalId()] += 1;
            }
        }

        return aint0[itemtype.getNumericalId()];
    }

    public boolean playerVoteHealth(HealthType healthtype, EwPlayer ewplayer)
    {
        if (this.healthVotes.containsKey(ewplayer) && this.healthVotes.get(ewplayer) == healthtype)
        {
            return false;
        }
        else if (this.status.isLobby() || this.status == ArenaStatus.STARTING_GAME)
        {
            this.healthVotes.put(ewplayer, healthtype);
            return true;
        }

        return false;
    }

    public int getVotesForHealth(HealthType healthtype)
    {
        int[] aint0 = new int[HealthType.values().length];

        for (EwPlayer ewplayer : this.players)
        {
            HealthType healthtype1 = this.healthVotes.get(ewplayer);

            if (healthtype1 != null)
            {
                aint0[healthtype1.getNumericalId()] += 1;
            }
        }

        return aint0[healthtype.getNumericalId()];
    }

    public void setPlayerMaxHealth(EwPlayer player)
    {
        double d0 = 20.0;

        if (this.healthType == HealthType.HALF)
        {
            d0 = 10.0;
        }
        else if (this.healthType == HealthType.DOUBLE)
        {
            d0 = 40.0;
        }
        else if (this.healthType == HealthType.TRIPLE)
        {
            d0 = 60.0;
        }

        player.getPlayer().getAttribute(ReflectionUtils.getMaxHealthAttribute()).setBaseValue(d0);
        player.getPlayer().setHealth(d0);
    }

    public int getCurrentCountdown()
    {
        return this.currCountdown;
    }

    public void setCurrentCountdown(int i)
    {
        this.currCountdown = i;
    }

    public boolean forceStart()
    {
        if (!this.status.isLobby() || this.getPlayers().size() < 1)
        {
            return false;
        }

        (new BukkitRunnable()
        {
            public void run()
            {
                //broadcast forcestart must happen after command success
                Arena.this.sendBroadcast("gameplay.lobby.force_started");
                Lobby.endStartingPhase(Arena.this);
                Arena.this.forced = Arena.this.getPlayers().size() == 1 || Arena.this.getAliveTeams().size() == 1;
            }
        }).runTaskLater(EggWars.instance, 3L);
        return true;
    }

    public boolean beenForced()
    {
        return this.forced;
    }

    public boolean isSaving()
    {
        return this.saving;
    }

    public boolean skipsLobby()
    {
        return EggWars.config.skipsLobby.applies(this.getMode());
    }

    public int hashCode()
    {
        int i = 1;
        i = 31 * i + (this.name != null ? this.name.hashCode() : 0);
        return i;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (this.getClass() != obj.getClass())
        {
            return false;
        }

        Arena arena = (Arena)obj;

        if (this.name == null)
        {
            if (arena.name != null)
            {
                return false;
            }
        }
        else if (!this.name.equals(arena.name))
        {
            return false;
        }

        return true;
    }

    public static Arena checkEditArena(Player p)
    {
        Arena arena = EggWars.getArenaManager().getArenaByWorld(p.getWorld());

        if (arena == null)
        {
            TranslationUtils.sendMessage("commands.error.not_in_arena_world", p);
            return null;
        }

        if (!arena.getStatus().equals(ArenaStatus.SETTING))
        {
            TranslationUtils.sendMessage("commands.error.arena_needs_edit_mode", p);
            return null;
        }

        return arena;
    }
}
