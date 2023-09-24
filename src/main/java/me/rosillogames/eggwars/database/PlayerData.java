package me.rosillogames.eggwars.database;

import java.util.HashMap;
import javax.annotation.Nullable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.Mode;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.objects.Kit;

public class PlayerData
{
    private HashMap<String, KitData> kits;
    private HashMap<String, HashMap<String, Integer>> stats;
    private HashMap<String, Integer> totalStats;
    private String locale;
    private String currentKit;
    private int points;
    private boolean classicShop;

    public PlayerData()
    {
        this.kits = new HashMap<String, KitData>();
        this.stats = new HashMap<String, HashMap<String, Integer>>();
        this.totalStats = new HashMap<String, Integer>();

        for (StatType type : StatType.values())
        {
            this.stats.putIfAbsent(type.name(), new HashMap<String, Integer>());

            for (Mode mode : Mode.values())
            {
                this.stats.get(type.name()).putIfAbsent(mode.name(), 0);
            }

            this.totalStats.putIfAbsent(type.name(), 0);
        }

        this.points = 0;
        this.locale = "default";
        this.currentKit = "";
        this.classicShop = false;
    }

    public void addStat(StatType stat, Mode mode, int amount)
    {
        this.stats.putIfAbsent(stat.name(), new HashMap<String, Integer>());
        this.stats.get(stat.name()).put(mode.name(), this.stats.get(stat.name()).getOrDefault(mode.name(), 0) + amount);
        this.totalStats.put(stat.name(), this.totalStats.getOrDefault(stat.name(), 0) + amount);
    }

    public int getStat(StatType stat, Mode mode)
    {
        return this.stats.get(stat.name()).getOrDefault(mode.name(), 0);
    }

    public int getTotalStat(StatType stat)
    {
        return this.totalStats.getOrDefault(stat.name(), 0);
    }

    public void addPoints(int p)
    {
        this.points += p;
    }

    public void takePoints(int p)
    {
        this.points -= p;
    }

    public void unlockKit(String kitID)
    {
        this.kits.putIfAbsent(kitID, new KitData());
        this.kits.get(kitID).unlocked = true;
        this.kits.get(kitID).unlockTime = System.currentTimeMillis();
    }

    public boolean hasKit(String kitID)
    {
        return this.kits.containsKey(kitID) && this.kits.get(kitID).unlocked;
    }

    public int timeSinceKit(String kitID)
    {
        if (!this.kits.containsKey(kitID))
        {
            return -1;
        }

        long boughtTime = this.kits.get(kitID).unlockTime;
        return (int)((System.currentTimeMillis() - boughtTime) / 1000);
    }

    public HashMap<String, KitData> getKits()
    {
        return this.kits;
    }

    public HashMap<String, HashMap<String, Integer>> getStats()
    {
        return this.stats;
    }

    public HashMap<String, Integer> getTotalStats()
    {
        return this.totalStats;
    }

    public String getLocale()
    {
        return this.locale;
    }

    @Nullable
    public Kit getKit()
    {
        return EggWars.getKitManager().getKit(this.currentKit);
    }

    public int getPoints()
    {
        return this.points;
    }

    public boolean isClassicShop()
    {
        return this.classicShop;
    }

    public void setKits(HashMap<String, KitData> kits)
    {
        this.kits = kits;
    }

    public void setStats(HashMap<String, HashMap<String, Integer>> stats)
    {
        this.stats = stats;
    }

    public void setTotalStats(HashMap<String, Integer> totalStats)
    {
        this.totalStats = totalStats;
    }

    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    public boolean setKit(String kitID)
    {
        if (this.currentKit.equals(kitID))
        {
            return false;
        }

        this.currentKit = kitID;
        return true;
    }

    public void setPoints(int p)
    {
        this.points = p;
    }

    public void setClassicShop(boolean classicShop)
    {
        this.classicShop = classicShop;
    }

    static class KitData
    {
        private boolean unlocked = false;
        private long unlockTime = -1L;
    }
}
