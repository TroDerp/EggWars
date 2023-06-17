package me.rosillogames.eggwars.objects;

import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import com.mojang.datafixers.util.Pair;
import me.rosillogames.eggwars.loaders.KitLoader;

public class Kit
{
    private final List<Pair<EquipmentSlot, ItemStack>> items;
    private final String id;
    private final ItemStack display;
    private final String name;
    private final String desc;
    private final int price;
    private final int cooldown;

    public Kit(List<Pair<EquipmentSlot, ItemStack>> list, String key, ItemStack display, int j, int k)
    {
        this.items = list;
        this.id = key;
        this.display = display;
        String kitT = "kit." + key;
        this.name = kitT + ".name";
        this.desc = kitT + ".desc";
        this.price = j;
        this.cooldown = k;
    }

    public List<Pair<EquipmentSlot, ItemStack>> items()
    {
        return this.items;
    }

    public String id()
    {
        return this.id;
    }

    public ItemStack displayItem()
    {
        return this.display.clone();
    }

    public String getName()
    {
        return this.name;
    }

    public String getDesc()
    {
        return this.desc;
    }

    public int price()
    {
        return this.price;
    }

    public int cooldownTime()
    {
        return this.cooldown < 0 ? KitLoader.cooldownSeconds : this.cooldown;
    }

    public void equip(Player player)
    {
        for (Pair<EquipmentSlot, ItemStack> pair : this.items)
        {
            EquipmentSlot slot = pair.getFirst();
            ItemStack stack = pair.getSecond();

            if (slot != null && slot != EquipmentSlot.HAND && player.getInventory().getItem(slot) == null)
            {
                player.getInventory().setItem(slot, stack);
            }
            else
            {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(stack);

                for (ItemStack item : remaining.values())
                {
                    player.getWorld().dropItem(player.getLocation(), item).setVelocity(new Vector(0.0, 0.0, 0.0));
                }
            }
        }
    }

    public String toString()
    {
        return "EwKit[Name=" + this.name + ",DisplayItem=" + this.display.toString() + ",Items=" + this.items.toString() + ",Id=" + this.id + ",CooldownTime=" + this.cooldown + ",Price=" + this.price + ",Description=" + this.desc + "]";
    }

    public int hashCode()
    {
        int i = 1;
        i = 31 * i + this.id.hashCode();
        return i;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass())
        {
            return false;
        }

        return this.id.equals(((Kit)obj).id);
    }
}
