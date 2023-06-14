package me.rosillogames.eggwars.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigAccessor
{
    private final String fileName;
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration fileConfiguration;

    public ConfigAccessor(JavaPlugin javaplugin, String s)
    {
        if (javaplugin == null)
        {
            throw new IllegalArgumentException("plugin cannot be null");
        }

        this.plugin = javaplugin;
        this.fileName = s;
        File file = javaplugin.getDataFolder();

        if (file == null)
        {
            throw new IllegalStateException();
        }
        else
        {
            this.configFile = new File(javaplugin.getDataFolder(), s);
            return;
        }
    }

    public ConfigAccessor(JavaPlugin javaplugin, File file)
    {
        if (javaplugin == null)
        {
            throw new IllegalArgumentException("plugin cannot be null");
        }

        this.plugin = javaplugin;
        this.fileName = file.getName();
        File file1 = javaplugin.getDataFolder();

        if (file1 == null)
        {
            throw new IllegalStateException();
        }
        else
        {
            this.configFile = file;
            return;
        }
    }

    public FileConfiguration getConfig()
    {
        if (this.fileConfiguration == null)
        {
            this.reloadConfig();
        }

        return this.fileConfiguration;
    }

    /** Creates a new config instance and deletes the file of older instance **/
    public void createNewConfig()
    {
        try
        {
            if (!this.configFile.exists())
            {
                this.configFile.createNewFile();
            }
            else
            {
                this.configFile.delete();
            }
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }

        this.fileConfiguration = new YamlConfiguration();
    }

    public void reloadConfig()
    {
        try
        {
            if (!this.configFile.exists())
            {
                try
                {
                    this.configFile.createNewFile();
                }
                catch (IOException ioexception)
                {
                    ioexception.printStackTrace();
                }
            }

            this.fileConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(this.configFile), "UTF-8"));
            InputStream inputstream = this.plugin.getResource(this.fileName);

            if (inputstream != null)
            {
                YamlConfiguration yamlconfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(inputstream, "UTF-8"));
                this.fileConfiguration.setDefaults(yamlconfiguration);
            }
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
    }

    public void saveConfig()
    {
        if (this.fileConfiguration != null && this.configFile != null)
        {
            try
            {
                this.getConfig().save(this.configFile);
            }
            catch (IOException ioexception)
            {
                this.plugin.getLogger().log(Level.SEVERE, (new StringBuilder()).append("Could not save config to ").append(this.configFile).toString(), ioexception);
            }
        }
    }

    public void saveDefaultConfig()
    {
        if (!this.configFile.exists())
        {
            this.plugin.saveResource(this.fileName, false);
        }
    }
}
