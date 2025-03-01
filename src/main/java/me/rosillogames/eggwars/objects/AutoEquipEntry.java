package me.rosillogames.eggwars.objects;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.rosillogames.eggwars.utils.ItemUtils;

public class AutoEquipEntry implements Listener
{
    private final EquipmentSlot slot;
    private final List<Material> replaces;
    private final List<Material> doesntReplace;
    private final boolean replaceEnchanted;

    /** When "*" is specified as "replaces", it means it can replace everything */
    public AutoEquipEntry(EquipmentSlot slot, boolean replaceEnchanted)
    {
        this.slot = slot;
        this.replaces = null;
        this.doesntReplace = null;
        this.replaceEnchanted = replaceEnchanted;
        
    }

    public AutoEquipEntry(EquipmentSlot slot, List<Material> replaces, List<Material> doesntReplace, boolean replaceEnchanted)
    {
        this.slot = slot;
        this.replaces = replaces;
        this.doesntReplace = doesntReplace;
        this.replaceEnchanted = replaceEnchanted;
    }

    public EquipmentSlot getTradeSlot(Player player)
    {
        ItemStack stack = ItemUtils.getSlot(player, this.slot);
        Material mat;

        if (stack == null || (mat = stack.getType()) == Material.AIR)
        {
            return this.slot;
        }

        if (!this.replaceEnchanted && !stack.getEnchantments().isEmpty())
        {
            return EquipmentSlot.HAND;
        }

        if (this.replaces == null)
        {
            return this.slot;
        }

        boolean replace = !this.doesntReplace.isEmpty();

        for (Material badMat : this.doesntReplace)
        {
            replace = replace && (mat != badMat);
        }

        if (!replace)
        {
            for (Material goodMat : this.replaces)
            {
                if (goodMat == mat)
                {
                    replace = true;
                    break;
                }
            }
        }

        return replace ? this.slot : EquipmentSlot.HAND;
    }
}
