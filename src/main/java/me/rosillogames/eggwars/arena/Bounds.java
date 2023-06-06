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
        this.fix();
    }

    public Location getStart()
    {
        if (this.start != null)
        {
            return this.start.clone();
        }

        return null;
    }

    public void setStart(Location s)
    {
        this.start = s;
        this.fix();
    }

    public Location getEnd()
    {
        if (this.end != null)
        {
            return this.end.clone();
        }

        return null;
    }

    public void setEnd(Location e)
    {
        this.end = e;
        this.fix();
    }

    public boolean canPlaceAt(Location loc)
    {
        return this.areComplete() ? loc.getBlockX() >= this.start.getBlockX() && loc.getBlockY() >= this.start.getBlockY() && loc.getBlockZ() >= this.start.getBlockZ() && loc.getBlockX() <= this.end.getBlockX() && loc.getBlockY() <= this.end.getBlockY() && loc.getBlockZ() <= this.end.getBlockZ() : true;
    }

    public boolean areComplete()
    {
        return this.start != null && this.end != null;
    }

    private void fix()
    {
        if (this.start == null || this.end == null)
        {
            return;
        }

        int sX = this.start.getBlockX();
        int sY = this.start.getBlockY();
        int sZ = this.start.getBlockZ();
        int eX = this.end.getBlockX();
        int eY = this.end.getBlockY();
        int eZ = this.end.getBlockZ();
        this.start = new Location(null, sX > eX ? eX : sX, sY > eY ? eY : sY, sZ > eZ ? eZ : sZ);
        this.end = new Location(null, sX > eX ? sX : eX, sY > eY ? sY : eY, sZ > eZ ? sZ : eZ);
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
