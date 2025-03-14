package me.rosillogames.eggwars.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.enums.Mode;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.menu.ProfileMenus;
import me.rosillogames.eggwars.objects.AttackInstance;
import me.rosillogames.eggwars.objects.Cooldown;

public class EwPlayer
{
    private final IngameStats ingameStats = new IngameStats();
    private final Player player;
    @Nullable
    private Arena arena;
    @Nullable
    private Team team;
    private boolean eliminated;
    private boolean joining;
    @Nullable
    private TempGameData outsideDat;
    @Nullable
    private MenuPlayer menuPlayer;
    private final ProfileMenus profile;
    private final Cooldown invincibleRemain = new Cooldown();
    private final Cooldown lastDamagerRemain = new Cooldown();
    private final Cooldown kitCooldown = new Cooldown();
    @Nullable
    private EwPlayer lastDamager;
    @Nullable//cache last damager team to prevent a bug with message
    private TeamType lastDamagerTeam;
    private List<AttackInstance> assists = new ArrayList();
    @Nullable
    private EwPlayer trackedPlayer;
    private float votePower = 1.0F;

    public EwPlayer(Player player1)
    {
        this.player = player1;
        this.arena = null;
        this.team = null;
        this.eliminated = false;
        this.joining = false;
        this.outsideDat = null;
        this.menuPlayer = null;
        this.lastDamager = null;
        this.lastDamagerTeam = null;
        this.profile = new ProfileMenus(this);

        try
        {
            this.profile.loadGuis();
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
        this.clearAssists();
        this.kitCooldown.clear();
    }

    @Nullable
    public MenuPlayer getMenuPlayer()
    {
        if (this.menuPlayer == null)
        {
            this.menuPlayer = new MenuPlayer(this);
        }

        return this.menuPlayer;
    }

    public void setMenuPlayer(MenuPlayer menu)
    {
        this.menuPlayer = menu;
    }

    public ProfileMenus getProfile()
    {
        return this.profile;
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

    public void setLastDamager(EwPlayer ewplayer, float damage)
    {
        this.lastDamager = ewplayer;
        this.lastDamagerTeam = ewplayer.getTeam().getType();
        this.lastDamagerRemain.setFinish(EggWars.config.dmgForgetTime);
        this.assists.add(new AttackInstance(ewplayer, this.lastDamagerRemain.clone(), damage));
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

    public List<AttackInstance> getAssists()
    {
        this.recalculateAssists(0.0F);
        return new ArrayList<>(this.assists);
    }

    public void recalculateAssists(float heal)
    {
        (new ArrayList<>(this.assists)).stream().filter(attack -> attack.hasExpired()).forEach(attack -> this.assists.remove(attack));

        if (heal > 0.0F)
        {
            float remain = heal;

            for (int i = this.assists.size() - 1; i >= 0 && remain > 0.0F; --i)//backwards (don't need to copy)
            {
                AttackInstance atck = this.assists.get(i);
                remain = atck.getDamage() - remain;

                if (remain <= 0.0F)
                {
                    this.assists.remove(i);
                    remain = -remain;
                }
                else
                {
                    atck.setDamage(remain);
                }
            }
        }
    }

    public void clearAssists()
    {
        this.assists.clear();
    }

    @Nullable
    public TeamType getLastDamagerTeam()
    {
        return this.lastDamagerTeam;
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

    public class IngameStats
    {
        private final Map<StatType, Integer> stats = new HashMap<StatType, Integer>();

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
                me.rosillogames.eggwars.database.PlayerData playerData = EggWars.getDB().getPlayerData(EwPlayer.this.player);
                playerData.addStat(entry.getKey(), mode, entry.getValue());
                //TODO Re-make this (DONT FORGET ABOUT ADDSTAT!!) when completed migration
                //also maybe add option to determine if a player saves stats to DB on game end
                EggWars.getDB().saveStats(EwPlayer.this.player, playerData);
            }

            this.stats.clear();
        }
    }
}
