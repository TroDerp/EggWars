package me.rosillogames.eggwars.player.inventory;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class InventoryController
{
    public static EwInventory openInventory(Player player, TranslatableInventory inv, MenuType type)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);
        EwInventory newInventory = new EwInventory(ewplayer, inv, type);
        newInventory.setParent(ewplayer.getInv());
        ewplayer.setInv(newInventory);
        player.openInventory(newInventory.getInventory());
        return newInventory;
    }

    public static void closeInventory(Player player, boolean goToParent)
    {
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(player);

        if (goToParent && ewplayer.getInv() != null && ewplayer.getInv().getParent() != null)
        {
            EwInventory parentInventory = ewplayer.getInv().getParent();

            while (true)
            {
                if (parentInventory != null && parentInventory.getInventoryType().equals(ewplayer.getInv().getInventoryType()))
                {
                    parentInventory = parentInventory.getParent();
                    continue;
                }

                break;
            }

            if (parentInventory != null)
            {
                ewplayer.setInv(parentInventory);
                parentInventory.updateHandler(null, true);
                return;
            }
        }

        player.closeInventory();
        ewplayer.setInv(null);
    }

    public static void updateInventory(EwPlayer ewplayer, @Nullable TranslatableInventory inv, boolean reopen)
    {
        if (ewplayer.getInv() != null)
        {
            ewplayer.getInv().updateHandler(inv, reopen);
        }
    }

    public static void updateInventories(Predicate<EwPlayer> predicate, @Nullable TranslatableInventory inv, MenuType type)
    {
        for (EwPlayer ewplayer : EggWars.players)
        {
            if (predicate.test(ewplayer) && ewplayer.getInv() != null && ewplayer.getInv().getInventoryType() == type)
            {
                ewplayer.getInv().updateHandler(inv, true);
            }
        }
    }
}
