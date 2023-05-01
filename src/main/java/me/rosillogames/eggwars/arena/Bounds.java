package me.rosillogames.eggwars.arena;

import org.bukkit.Location;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.rosillogames.eggwars.utils.GsonHelper;
import me.rosillogames.eggwars.utils.Locations;

public class Bounds
{
    protected Location start;
    protected Location end;

    public Bounds(Location startIn, Location endIn)
    {
        this.start = startIn;
        this.end = endIn;
    }

    public Location getStart()
    {
        return this.start.clone();
    }

    public void setStart(Location s)
    {
        this.start = s;
    }

    public Location getEnd()
    {
        return this.end.clone();
    }

    public void setEnd(Location e)
    {
        this.end = e;
    }

    public boolean canPlaceAt(Location loc)
    {
        return this.areComplete() ? loc.getBlockX() >= this.start.getBlockX() && loc.getBlockY() >= this.start.getBlockY() && loc.getBlockZ() >= this.start.getBlockZ() && loc.getBlockX() <= this.end.getBlockX() && loc.getBlockY() <= this.end.getBlockY() && loc.getBlockZ() <= this.end.getBlockZ() : true;
    }

    public boolean areComplete()
    {
        return this.start != null && this.end != null;
    }

    public static Bounds deserialize(String json)
    {
        Bounds builder = new Bounds(null, null);

        try
        {
            JsonObject jsonObj = GsonHelper.parse(json);

            try
            {
                return new Bounds(new Location(null, GsonHelper.getAsInt(jsonObj, "min_x"), GsonHelper.getAsInt(jsonObj, "min_y"), GsonHelper.getAsInt(jsonObj, "min_z")), new Location(null, GsonHelper.getAsInt(jsonObj, "max_x"), GsonHelper.getAsInt(jsonObj, "max_y"), GsonHelper.getAsInt(jsonObj, "max_z")));
            }
            catch (JsonSyntaxException ex)
            {
                if (jsonObj.has("start_pos"))
                {
                    builder.setStart(Locations.fromString(GsonHelper.getAsString(jsonObj, "start_pos")));
                }

                if (jsonObj.has("end_pos"))
                {
                    builder.setEnd(Locations.fromString(GsonHelper.getAsString(jsonObj, "end_pos")));
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return builder;
    }

    public static String serialize(Bounds bounds)
    {
        JsonObject jsonObj = new JsonObject();

        if (bounds != null)
        {
            String start = Locations.toString(bounds.getStart(), false);

            if (start != null)
            {
                jsonObj.addProperty("start_pos", start);
            }

            String end = Locations.toString(bounds.getEnd(), false);

            if (end != null)
            {
                jsonObj.addProperty("end_pos", end);
            }
        }

        return jsonObj.toString();
    }
}
