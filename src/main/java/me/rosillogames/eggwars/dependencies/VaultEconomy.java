package me.rosillogames.eggwars.dependencies;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import me.rosillogames.eggwars.EggWars;

public class VaultEconomy
{
    private static Object vaultEco;

    public static boolean loadEconomy()
    {
        try
        {
            Class cEconomy = Class.forName(atMilkbowVaultPackage("economy.Economy"));
            RegisteredServiceProvider registeredserviceprovider = EggWars.instance.getServer().getServicesManager().getRegistration(cEconomy);

            if (registeredserviceprovider == null)
            {
                return false;
            }
            else
            {
                vaultEco = registeredserviceprovider.getProvider();
                return vaultEco != null;
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    public static void setPoints(OfflinePlayer offlineplayer, int i)
    {
        if (vaultEco == null)
        {
            return;
        }
        else
        {
            try
            {
                Class cEconomy = Class.forName(atMilkbowVaultPackage("economy.Economy"));
                double balance = (Double)cEconomy.getMethod("getBalance", new Class[] {OfflinePlayer.class}).invoke(vaultEco, new Object[] {offlineplayer});
                cEconomy.getMethod("withdrawPlayer", new Class[] {OfflinePlayer.class, double.class}).invoke(vaultEco, new Object[] {offlineplayer, balance});
                cEconomy.getMethod("depositPlayer", new Class[] {OfflinePlayer.class, double.class}).invoke(vaultEco, new Object[] {offlineplayer, (double)i});
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }

            return;
        }
    }

    public static double getBalance(OfflinePlayer offlineplayer)
    {
        if (vaultEco == null)
        {
            return 0;
        }
        else
        {
            try
            {
                Class cEconomy = Class.forName(atMilkbowVaultPackage("economy.Economy"));
                return (Double)cEconomy.getMethod("getBalance", new Class[] {OfflinePlayer.class}).invoke(vaultEco, new Object[] {offlineplayer});
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }

            return 0;
        }
    }

    public static String atMilkbowVaultPackage(String s)
    {
        return "net.milkbowl.vault." + s;
    }
}
