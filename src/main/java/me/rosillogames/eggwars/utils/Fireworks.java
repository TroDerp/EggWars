package me.rosillogames.eggwars.utils;

import java.util.Random;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class Fireworks
{
    public static Firework genRawFirework(Location location)
    {
        return (Firework)location.getWorld().spawn(location, org.bukkit.entity.Firework.class);
    }

    /** Generates a firework at the given position with random properties **/
    public static Firework genFirework(Location location)
    {
        Firework firework = genRawFirework(location);
        Random random = new Random();
        Color color = getColor(random.nextInt(17) + 1);
        Color fadecolor = getColor(random.nextInt(17) + 1);
        FireworkEffect fireworkeffect = FireworkEffect.builder().flicker(random.nextBoolean()).withColor(color).withFade(fadecolor).with(org.bukkit.FireworkEffect.Type.values()[random.nextInt(org.bukkit.FireworkEffect.Type.values().length)]).trail(random.nextBoolean()).build();
        FireworkMeta fireworkmeta = firework.getFireworkMeta();
        fireworkmeta.addEffect(fireworkeffect);
        fireworkmeta.setPower(random.nextInt(2) + 1);
        firework.setFireworkMeta(fireworkmeta);
        return firework;
    }

    private static Color getColor(int i)
    {
        Color color = null;

        if (i == 1)
        {
            color = Color.AQUA;
        }

        if (i == 2)
        {
            color = Color.BLACK;
        }

        if (i == 3)
        {
            color = Color.BLUE;
        }

        if (i == 4)
        {
            color = Color.FUCHSIA;
        }

        if (i == 5)
        {
            color = Color.GRAY;
        }

        if (i == 6)
        {
            color = Color.GREEN;
        }

        if (i == 7)
        {
            color = Color.LIME;
        }

        if (i == 8)
        {
            color = Color.MAROON;
        }

        if (i == 9)
        {
            color = Color.NAVY;
        }

        if (i == 10)
        {
            color = Color.OLIVE;
        }

        if (i == 11)
        {
            color = Color.ORANGE;
        }

        if (i == 12)
        {
            color = Color.PURPLE;
        }

        if (i == 13)
        {
            color = Color.RED;
        }

        if (i == 14)
        {
            color = Color.SILVER;
        }

        if (i == 15)
        {
            color = Color.TEAL;
        }

        if (i == 16)
        {
            color = Color.WHITE;
        }

        if (i == 17)
        {
            color = Color.YELLOW;
        }

        return color;
    }
}
