package me.rosillogames.eggwars.player;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.dependencies.VaultEconomy;
import me.rosillogames.eggwars.enums.Mode;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.events.EwPlayerChangeLangEvent;
import me.rosillogames.eggwars.objects.Cooldown;
import me.rosillogames.eggwars.objects.Kit;
import me.rosillogames.eggwars.player.inventory.EwInventory;

public class EwPlayer
{
    private final IngameStats ingameStats = new IngameStats();
    private final Player player;
    @Nullable
    private Arena arena;
    @Nullable
    private Arena settingArena;
    @Nullable
    private Team team;
    private boolean eliminated;
    private boolean joining;
    @Nullable
    private TempGameData outsideDat;
    @Nullable
    private EwInventory inv;
    private final EwPlayerMenu menu;
    private final Cooldown invincibleRemain = new Cooldown();
    private final Cooldown lastDamagerRemain = new Cooldown();
    private final Cooldown kitCooldown = new Cooldown();
    @Nullable
    private EwPlayer lastDamager;
    @Nullable//cache last damager team to prevent a bug with message
    private TeamType lastDamagerTeam;
    @Nullable
    private EwPlayer trackedPlayer;
    private float votePower = 1.0F;

    public EwPlayer(Player player1)
    {
        this.player = player1;
        this.arena = null;
        this.settingArena = null;
        this.team = null;
        this.eliminated = false;
        this.joining = false;
        this.outsideDat = null;
        this.inv = null;
        this.lastDamager = null;
        this.menu = new EwPlayerMenu(this);

        try
        {
            this.menu.loadGuis();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void setArena(Arena arena)
    {
        this.arena = arena;
    }

    @Nullable
    public Arena getArena()
    {
        return this.arena;
    }

    public void setSettingArena(Arena arena)
    {
        this.settingArena = arena;
    }

    @Nullable//TODO Remove for next version, this will only create bugs on the future
    public Arena getSettingArena()
    {
        return this.settingArena;
    }

    public Player getPlayer()
    {
        return this.player;
    }

    /** Note: this does not count if the player is in a setting arena **/
    public boolean isInArena()
    {
        return this.arena != null;
    }

    @Nullable
    public Team getTeam()
    {
        return this.team;
    }

    public void setTeam(Team team)
    {
        this.team = team;
    }

    public boolean isEliminated()
    {
        return this.eliminated;
    }

    public void setEliminated(boolean flag)
    {
        this.eliminated = flag;
    }

    public boolean isJoining()
    {
        return this.joining;
    }

    public void setJoining(boolean flag)
    {
        this.joining = flag;
    }

    public void storeGameData()
    {
        this.restoreGameData();
        this.outsideDat = new TempGameData(this.getPlayer());
    }

    public void restoreGameData()
    {
        if (this.outsideDat != null)
        {
            this.outsideDat.sendToPlayer();
            this.outsideDat = null;
        }

        this.clearInvincible();
        this.clearLastDamager();
        this.kitCooldown.clear();
    }

    @Nullable
    public EwInventory getInv()
    {
        return this.inv;
    }

    public void setInv(EwInventory invplayer)
    {
        this.inv = invplayer;
    }

    public EwPlayerMenu getMenu()
    {
        return this.menu;
    }

    @Nullable
    public Kit getKit()
    {
        return EggWars.getDB().getPlayerData(this.player).getKit();
    }

    public boolean isInvincible()
    {
        return !this.invincibleRemain.hasFinished();
    }

    public void setInvincible()
    {
        this.invincibleRemain.setFinish(EggWars.config.invincibleTime);
    }

    public void clearInvincible()
    {
        this.invincibleRemain.clear();
    }

    public boolean hasKit(Kit kit)
    {
        if (kit.price() <= 0)
        {
            return true;
        }
        else
        {
            return EggWars.getDB().getPlayerData(this.player).hasKit(kit.id());
        }
    }

    public void setLastDamager(EwPlayer ewplayer)
    {
        this.lastDamager = ewplayer;
        this.lastDamagerTeam = ewplayer.getTeam().getType();
        this.lastDamagerRemain.setFinish(20);
    }

    public void clearLastDamager()
    {
        this.lastDamager = null;
        this.lastDamagerTeam = null;
        this.lastDamagerRemain.clear();
    }

    @Nullable
    public EwPlayer getLastDamager()
    {
        if (this.lastDamagerRemain.hasFinished())
        {
            this.clearLastDamager();
        }

        return this.lastDamager;
    }

    @Nullable
    public TeamType getLastDamagerTeam()
    {
        return this.lastDamagerTeam;
    }

    public int getPoints()
    {
        if (EggWars.config.vault)
        {
            int i = VaultEconomy.getBalance(this.player);
            EggWars.getDB().getPlayerData(this.player).setPoints(i);
            return i;
        }
        else
        {
            return EggWars.getDB().getPlayerData(this.player).getPoints();
        }
    }

    public void setPoints(int i)
    {
        if (EggWars.config.vault)
        {
            VaultEconomy.setPoints(this.player, i);
        }

        EggWars.getDB().getPlayerData(this.player).setPoints(i);
    }

    public float getVotePower()
    {
        return this.votePower;
    }

    public void setVotePower(float f)
    {
        this.votePower = f;
    }

    public IngameStats getIngameStats()
    {
        return this.ingameStats;
    }

    public Cooldown getKitCooldown()
    {
        return this.kitCooldown;
    }

    @Nullable
    public EwPlayer getTrackedPlayer()
    {
        return this.trackedPlayer;
    }

    public void setTrackedPlayer(@Nullable EwPlayer player)
    {
        this.trackedPlayer = player;
    }

    public String getLangId()
    {
        return EggWars.getDB().getPlayerData(this.player).getLocale();
    }

    public void setLangId(String langCode)
    {
        EggWars.getDB().getPlayerData(this.player).setLocale(langCode);

        try
        {
            EwPlayerChangeLangEvent ewplayerchangelangevent = new EwPlayerChangeLangEvent(this);
            Bukkit.getPluginManager().callEvent(ewplayerchangelangevent);
        }
        catch (LinkageError linkageerror)
        {
        }
    }

    public class IngameStats
    {
        private final HashMap<StatType, Integer> stats = new HashMap<StatType, Integer>();

        public void addStat(StatType stat, int amount)
        {
            this.stats.put(stat, this.stats.getOrDefault(stat, 0) + amount);
        }

        public int getStat(StatType stat)
        {
            return this.stats.getOrDefault(stat, 0);
        }

        public void saveAndReset(Mode mode)
        {
            for (Map.Entry<StatType, Integer> entry : this.stats.entrySet())
            {
                EggWars.getDB().getPlayerData(EwPlayer.this.player).addStat(entry.getKey(), mode, entry.getValue());
            }

            this.stats.clear();
        }
    }
}
