package me.rosillogames.eggwars.dependencies;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import me.rosillogames.eggwars.EggWars;
import net.milkbowl.vault.economy.Economy;

public class VaultEconomy
{
    private static Economy vaultEco;

    public static boolean loadEconomy()
    {
        RegisteredServiceProvider registeredserviceprovider = EggWars.instance.getServer().getServicesManager().getRegistration(Economy.class);

        if (registeredserviceprovider == null)
        {
            return false;
        }

        vaultEco = (Economy)registeredserviceprovider.getProvider();
        return vaultEco != null;
    }

    public static void setPoints(OfflinePlayer offPlayer, int amount)
    {
        if (vaultEco == null)
        {
            return;
        }

        vaultEco.withdrawPlayer(offPlayer, vaultEco.getBalance(offPlayer));
        vaultEco.depositPlayer(offPlayer, (double)amount);
    }

    public static int getBalance(OfflinePlayer offPlayer)
    {
        if (vaultEco == null)
        {
            return 0;
        }

        return (int)vaultEco.getBalance(offPlayer);
    }
}
