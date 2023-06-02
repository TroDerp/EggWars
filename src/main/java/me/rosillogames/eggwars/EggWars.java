package me.rosillogames.eggwars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.SetupGUI;
import me.rosillogames.eggwars.commands.CmdEw;
import me.rosillogames.eggwars.commands.CmdLeave;
import me.rosillogames.eggwars.commands.CmdSetup;
import me.rosillogames.eggwars.database.Database;
import me.rosillogames.eggwars.dependencies.DependencyUtils;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.language.LanguageManager;
import me.rosillogames.eggwars.listeners.BlockBreakListener;
import me.rosillogames.eggwars.listeners.BlockPlaceListener;
import me.rosillogames.eggwars.listeners.ClickInventoryListener;
import me.rosillogames.eggwars.listeners.EggInteractListener;
import me.rosillogames.eggwars.listeners.EggWarsListener;
import me.rosillogames.eggwars.listeners.EntityExplodeListener;
import me.rosillogames.eggwars.listeners.EntityHurtListener;
import me.rosillogames.eggwars.listeners.EntitySpawnListener;
import me.rosillogames.eggwars.listeners.InventoryListener;
import me.rosillogames.eggwars.listeners.ItemPickupListener;
import me.rosillogames.eggwars.listeners.PlayerChatListener;
import me.rosillogames.eggwars.listeners.PlayerCraftListener;
import me.rosillogames.eggwars.listeners.PlayerDeathListener;
import me.rosillogames.eggwars.listeners.PlayerDropListener;
import me.rosillogames.eggwars.listeners.PlayerInteractListener;
import me.rosillogames.eggwars.listeners.PlayerJoinListener;
import me.rosillogames.eggwars.listeners.PlayerLeaveListener;
import me.rosillogames.eggwars.listeners.PlayerMoveListener;
import me.rosillogames.eggwars.listeners.ServerListPingListener;
import me.rosillogames.eggwars.listeners.SignChangeListener;
import me.rosillogames.eggwars.listeners.SignClickListener;
import me.rosillogames.eggwars.listeners.TickClock;
import me.rosillogames.eggwars.loaders.ArenaLoader;
import me.rosillogames.eggwars.loaders.GeneratorLoader;
import me.rosillogames.eggwars.loaders.KitLoader;
import me.rosillogames.eggwars.loaders.TokenLoader;
import me.rosillogames.eggwars.loaders.TradingLoader;
import me.rosillogames.eggwars.objects.ArenaSign;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.Colorizer;
import me.rosillogames.eggwars.utils.ConfigAccessor;
import me.rosillogames.eggwars.utils.GsonHelper;
import me.rosillogames.eggwars.utils.LobbySigns;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class EggWars extends JavaPlugin
{
    public static String EGGWARS_VERSION;
    public static EggWars instance;
    public static BungeeCord bungee = new BungeeCord();
    public static File arenasFolder;
    public static ConfigAccessor signsConfig;
    public static Set<EwPlayer> players;
    public static Set<ArenaSign> signs;
    public static Config config = new Config();
    public static Versions serverVersion;
    public static NamespacedKey genType;
    public static NamespacedKey genLevel;
    private ArenaLoader arenaLoader;
    private KitLoader kitLoader;
    private TokenLoader tokenLoader;
    private GeneratorLoader generatorLoader;
    private TradingLoader tradingLoader;
    private LanguageManager languageManager;
    private Database database;
    private Gson gson;

    public EggWars()
    {
    }

    @Override
    public void onDisable()
    {
        if (!serverVersion.isAllowedVersion())
        {
            return;
        }

        for (Arena arena : this.arenaLoader.getArenas())
        {
            arena.closeArena();
        }

        if (this.database != null)
        {
            this.database.savePlayers();
            this.database.close();
        }
    }

    @Override
    public void onEnable()
    {
        instance = this;
        EGGWARS_VERSION = this.getDescription().getVersion();
        String s = this.getServer().getClass().getPackage().getName();
        serverVersion = Versions.get(s.substring(s.lastIndexOf('.') + 1));

        if (!serverVersion.isAllowedVersion())
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[EggWars] " + ChatColor.RESET + "Uncompatible version! Currently supported: " + Versions.SUPPORTED_TEXT);
            Bukkit.shutdown();
            return;
        }

        ReflectionUtils.setReflections(serverVersion);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.languageManager = new LanguageManager();
        this.gson = new Gson();
        this.loadLists();
        config.loadConfig();

        if (config.checkUpdates)
        {
            (new BukkitRunnable()
            {
                public void run()
                {
                    EggWars.checkCurrentVersion();
                }
            }).runTaskLaterAsynchronously(this, 30L);
        }

        this.languageManager.loadLangs();
        this.loadFiles();
        this.eventRegister();
        this.commandRegister();
        DependencyUtils.registerEggWarsPlaceHolders();
        this.loadLoaders();
        this.loadNamespaces();
        this.loadArenas();
        this.loadSigns();
        TickClock.start();
        this.kitLoader.loadKits();
        this.tokenLoader.loadTokens();
        this.generatorLoader.loadGenerators();
        this.tradingLoader.loadTrades();

        for (Player player : Bukkit.getOnlinePlayers())
        {
            Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, null));
        }
    }

    private void loadLoaders()
    {
        this.kitLoader = new KitLoader();
        this.arenaLoader = new ArenaLoader();
        this.tokenLoader = new TokenLoader();
        this.generatorLoader = new GeneratorLoader();
        this.tradingLoader = new TradingLoader();
        this.database = new Database(this);
    }

    private void loadNamespaces()
    {
        genType = new NamespacedKey(this, "GEN_TYPE");
        genLevel = new NamespacedKey(this, "GEN_LEVEL");
    }

    private void loadLists()
    {
        Colorizer.init();
        players = new HashSet();
        signs = new HashSet();
    }

    private void eventRegister()
    {
        PluginManager pluginmanager = getServer().getPluginManager();
        pluginmanager.registerEvents(new BlockBreakListener(), this);
        pluginmanager.registerEvents(new BlockPlaceListener(), this);
        pluginmanager.registerEvents(new EggWarsListener(), this);
        pluginmanager.registerEvents(new ClickInventoryListener(), this);
        pluginmanager.registerEvents(new EggInteractListener(), this);
        pluginmanager.registerEvents(new EntityExplodeListener(), this);
        pluginmanager.registerEvents(new EntityHurtListener(), this);
        pluginmanager.registerEvents(new EntitySpawnListener(), this);
        pluginmanager.registerEvents(new InventoryListener(), this);
        pluginmanager.registerEvents(new ItemPickupListener(), this);
        pluginmanager.registerEvents(new PlayerChatListener(), this);
        pluginmanager.registerEvents(new PlayerCraftListener(), this);
        pluginmanager.registerEvents(new PlayerDeathListener(), this);
        pluginmanager.registerEvents(new PlayerDropListener(), this);
        pluginmanager.registerEvents(new PlayerInteractListener(), this);
        pluginmanager.registerEvents(new PlayerJoinListener(), this);
        pluginmanager.registerEvents(new PlayerLeaveListener(), this);
        pluginmanager.registerEvents(new PlayerMoveListener(), this);
        pluginmanager.registerEvents(new ServerListPingListener(), this);
        pluginmanager.registerEvents(new SetupGUI.Listener(), this);
        pluginmanager.registerEvents(new SignChangeListener(), this);
        pluginmanager.registerEvents(new SignClickListener(), this);
    }

    private void commandRegister()
    {
        this.getCommand("ews").setExecutor(new CmdSetup());
        this.getCommand("ew").setExecutor(new CmdEw());
        this.getCommand("leave").setExecutor(new CmdLeave());
    }

    private void loadFiles()
    {
        arenasFolder = new File(this.getDataFolder(), "arenas");

        if (!arenasFolder.exists())
        {
            arenasFolder.mkdirs();
        }

        if (!arenasFolder.isDirectory())
        {
            arenasFolder.delete();
            arenasFolder.mkdirs();
        }
    }

    private void loadArenas()
    {
        if (!bungee.isEnabled())
        {
            File afile[] = arenasFolder.listFiles();
            int i = afile.length;

            for (int k = 0; k < i; k++)
            {
                File file = afile[k];

                if (!file.exists() || !file.isDirectory())
                {
                    continue;
                }

                if (!file.exists())
                {
                    file.mkdirs();
                }

                if (!file.isDirectory())
                {
                    file.delete();
                    file.mkdirs();
                }

                try
                {
                    this.arenaLoader.addArena(ArenaLoader.loadArena(file));
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        else
        {
            if (bungee.useRandomArena())
            {
                this.loadRandomArena();
                return;
            }

            bungee.setArena(null);
            File afile1[] = arenasFolder.listFiles();
            int j = afile1.length;
            int l = 0;

            do
            {
                if (l >= j)
                {
                    break;
                }

                File file1 = afile1[l];

                if (!file1.exists() || !file1.isDirectory())
                {
                    continue;
                }

                if (file1.getName().equals("Bungee"))
                {
                    Arena arena = ArenaLoader.loadArena(file1);
                    this.arenaLoader.addArena(arena);
                    bungee.setArena(arena);
                    break;
                }

                l++;
            }
            while (true);

            if (bungee.getArena() == null)
            {
                Arena arena1 = new Arena("Bungee");
                this.arenaLoader.addArena(arena1);
                arena1.setStatus(ArenaStatus.SETTING);
                arena1.getWorld().getBlockAt(0, 99, 0).setType(Material.STONE);
                bungee.setArena(arena1);
            }
        }
    }

    public void loadRandomArena()
    {
        List<File> list = Arrays.asList(arenasFolder.listFiles());
        List<File> list1 = new ArrayList();

        for (File file : list)
        {
            if (file.exists() && file.isDirectory())
            {
                list1.add(file);
            }
        }

        Collections.shuffle(list1);
        Arena arena = ArenaLoader.loadArena((File)list1.get(0));
        this.arenaLoader.addArena(arena);
        bungee.setArena(arena);
    }

    private void loadSigns()
    {
        if (bungee.isEnabled())
        {
            return;
        }

        signsConfig = new ConfigAccessor(this, new File(this.getDataFolder(), "signs.yml"));
        FileConfiguration fileconfiguration = signsConfig.getConfig();
        fileconfiguration.addDefault("Signs", new ArrayList());
        fileconfiguration.options().copyDefaults(true);
        signsConfig.saveConfig();

        for (String s : fileconfiguration.getStringList("Signs"))
        {
            Arena arena;
            Location location;

            try
            {
                JsonObject entryjson = GsonHelper.parse(s);
                JsonObject locjson = GsonHelper.getAsJsonObject(entryjson, "location");
                World world = Bukkit.getWorld(GsonHelper.getAsString(locjson, "world_name"));
                double d0 = (double)GsonHelper.getAsFloat(locjson, "x");
                double d1 = (double)GsonHelper.getAsFloat(locjson, "y");
                double d2 = (double)GsonHelper.getAsFloat(locjson, "z");
                location = new Location(world, d0, d1, d2);
                arena = this.arenaLoader.getArenaByName(GsonHelper.getAsString(entryjson, "arena"));
            }
            catch (Exception ex)
            {
                continue;
            }

            if (location.getWorld() != null && location.getBlock().getState() instanceof Sign && LobbySigns.isValidWallSign((Sign)location.getBlock().getState()))
            {
                signs.add(new ArenaSign(arena, location));
            }
        }

        saveSigns();
    }

    public static void saveSigns()
    {
        List list = new ArrayList();

        for (ArenaSign ewsign : EggWars.signs)
        {
            Location loc = ewsign.getLocation();
            JsonObject json = new JsonObject();
            json.addProperty("x", loc.getX());
            json.addProperty("y", loc.getY());
            json.addProperty("z", loc.getZ());
            json.addProperty("world_name", loc.getWorld().getName());
            JsonObject json1 = new JsonObject();
            json1.add("location", json);
            json1.addProperty("arena", ewsign.getArena().getName());
            list.add(json1.toString());
        }

        EggWars.signsConfig.getConfig().set("Signs", list);
        EggWars.signsConfig.saveConfig();
    }

    public Gson getGson()
    {
        return this.gson;
    }

    public static ArenaLoader getArenaManager()
    {
        return instance.arenaLoader;
    }

    public static KitLoader getKitManager()
    {
        return instance.kitLoader;
    }

    public static TokenLoader getTokenManager()
    {
        return instance.tokenLoader;
    }

    public static GeneratorLoader getGeneratorManager()
    {
        return instance.generatorLoader;
    }

    public static TradingLoader getTradingManager()
    {
        return instance.tradingLoader;
    }

    public static Database getDB()
    {
        return instance.database;
    }

    public static LanguageManager languageManager()
    {
        return instance.languageManager;
    }

    public void saveCustomResource(String resPath, File outFile, boolean warnIfExists)
    {
        if (resPath == null || resPath.equals(""))
        {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resPath = resPath.replace('\\', '/');
        InputStream in = this.getResource(resPath);

        if (in == null)
        {
            this.getLogger().log(Level.SEVERE, "The embedded resource '" + resPath + "' cannot be found in " + this.getFile());
            return;
        }

        int lastIndex = resPath.lastIndexOf('/');

        if (outFile == null)
        {
            File outDir = new File(this.getDataFolder(), resPath.substring(0, (lastIndex >= 0) ? lastIndex : 0));
            outFile = new File(outDir, resPath.contains("/") ? resPath.substring((lastIndex >= 0) ? lastIndex + 1 : 1, resPath.length()) : resPath);

            if (!outDir.exists())
            {
                outDir.mkdirs();
            }
        }

        try
        {
            if (!outFile.exists())
            {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }

                out.close();
                in.close();
            }
            else if (warnIfExists)
            {
                this.getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists");
            }
        }
        catch (IOException ex)
        {
            this.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    public static void checkCurrentVersion()
    {
        EggWars.instance.getLogger().log(Level.INFO, "Checking plugin version...");
        String chkV = null;

        try
        {
            HttpURLConnection connection = (HttpURLConnection)(new URL("https://api.spigotmc.org/legacy/update.php?resource=97569")).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            chkV = (new BufferedReader(new InputStreamReader(connection.getInputStream()))).readLine();
            connection.disconnect();
        }
        catch (Exception ex)
        {
        }

        if (chkV != null && !chkV.equals(EGGWARS_VERSION) && !EGGWARS_VERSION.contains("dev") && !chkV.contains("dev"))
        {
            EggWars.instance.getLogger().log(Level.INFO, "Found new update (" + chkV + ")! You're still using \"" + EGGWARS_VERSION + "\". Download the latest version here (FREE): \"https://www.spigotmc.org/resources/free-eggwars-mini-game-remastered.97569/\"");
        }
        else if (chkV == null)
        {
            EggWars.instance.getLogger().log(Level.WARNING, "Failed to check updates from Spigot!");
        }
        else
        {
            EggWars.instance.getLogger().log(Level.INFO, "No updates found");
        }
    }
}
