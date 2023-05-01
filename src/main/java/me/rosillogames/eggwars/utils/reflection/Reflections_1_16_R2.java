package me.rosillogames.eggwars.utils.reflection;

import java.lang.reflect.Field;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class Reflections_1_16_R2 extends Reflections_1_16_R3
{
    @Override
    public void setTNTSource(TNTPrimed tnt, Player player)
    {
        try
        {
            Object obj = tnt.getClass().getMethod("getHandle", new Class[0]).invoke(tnt, new Object[0]);
            Class cEntityTNTPrimed = this.getNMSClass("EntityTNTPrimed");
            Field field = cEntityTNTPrimed.getDeclaredField("source");
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(obj, this.toEntityHuman(player));
            field.setAccessible(accessible);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void hideDyeFlag(LeatherArmorMeta meta)
    {
    }
}
