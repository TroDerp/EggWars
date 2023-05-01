package me.rosillogames.eggwars;

import org.bukkit.configuration.file.FileConfiguration;
import me.rosillogames.eggwars.arena.Arena;

public class BungeeCord
{
    private boolean enabled = false;
    private String arenaName = "";
    private boolean randomArena = false;
    private boolean restart = false;
    private int gamesUntilRestart = 1;
    private String lobby = "";
    private Arena arena = null;

    public void addConfigDefaults(FileConfiguration fileConf)
    {
        fileConf.addDefault("bungee.enable", false);
        fileConf.addDefault("bungee.arena_display_name", "not_set");
        fileConf.addDefault("bungee.use_random_arena", false);
        fileConf.addDefault("bungee.restart", false);
        fileConf.addDefault("bungee.games_until_restart", 1);
        fileConf.addDefault("bungee.lobby", "not_set");
    }

    public void loadConfig(FileConfiguration fileConf)
    {
        this.enabled = fileConf.getBoolean("bungee.enable");
        this.arenaName = fileConf.getString("bungee.arena_display_name");
        this.arenaName = this.arenaName == null || this.arenaName.equals("not_set") ? "" : this.arenaName;
        this.randomArena = this.arenaName.isEmpty() || fileConf.getBoolean("bungee.use_random_arena");
        this.restart = fileConf.getBoolean("bungee.restart");
        this.gamesUntilRestart = fileConf.getInt("bungee.games_until_restart");
        this.lobby = fileConf.getString("bungee.lobby");
        this.lobby = this.lobby == null || this.lobby.equals("not_set") ? "" : this.lobby;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public boolean useRandomArena()
    {
        return this.randomArena;
    }

    public boolean shouldRestart()
    {
        return this.restart;
    }

    public int gamesUntilRestart()
    {
        return this.gamesUntilRestart;
    }

    public String getArenaName()
    {
        return this.arenaName;
    }

    public String getLobby()
    {
        return this.lobby;
    }

    public void setLobby(String s)
    {
        this.lobby = s;
        EggWars.instance.getConfig().set("bungee.lobby", s);
        EggWars.instance.saveConfig();
    }

    public Arena getArena()
    {
        return this.arena;
    }

    public void setArena(Arena arena)
    {
        this.arena = arena;
    }
}
