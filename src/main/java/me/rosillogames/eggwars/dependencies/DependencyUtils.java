package me.rosillogames.eggwars.dependencies;

import me.rosillogames.eggwars.EggWars;

public class DependencyUtils
{//Note: be sure to not use these classes without checking first
    public DependencyUtils()
    {
    }

    public static boolean vault()
    {
        if (EggWars.instance.getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }

        return VaultEconomy.loadEconomy();
    }

    public static void registerEggWarsPlaceHolders()
    {
        if (EggWars.instance.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null)
        {
            return;
        }

        (new EggWarsExpansionPAPI()).register();
    }
}
