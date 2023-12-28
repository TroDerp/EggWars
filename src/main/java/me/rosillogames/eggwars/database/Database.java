package me.rosillogames.eggwars.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.Mode;
import me.rosillogames.eggwars.enums.StatType;

public class Database
{
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private final EggWars plugin;
    private Connection connection;

    //adding parameters to PlayerData doesn't reset the database, but remember "ALTER TABLE" for outside
    public Database(EggWars plugin)
    {
        this.plugin = plugin;

        try
        {
            if (plugin.getConfig().getBoolean("database.auto_mode"))
            {
                File datafile = new File(plugin.getDataFolder(), "/EggWars.db");

                if (!datafile.exists())
                {
                    try
                    {
                        datafile.createNewFile();
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                        Bukkit.getPluginManager().disablePlugin((Plugin)plugin);
                    }
                }

                this.connection = DriverManager.getConnection("jdbc:sqlite:" + datafile);
            }
            else
            {
                this.connection = DriverManager.getConnection(plugin.getConfig().getString("database.url"), plugin.getConfig().getString("database.username"), plugin.getConfig().getString("database.password"));
            }

            this.createTables();
        }
        catch (SQLException ex1)
        {
            ex1.printStackTrace();
            Bukkit.getPluginManager().disablePlugin((Plugin)plugin);
        }
    }

    public void loadPlayer(OfflinePlayer p)
    {
        try
        {
            PreparedStatement select = this.connection.prepareStatement("SELECT * FROM ew_players WHERE UUID=?;");
            select.setString(1, p.getUniqueId().toString());
            ResultSet result = select.executeQuery();
            PlayerData bw;

            if (result.next())
            {
                bw = this.plugin.getGson().<PlayerData>fromJson(result.getString("Data"), PlayerData.class);
            }
            else
            {
                bw = new PlayerData();
                final PreparedStatement insert = this.connection.prepareStatement("INSERT INTO ew_players (`UUID`, `Name`, `Data`) VALUES (?, ?, ?);");
                insert.setString(1, p.getUniqueId().toString());
                insert.setString(2, p.getName());
                insert.setString(3, this.plugin.getGson().toJson(bw, PlayerData.class));
                insert.execute();
                this.close(insert, null);
            }

            this.close(select, result);
            this.players.put(p.getUniqueId(), bw);

            if (!bw.hasMigratedStats())
            {
                StringBuilder sb = new StringBuilder("INSERT INTO ew_stats (`UUID`, `Name`");
                StringBuilder sb1 = new StringBuilder(") VALUES (?, ?");

                for (StatType stattype : StatType.values())
                {
                    for (String mode1 : Arrays.asList("total", "solo", "teams"))
                    {//TODO refactor Mode to StatMode and add "total"
                        sb.append(", `").append(mode1).append("_").append(stattype.name().toLowerCase()).append("`");
                        sb1.append(", ?");
                    }
                }

                sb.append(sb1).append(");");
                PreparedStatement insert = this.connection.prepareStatement(sb.toString());
                insert.setString(1, p.toString());
                insert.setString(2, p.getName());
                int idx = 3;

                for (StatType stattype : StatType.values())
                {
                    for (int mode1 = 0; mode1 < 3; mode1++)
                    {
                        int statvalue = mode1 == 0 ? bw.getTotalStat(stattype) : bw.getStat(stattype, mode1 == 1 ? Mode.SOLO : Mode.TEAM);
                        insert.setInt(idx, statvalue);
                        idx++;
                    }
                }

                insert.execute();
                this.close(insert, null);
                bw.migratedStats();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private static final String SAVE = "UPDATE ew_players SET Data=? WHERE UUID=?;";

    public void savePlayers()
    {
        for (Map.Entry<UUID, PlayerData> entry : this.players.entrySet())
        {
            try
            {
                PreparedStatement statement = this.connection.prepareStatement(SAVE);
                statement.setString(1, this.plugin.getGson().toJson(entry.getValue(), PlayerData.class));
                statement.setString(2, entry.getKey().toString());
                statement.execute();
                this.close(statement, null);
                this.saveStats(entry.getKey(), entry.getValue());
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }

        this.players.clear();
    }

    public void savePlayer(Player p)
    {
        PlayerData bw = this.players.get(p.getUniqueId());

        if (bw == null)
        {
            return;
        }

        try
        {
            PreparedStatement statement = this.connection.prepareStatement(SAVE);
            statement.setString(1, this.plugin.getGson().toJson(bw, PlayerData.class));
            statement.setString(2, p.getUniqueId().toString());
            statement.execute();
            this.close(statement, null);
            this.saveStats(p.getUniqueId(), bw);
            this.players.remove(p.getUniqueId());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public int loadStat(OfflinePlayer p, String mode, String stat)
    {
        int value = -1;

        try
        {
            PreparedStatement select = this.connection.prepareStatement("SELECT * FROM ew_stats WHERE UUID=?;");
            select.setString(1, p.getUniqueId().toString());
            ResultSet result = select.executeQuery();

            if (result.next())
            {
                value = result.getInt(mode.toString().toLowerCase() + "_" + stat.toLowerCase());
            }//also create stats entry when querying offline?

            this.close(select, result);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return value;
    }

    public void saveStats(UUID uuid, PlayerData data)
    {//NEVER remove saveStats from savePlayer(s) to make sure they always get saved
        try
        {
            StringBuilder sb = new StringBuilder("UPDATE ew_stats SET Name=?");

            for (StatType stattype : StatType.values())
            {
                for (String mode : Arrays.asList("total", "solo", "teams"))
                {
                    sb.append(", ").append(mode).append("_").append(stattype.name().toLowerCase()).append("=?");
                }
            }

            sb.append(" WHERE UUID=?;");
            PreparedStatement update = this.connection.prepareStatement(sb.toString());
            update.setString(1, uuid.toString());
            int idx = 1;

            for (StatType stattype : StatType.values())
            {
                for (int mode = 0; mode < 3; mode++)
                {
                    int statvalue = mode == 0 ? data.getTotalStat(stattype) : data.getStat(stattype, mode == 1 ? Mode.SOLO : Mode.TEAM);
                    update.setInt(idx++, statvalue);
                }
            }

            update.execute();
            this.close(update, null);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void close()
    {
        if (this.connection != null)
        {
            try
            {
                this.connection.close();
            }
            catch (SQLException throwables)
            {
                throwables.printStackTrace();
            }
        }
    }

    private void createTables()
    {
        try
        {
            final Statement statement = this.connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ew_players(UUID varchar(36) primary key, Name varchar(20), Data LONGTEXT);");
            StringBuilder sb = new StringBuilder();

            for (StatType stattype : StatType.values())
            {
                for (String mode : Arrays.asList("total", "solo", "teams"))
                {
                    sb.append(", ").append(mode + "_").append(stattype.name().toLowerCase()).append(" INT");
                }
            }

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ew_stats(UUID varchar(36) primary key, Name varchar(20)" + sb.toString().trim() + ");");
            this.close(statement);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void close(PreparedStatement statement, ResultSet result)
    {
        if (statement != null)
        {
            try
            {
                statement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        if (result != null)
        {
            try
            {
                result.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void close(Statement statement)
    {
        if (statement != null)
        {
            try
            {
                statement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    public PlayerData getPlayerData(Player p)
    {
        return this.players.get(p.getUniqueId());
    }
}
