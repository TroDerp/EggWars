package me.rosillogames.eggwars.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import com.google.gson.JsonObject;

public class Locations
{
    public static Location toBlock(Location l1, boolean removeDir)
    {
        Location l2 = l1.clone();
        l2.setX(l1.getBlockX());
        l2.setY(l1.getBlockY());
        l2.setZ(l1.getBlockZ());

        if (removeDir)
        {
            l2.setYaw(0.0F);
            l2.setPitch(0.0F);
        }

        return l2;
    }

    public static Location toMiddle(Location l1)
    {
        return l1.clone().add(0.5D, 0.0D, 0.5D);
    }

    public static String toString(Location location, boolean yawPitch)
    {
        if (location == null)
        {
            return null;
        }

        JsonObject json = new JsonObject();
        json.addProperty("x", location.getX());
        json.addProperty("y", location.getY());
        json.addProperty("z", location.getZ());

        if (yawPitch)
        {
            json.addProperty("yaw", location.getYaw());
            json.addProperty("pitch", location.getPitch());
        }

        return json.toString();
    }

    public static Location fromString(String s)
    {
        JsonObject json = GsonHelper.parse(s);
        double d0 = (double)GsonHelper.getAsFloat(json, "x");
        double d1 = (double)GsonHelper.getAsFloat(json, "y");
        double d2 = (double)GsonHelper.getAsFloat(json, "z");

        if (json.has("yaw") && json.has("pitch"))
        {
            float f0 = GsonHelper.getAsFloat(json, "yaw");
            float f1 = GsonHelper.getAsFloat(json, "pitch");
            return new Location(null, d0, d1, d2, f0, f1);
        }

        return new Location(null, d0, d1, d2);
    }

    public static String toStringWithWorld(Location location, boolean yawPitch)
    {
        JsonObject json = new JsonObject();
        json.addProperty("x", location.getX());
        json.addProperty("y", location.getY());
        json.addProperty("z", location.getZ());

        if (yawPitch)
        {
            json.addProperty("yaw", location.getYaw());
            json.addProperty("pitch", location.getPitch());
        }

        json.addProperty("world_name", location.getWorld().getName());
        return json.toString();
    }

    public static Location fromStringWithWorld(String s)
    {
        JsonObject json = GsonHelper.parse(s);
        World world = Bukkit.getWorld(GsonHelper.getAsString(json, "world_name"));
        double d0 = (double)GsonHelper.getAsFloat(json, "x");
        double d1 = (double)GsonHelper.getAsFloat(json, "y");
        double d2 = (double)GsonHelper.getAsFloat(json, "z");

        if (json.has("yaw") && json.has("pitch"))
        {
            float f0 = GsonHelper.getAsFloat(json, "yaw");
            float f1 = GsonHelper.getAsFloat(json, "pitch");
            return new Location(world, d0, d1, d2, f0, f1);
        }

        return new Location(world, d0, d1, d2);
    }
}
