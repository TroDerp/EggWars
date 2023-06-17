package me.rosillogames.eggwars.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import me.rosillogames.eggwars.EggWars;

public class Database
{
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private final EggWars plugin;
    private Connection connection;

    //adding parameters doesn't reset the database
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

            this.createTable();
        }
        catch (SQLException ex1)
        {
            ex1.printStackTrace();
            Bukkit.getPluginManager().disablePlugin((Plugin)plugin);
        }
    }

    public void loadPlayer(Player p)
    {
        try
        {
            PreparedStatement select = this.connection.prepareStatement("SELECT * FROM ew_players WHERE UUID=?;");
            select.setString(1, p.getUniqueId().toString());
            ResultSet result = select.executeQuery();
            PlayerData bw;

            if (result.next())
            {
                bw = Database.this.plugin.getGson().<PlayerData>fromJson(result.getString("Data"), PlayerData.class);
            }
            else
            {
                bw = new PlayerData();
                final PreparedStatement insert = this.connection.prepareStatement("INSERT INTO ew_players (`UUID`, `Name`, `Data`) VALUES (?, ?, ?);");
                insert.setString(1, p.getUniqueId().toString());
                insert.setString(2, p.getName());
                insert.setString(3, Database.this.plugin.getGson().toJson(bw, PlayerData.class));
                insert.execute();
                Database.this.close(insert, null);
            }

            Database.this.close(select, result);
            Database.this.players.put(p.getUniqueId(), bw);
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
            this.players.remove(p.getUniqueId());
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

    private void createTable()
    {
        try
        {
            final Statement statement = this.connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ew_players(UUID varchar(36) primary key, Name varchar(20), Data LONGTEXT);");
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
