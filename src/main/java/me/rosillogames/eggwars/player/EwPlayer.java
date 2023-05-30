package me.rosillogames.eggwars.player;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.arena.game.Countdown;
import me.rosillogames.eggwars.dependencies.VaultEconomy;
import me.rosillogames.eggwars.enums.Mode;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.events.EwPlayerChangeLangEvent;
import me.rosillogames.eggwars.language.Language;
import me.rosillogames.eggwars.language.LanguageManager;
import me.rosillogames.eggwars.language.TranslationUtils;
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
    private TempGameData outsideDat;
    @Nullable
    private EwInventory inv;
    private final EwPlayerMenu menu;
    private Countdown invincibleTime;
    private long lastDamagerMillis;
    private Countdown timeUntilKit;
    private EwPlayer lastDamager;
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
        this.lastDamagerMillis = 0L;
        this.invincibleTime = null;
        this.timeUntilKit = null;
        this.menu = new EwPlayerMenu(this);

        try
        {
            this.menu.loadGuis();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        this.setLanguage(this.getLanguage());//this is to detect language for new players
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

    @Nullable
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
        if (!EggWars.bungee.isEnabled())
        {
            this.outsideDat = new TempGameData(this.getPlayer());
        }
    }

    public void restoreGameData()
    {
        if (this.outsideDat != null)
        {
            this.outsideDat.sendToPlayer();
            this.outsideDat = null;
        }
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
        return this.invincibleTime != null && this.invincibleTime.getCountdown() > 0;
    }

    public void setInvincible()
    {
        this.invincibleTime = new Countdown(EggWars.config.invincibilityTime);
        (new BukkitRunnable()
        {
            public void run()
            {
                if (EwPlayer.this.invincibleTime == null)
                {
                    this.cancel();
                    return;
                }

                EwPlayer.this.invincibleTime.decrease();

                if (EwPlayer.this.invincibleTime.getCountdown() == 0)
                {
                    this.cancel();
                    EwPlayer.this.clearInvincible();
                }
            }
        }).runTaskTimer(EggWars.instance, 0L, 20L);
    }

    public void clearInvincible()
    {
        this.invincibleTime = null;
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
        this.lastDamagerMillis = System.currentTimeMillis();
    }

    @Nullable
    public EwPlayer getLastDamager()
    {
        if (System.currentTimeMillis() - this.lastDamagerMillis > 20000L)//20 seconds
        {
            this.lastDamager = null;
        }

        return this.lastDamager;
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
            EggWars.getDB().getPlayerData(this.player).setPoints(i);
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

    public Language getLanguage()
    {
        String locale = EggWars.getDB().getPlayerData(this.player).getLocale();

        if (locale.isEmpty() || locale.equals("default"))
        {
            return EggWars.languageManager().ignoreClientLanguage ? LanguageManager.getDefaultLanguage() : EggWars.languageManager().getLanguageOrDefault(this.player.getLocale());
        }

        return EggWars.languageManager().getLanguageOrDefault(locale);
    }

    public void setLanguage(Language language)
    {
        EggWars.getDB().getPlayerData(this.player).setLocale(language.getLocale());

        try
        {
            EwPlayerChangeLangEvent ewplayerchangelangevent = new EwPlayerChangeLangEvent(this);
            Bukkit.getPluginManager().callEvent(ewplayerchangelangevent);
        }
        catch (LinkageError linkageerror)
        {
        }
    }

    public int timeUntilKit()
    {
        if (this.timeUntilKit == null)
        {
            return -1;
        }

        return this.timeUntilKit.getCountdown();
    }

    public void startKitCooldown(int cooldown)
    {
        this.timeUntilKit = new Countdown(cooldown);
        TranslationUtils.sendMessage("gameplay.kits.cooldown_started", this.getPlayer(), TranslationUtils.translateTime(this.getPlayer(), this.timeUntilKit(), true));
        (new BukkitRunnable()
        {
            public void run()
            {
                if (EwPlayer.this.timeUntilKit == null)
                {
                    this.cancel();
                    return;
                }

                EwPlayer.this.timeUntilKit.decrease();

                if (EwPlayer.this.timeUntilKit.getCountdown() == 0)
                {
                    this.cancel();
                    EwPlayer.this.clearKitCooldown();
                }
            }
        }).runTaskTimer(EggWars.instance, 0L, 20L);
    }

    public void clearKitCooldown()
    {
        this.timeUntilKit = null;
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
