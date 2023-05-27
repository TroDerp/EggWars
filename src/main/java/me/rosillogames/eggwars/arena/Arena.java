package me.rosillogames.eggwars.arena;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
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
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
import me.rosillogames.eggwars.events.EwPlayerJoinArenaEvent;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.loaders.TradingLoader;
import me.rosillogames.eggwars.objects.ArenaSign;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.player.EwPlayerMenu.MenuSize;
import me.rosillogames.eggwars.player.inventory.EwInvType;
import me.rosillogames.eggwars.player.inventory.InventoryController;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ConfigAccessor;
import me.rosillogames.eggwars.utils.GsonHelper;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamTypes;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.VoteUtils;
import me.rosillogames.eggwars.utils.WorldController;

public class Arena
{
    //Universal
    public final File arenaFolder;
    private final String name;
    private final Map<TeamTypes, Team> teams = Maps.newEnumMap(TeamTypes.class);
    //Use vector instead of location to skip an issue with worlds when used loc.equals(other)
    private final Map<Vector, Generator> generators = Maps.newHashMap();
    //Arena status will now always be "Setup" at first, before completing init or when arena is newly created
    private ArenaStatus status = ArenaStatus.SETTING;

    //For game
    private World world;
    private final Set<EwPlayer> players = Sets.newHashSet();
    private final Set<Location> placedBlocks = Sets.newHashSet();
    private final Set<BlockState> brokenBlocks = Sets.newHashSet();
    private ItemType itemType = ItemType.NORMAL;
    private HealthType healthType = HealthType.NORMAL;
    private final Map<EwPlayer, ItemType> itemsVotes = new HashMap();
    private final Map<EwPlayer, HealthType> healthVotes = new HashMap();
    private TranslatableInventory teamInv;
    private TranslatableInventory voteInv;
    private TranslatableInventory itemVoteInv;
    private TranslatableInventory healthVoteInv;
    private final List<TranslatableInventory> shopInvs = new ArrayList();
    private final List<Map<Integer, Category>> shopCategsSlots = new ArrayList();
    private boolean forced = false;
    private int currCountdown;

    //Constant stored settings
    private boolean customTrades = false;
    private Location lobby;
    private Location center;
    private Bounds boundaries;
    private int maxTeamPlayers;
    /** Min players required to start game */
    private int minPlayers;
    /** Default count down (in seconds), when waiting on the arena's lobby and it's not full **/
    private int defCountdown;
    /** Full count down (in seconds), when the arena is full the counter changes to this.
     ** It is optional in the configuration files. If not set, the counter will not change. **/
    private int fullCountdown;
    /** Count down for the game (in seconds). It is used when waiting on cages **/
    private int gameCountdown;

    public Arena(String name)
    {
        this.name = name;
        this.arenaFolder = new File(EggWars.arenasFolder, name);

        if (!this.arenaFolder.exists())
        {
            this.arenaFolder.mkdirs();
        }
        else if (!this.arenaFolder.isDirectory())
        {
            this.arenaFolder.delete();
            this.arenaFolder.mkdirs();
        }

        this.world = WorldController.createArenaInitWorld(name);
        this.lobby = null;
        this.center = null;
        this.boundaries = new Bounds(null, null);
        this.maxTeamPlayers = 0;
        this.minPlayers = 0;
        this.defCountdown = -1;
        this.fullCountdown = -1;
        this.gameCountdown = -1;
    }

    /** Only created when an arena is loaded or cloned.
     ** @param fldrIn is the arena folder
     ** @param newName is the override name for the arena, it is used when cloning
     **/
    public Arena(File fldrIn, @Nullable String newName)
    {
        this.arenaFolder = fldrIn;
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
        this.defCountdown = fileconf.getInt("Countdown");
        this.fullCountdown = fileconf.getInt("FullCountdown", -1);
        this.gameCountdown = fileconf.getInt("GameCountdown");
        this.customTrades = fileconf.getBoolean("ArenaSpecificTrades", false);

        if (this.customTrades)
        {
            EggWars.instance.saveCustomResource("custom/" + TradingLoader.SPEC_TRADES_FILE, new File(this.arenaFolder, TradingLoader.SPEC_TRADES_FILE), false);
        }

        for (TeamTypes teamtype : TeamTypes.values())
        {
            String teamtypeid = "Team." + teamtype.id();

            if (!fileconf.contains(teamtypeid))
            {
                continue;
            }

            Team team = new Team(this, teamtype);
            String cagekey = ".Glasses.";//LEGACY KEY

            if (fileconf.getConfigurationSection(teamtypeid + cagekey) == null)//if legacy not present then use new key
            {
                cagekey = ".Cages.";
            }

            ConfigurationSection cages;

            if ((cages = fileconf.getConfigurationSection(teamtypeid + cagekey)) != null)
            {
                for (String key : cages.getKeys(false))
                {
                    team.addCage(Locations.fromString(fileconf.getString(teamtypeid + cagekey + key)));
                }
            }

            loadIfPresent(fileconf, teamtypeid + ".Respawn", team::setRespawn);
            loadIfPresent(fileconf, teamtypeid + ".Villager", team::setVillager);
            loadIfPresent(fileconf, teamtypeid + ".Egg", team::setEgg);
            this.teams.put(teamtype, team);
        }

        ConfigurationSection gens;

        if ((gens = fileconf.getConfigurationSection("Generator")) != null)
        {
            for (String gen : gens.getKeys(false))
            {
                Generator generator = new Generator(Locations.fromString(fileconf.getString("Generator." + gen + ".Loc")), fileconf.getInt("Generator." + gen + ".DefLevel"), fileconf.getString("Generator." + gen + ".Type"), this);
                this.generators.put(generator.getBlock().toVector(), generator);
            }
        }

        this.setWorld(world);
        this.reset(!this.isSetup());
    }

    private static void loadIfPresent(FileConfiguration config, String key, Consumer<Location> cons)
    {
        if (config.contains(key))
        {
            cons.accept(Locations.fromString(config.getString(key)));
        }
    }

    public Mode getMode()
    {
        return this.maxTeamPlayers > 1 ? Mode.TEAM : Mode.SOLO;
    }

    public void loadShop()
    {
        this.shopCategsSlots.clear();
        this.shopInvs.clear();
        List<Category> trades = EggWars.getTradingManager().getMerchants();
        Category specific = null;

        if (this.customTrades)
        {
            try
            {
                EggWars.instance.getLogger().log(Level.INFO, "Loading data for arena \"" + this.getName() + "\" specific trades...");
                BufferedReader buffer = Files.newBufferedReader((new File(this.arenaFolder, TradingLoader.SPEC_TRADES_FILE)).toPath());
                specific = TradingLoader.loadCategory(GsonHelper.convertToJsonObject(GsonHelper.parse(buffer), "trades"));
                buffer.close();
            }
            catch (Exception ex)
            {
            	EggWars.instance.getLogger().log(Level.WARNING, "Error loading specific trades for arena \"" + this.getName() + "\" from type \": ", ex);
            }
        }

        List<MenuSize> sizes = MenuSize.fromChestSize(trades.size());
        int counter = 0;
        int expected = 0;

        for (int i = 0; i < sizes.size(); ++i)
        {
            MenuSize size = (MenuSize)sizes.get(i);
            TranslatableInventory translatableinv = new TranslatableInventory(size.getSlots(), "shop.title");
            Map<Integer, Category> pageTrades = new HashMap();
            expected += size.getFilledSlots();

            for (int j = 0; j < size.getFilledSlots() && counter < trades.size() && counter <= expected; ++counter)
            {
                int slot = 9 * (j / 7 + 1) + j % 7 + 1;
                Category category = trades.get(counter);
                translatableinv.setItem(slot, createCategoryItem(category));
                pageTrades.put(slot, category);
                ++j;
            }

            if (i < sizes.size() - 1)
            {
                translatableinv.setItem(8, EwPlayerMenu.getNextItem());
            }

            if (i > 0)
            {
                translatableinv.setItem(0, EwPlayerMenu.getPreviousItem());
            }

            if (specific != null && !specific.isEmpty())
            {
                int slot = 4;
                translatableinv.setItem(slot, createCategoryItem(specific));
                pageTrades.put(slot, specific);
            }

            translatableinv.setItem(size.getSlots() - 5, EwPlayerMenu.getCloseItem());
            translatableinv.setItem(size.getSlots() - 1, EwPlayerMenu.getClassicShopItem());
            this.shopInvs.add(i, translatableinv);
            this.shopCategsSlots.add(i, pageTrades);
        }
    }

    private static TranslatableItem createCategoryItem(Category category)
    {
        return TranslatableItem.translatableNameLore(ItemUtils.hideStackAttributes(category.getDisplayItem().clone()), (player) ->
        {
            return TranslationUtils.getMessage(category.getTranslation() + ".desc", player);
        }, (player) ->
        {
            return TranslationUtils.getMessage(category.getTranslation() + ".name", player);
        });
    }

    public Set getPlacedBlocks()
    {
        return new HashSet(this.placedBlocks);
    }

    public void addPlacedBlock(Location location)
    {
    	this.placedBlocks.add(location);
    }

    public void addBrokenBlock(BlockState state)
    {
        this.brokenBlocks.add(state);
    }

    public void removePlacedBlock(Block block)
    {
        if (!this.placedBlocks.contains(block.getLocation()))
        {
            this.brokenBlocks.add(block.getState());
        }
        else
        {
            this.placedBlocks.remove(block.getLocation());
        }
    }

    public Map<TeamTypes, Team> getTeams()
    {
        return new HashMap(this.teams);
    }

    public void removeTeam(TeamTypes teamTypes)
    {
        this.teams.remove(teamTypes);
    }

    public void addTeam(TeamTypes teamTypes)
    {
        this.teams.put(teamTypes, new Team(this, teamTypes));
    }

    public boolean moveTeam(TeamTypes oldTeam, TeamTypes newTeam)
    {
        Team team = this.teams.remove(oldTeam);
        boolean flag = false;

        if (this.teams.containsKey(newTeam))
        {
            Team team1 = this.teams.remove(newTeam);
            team1.setType(oldTeam);
            this.teams.put(oldTeam, team1);
            flag = true;
        }

        team.setType(newTeam);
        this.teams.put(newTeam, team);
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

        this.getTeams().values().forEach(team -> team.setArenaWorld());
        this.getGenerators().values().forEach(gen -> gen.setArenaWorld());
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

    public void setBounds(Bounds bounds)
    {
        this.boundaries = bounds;
    }

    public void removePlayer(EwPlayer ewplayer)
    {
        for (Team team : this.teams.values())
        {
            team.getPlayers().remove(ewplayer);
        }

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
        catch (LinkageError err) { }

        ewplayer.setJoining(true);
        this.players.add(ewplayer);
        return true;
    }

    public void joinArena(EwPlayer player, boolean silently, boolean toSpectate)
    {
        if (!this.addPlayer(player))
        {
            return;
        }

        this.hidePlayers(player, false);
        player.setArena(this);
        player.storeGameData();
        player.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getPlayer().getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getPlayer().getInventory().setBoots(new ItemStack(Material.AIR));
        player.getPlayer().getInventory().clear();
        player.getPlayer().setFlying(false);
        player.getPlayer().setAllowFlight(false);
        player.getPlayer().setHealth(20.0);
        player.getPlayer().setFoodLevel(20);
        player.getPlayer().setLevel(0);
        player.getPlayer().setExp(0.0f);
        player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        player.clearInvincible();
        player.clearKitCooldown();
        PlayerUtils.removePotionEffects(player.getPlayer());

        if (!silently && this.status.isLobby())
        {
        	this.sendBroadcast("gameplay.lobby.player_joined", player.getPlayer().getDisplayName(), Integer.valueOf(this.players.size()), Integer.valueOf(this.getTeams().size() * this.maxTeamPlayers));
        }

        if (toSpectate)
        {
            player.getPlayer().teleport(this.center.clone().add(0.0D, 0.5D, 0.0D));
            player.getPlayer().setGameMode(GameMode.SPECTATOR);
            player.setEliminated(true);
        }
        else
        {
            player.getPlayer().teleport(this.lobby.clone().add(0.0D, 0.5D, 0.0D));
            player.getPlayer().setGameMode(GameMode.SURVIVAL);
            Lobby.onEnter(this, player);
        }

        Scoreboards.setScore(this);

        if (this.hasEnoughPlayers() && this.status.equals(ArenaStatus.LOBBY))
        {
            Lobby.doStartingPhase(this);
        }

        player.setJoining(false);
    }

    public void leaveArena(EwPlayer player, boolean sendBungee, boolean silent)
    {
        this.players.remove(player);
        Scoreboards.clearScoreboard(player.getPlayer());

        if (!this.equals(player.getArena()))
        {
            Scoreboards.setScore(this);
            return;
        }

        this.hidePlayers(player, true);
        player.getIngameStats().saveAndReset(this.getMode());
        player.getPlayer().getInventory().clear();
        PlayerUtils.removePotionEffects(player.getPlayer());
        player.restoreGameData();
        player.clearInvincible();
        player.clearKitCooldown();

        if (!silent && !player.isEliminated())
        {
            this.sendBroadcast("gameplay." + (this.status.isLobby() ? "lobby" : "ingame") + ".player_left", player.getPlayer().getDisplayName(), Integer.valueOf(this.players.size()), Integer.valueOf(this.getTeams().size() * this.maxTeamPlayers));
        }

        if (player.getTeam() != null)
        {
            if (!player.isEliminated() && !silent && this.status.isGame())
            {
                player.getArena().sendBroadcast("gameplay.ingame.player_eliminated", player.getPlayer().getCustomName());
            }

            Team team = player.getTeam();
            team.removePlayer(player);

            if (this.status.isGame() && team.isEliminated())
            {
                if (team.canRespawn())
                {
                    team.getEgg().getBlock().setType(Material.AIR);
                }

                if (!silent)
                {
                    for (EwPlayer ewplayer1 : this.getPlayers())
                    {
                        ewplayer1.getPlayer().sendMessage("");
                        TranslationUtils.sendMessage("gameplay.ingame.team_eliminated", ewplayer1.getPlayer(), TeamUtils.translateTeamType(team.getType(), ewplayer1.getPlayer(), false));
                        ewplayer1.getPlayer().sendMessage("");
                        ewplayer1.getPlayer().playSound(ewplayer1.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 0.0F);
                    }
                }
            }
        }

        Scoreboards.setScore(this);

        if (this.status.isLobby())
        {
            this.itemsVotes.remove(player);
            this.healthVotes.remove(player);
            this.updateInvs();
        }

        player.setArena(null);
        PlayerUtils.tpToLobby(player, sendBungee);
        player.setEliminated(false);

        if (this.status.isGame())
        {
            Team team = this.getWinner();

            if ((!this.forced && team != null) || this.getAliveTeams().isEmpty())
            {
                Finish.finish(this, team);
            }
        }
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

    public int getDefCountdown()
    {
        return this.defCountdown;
    }

    public void setDefCountdown(int i)
    {
        this.defCountdown = i;
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

    public int getGameCountdown()
    {
        return this.gameCountdown;
    }

    public void setGameCountdown(int i)
    {
        this.gameCountdown = i;
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
                PlayerUtils.tpToLobby(PlayerUtils.getEwPlayer((Player)entity), true);
            }
            else
            {
                entity.remove();
            }
        }

        this.forced = false;
    	this.itemsVotes.clear();
        this.healthVotes.clear();
        this.players.clear();
        this.getTeams().values().forEach(team -> team.reset());
        this.getGenerators().values().forEach(generator -> generator.reset());
    	this.getWorld().setGameRule(GameRule.NATURAL_REGENERATION, Boolean.valueOf(true));

    	//If it is entering setup it will regen the world, and if it is exiting it will not.
    	//In other words, when the game was not in setup mode, if it is entering setup it will regen the world, if not then the config option must be enabled
    	//The status check serves to fix an internal issue when an incomplete arena is loaded, be able to set enterSetup=true to correctly set the world to edit mode without regenerating the world again on server startup
    	if (this.status != ArenaStatus.SETTING && (enterSetup || EggWars.instance.getConfig().getBoolean("plugin.regenerate_worlds")))
    	{
            this.placedBlocks.clear();
            this.brokenBlocks.clear();
            this.world.removePluginChunkTickets(EggWars.instance);
            this.setWorld(WorldController.regenArena(this));
    	}
    	else
    	{
            for (Location loc : this.placedBlocks)
    	    {
    	        loc.getBlock().setType(Material.AIR);
    	    }

    	    for (BlockState state : this.brokenBlocks)
            {
    	        //Get block location and then get block from that location in order to get the proper block
                state.getBlock().getLocation().getBlock().setType(state.getType());
                state.getBlock().getLocation().getBlock().setBlockData(state.getBlockData());
            }

    	    this.brokenBlocks.clear();
    	    this.placedBlocks.clear();
    	    this.world.removePluginChunkTickets(EggWars.instance);
    	}

    	if (!enterSetup)
    	{
            this.getWorld().setGameRule(GameRule.RANDOM_TICK_SPEED, Integer.valueOf(0));
    	}

    	this.itemType = ItemType.NORMAL;
        this.healthType = HealthType.NORMAL;
        this.status = enterSetup ? ArenaStatus.SETTING : ArenaStatus.LOBBY;
        this.updateInvs();

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

        if (this.defCountdown < 0)
        {
            return false;
        }

        if (this.gameCountdown < 0)
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
            todoList.add(TranslationUtils.getMessage("setup.todo.set_arena_lobby", player, new Object[] {this.getName()}));
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

        if (this.defCountdown < 0)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_countdown", player, new Object[] {this.getName()}));
        }

        /* Will keep fullCountdown here even if it is now fully optional, because
         * the message will mention it, and I prefer this to be still used */
        if (this.fullCountdown < 0)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_full_countdown", player, new Object[] {this.getName()}));
            optCount++;
        }

        if (this.gameCountdown < 0)
        {
            todoList.add(TranslationUtils.getMessage("setup.todo.set_game_countdown", player, new Object[] {this.getName()}));
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
        ConfigAccessor accessor = new ConfigAccessor(EggWars.instance, new File(this.arenaFolder, "arena.yml"));
        FileConfiguration fconfig = accessor.getConfig();
        fconfig.set("Name", this.getName());
        fconfig.set("MaxPlayersPerTeam", Integer.valueOf(this.maxTeamPlayers));
        fconfig.set("MinPlayers", Integer.valueOf(this.minPlayers));
        fconfig.set("Countdown", Integer.valueOf(this.defCountdown));
        fconfig.set("FullCountdown", Integer.valueOf(this.fullCountdown));
        fconfig.set("Bounds", Bounds.serialize(this.boundaries));
        fconfig.set("Lobby", Locations.toString(this.lobby, true));
        fconfig.set("Center", Locations.toString(this.center, true));
        fconfig.set("GameCountdown", Integer.valueOf(this.gameCountdown));
        fconfig.set("ArenaSpecificTrades", Boolean.valueOf(this.customTrades));

        for (Map.Entry<TeamTypes, Team> entry : this.teams.entrySet())
        {
            String id = entry.getKey().id();
            Team team = entry.getValue();
            int i = 0;//Cages will be saved with in the order of the list their locations are in

            for (Location cage : team.getCages())
            {
                fconfig.set("Team." + id + ".Cages." + i, Locations.toString(cage, true));
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

    public void updateInvs()
    {
        if (this.teamInv == null)
        {
            this.teamInv = new TranslatableInventory((int)Math.ceil((double)(this.teams.size() + 1) / 9D) * 9, "teams.menu_title");
        }

        int i = 0;

        for (TeamTypes teamtypes : TeamTypes.values())
        {
            Team team = (Team)this.teams.get(teamtypes);

            if (team == null)
            {
                continue;
            }

            int j = team.getPlayers().size();
            TranslatableItem transitem = new TranslatableItem((player) ->
            {
                EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);
                ItemStack stack = ItemUtils.tryColorizeByTeam(team.getType(), new ItemStack(Material.WHITE_WOOL, (j <= 0 ? 1 : j)));

                if (team.equals(ewplayer.getTeam()))
                {
                    ItemMeta meta = stack.getItemMeta();
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    stack.setItemMeta(meta);
                    stack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);
                }

                return stack;
            });
            transitem.setName((player) -> TranslationUtils.getMessage("teams.team.item_name", player, TeamUtils.translateTeamType(team.getType(), player, false), j, this.maxTeamPlayers));

            for (EwPlayer ewplayer : team.getPlayers())
            {
                transitem.addLoreTranslatable((player) -> TranslationUtils.getMessage("teams.team.item_lore_entry", player, ewplayer.getPlayer().getDisplayName()));
            }

            this.teamInv.setItem(i, transitem);
            i++;
        }

        this.teamInv.setItem(this.teamInv.getSize() - 1, TranslatableItem.translatableName(new ItemStack(Material.NETHER_STAR), "teams.random.item_name"));
        InventoryController.updateInventories((predicateplayer) -> predicateplayer.getArena() == this, this.teamInv, EwInvType.TEAM_SELECTION);
 
        if (this.voteInv == null)
        {
            this.voteInv = new TranslatableInventory(27, "voting.menu_title");
            this.voteInv.setItem(22, EwPlayerMenu.getCloseItem());
            this.voteInv.setItem(11, VoteUtils.itemVoteItem);
            this.voteInv.setItem(15, VoteUtils.healthVoteItem);
        }

        if (this.itemVoteInv == null)
        {
            this.itemVoteInv = new TranslatableInventory(27, "voting.items.menu_title");
            this.itemVoteInv.setItem(22, EwPlayerMenu.getCloseItem());
        }

        this.itemVoteInv.setItem(10, VoteUtils.getTradesVoteItem(ItemType.HARDCORE, this));
        this.itemVoteInv.setItem(13, VoteUtils.getTradesVoteItem(ItemType.NORMAL, this));
        this.itemVoteInv.setItem(16, VoteUtils.getTradesVoteItem(ItemType.OVERPOWERED, this));
        InventoryController.updateInventories((predicateplayer) -> predicateplayer.getArena() == this, this.itemVoteInv, EwInvType.ITEM_VOTING);

        if (this.healthVoteInv == null)
        {
            this.healthVoteInv = new TranslatableInventory(27, "voting.health.menu_title");
            this.healthVoteInv.setItem(22, EwPlayerMenu.getCloseItem());
        }

        this.healthVoteInv.setItem(10, VoteUtils.getHealthVoteItem(HealthType.HALF, this));
        this.healthVoteInv.setItem(12, VoteUtils.getHealthVoteItem(HealthType.NORMAL, this));
        this.healthVoteInv.setItem(14, VoteUtils.getHealthVoteItem(HealthType.DOUBLE, this));
        this.healthVoteInv.setItem(16, VoteUtils.getHealthVoteItem(HealthType.TRIPLE, this));
        InventoryController.updateInventories((predicateplayer) -> predicateplayer.getArena() == this, this.healthVoteInv, EwInvType.HEALTH_VOTING);
    }

    public void openTeamInv(Player player)
    {
        InventoryController.openInventory(player, this.teamInv, EwInvType.TEAM_SELECTION);
    }

    public void openVoteInv(Player player)
    {
        InventoryController.openInventory(player, this.voteInv, EwInvType.VOTING);
    }

    public void openItemVoteInv(Player player)
    {
        InventoryController.openInventory(player, this.itemVoteInv, EwInvType.ITEM_VOTING);
    }

    public void openHealthVoteInv(Player player)
    {
        InventoryController.openInventory(player, this.healthVoteInv, EwInvType.HEALTH_VOTING);
    }

    public void openVillagerInv(Player player, int page)
    {
        InventoryController.openInventory(player, this.shopInvs.get(page), EwInvType.VILLAGER_MENU).setExtraData(page);
    }

    @Nullable
    public Category getShopSlots(int page, int slot)
    {
        return this.shopCategsSlots.get(page).get(slot);
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
            player.sendMessage("");
            TranslationUtils.sendMessage("gameplay.voting.items.result", player, TranslationUtils.getMessage(this.itemType.getNameKey(), player));
            TranslationUtils.sendMessage("gameplay.voting.health.result", player, TranslationUtils.getMessage(this.healthType.getNameKey(), player));
            player.sendMessage("");
        }
    }

    public boolean playerVoteItem(ItemType itemtype, EwPlayer ewplayer)
    {
    	if (this.itemsVotes.containsKey(ewplayer) && this.itemsVotes.get(ewplayer) == itemtype)
    	{
    		return false;
    	}
    	else if (this.status == ArenaStatus.LOBBY || this.status == ArenaStatus.STARTING || this.status == ArenaStatus.STARTING_GAME)
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
        else if (this.status == ArenaStatus.LOBBY || this.status == ArenaStatus.STARTING || this.status == ArenaStatus.STARTING_GAME)
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

        player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(d0);
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

    public boolean skipSoloLobby()
    {
        return this.getMode() == Mode.SOLO && EggWars.config.skipSoloLobby;
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
