package me.rosillogames.eggwars.objects;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.utils.LobbySigns;

public class ArenaSign
{
    private final Arena arena;
    private final Location location;
    private final Location support;

    public ArenaSign(Arena arena1, Location location1)
    {
        this.arena = arena1;
        this.location = location1;
        Sign sign = (Sign)location1.getBlock().getState();
        this.support = sign.getBlock().getRelative(((WallSign)sign.getBlockData()).getFacing().getOppositeFace()).getLocation();
    }

    @SuppressWarnings("deprecation")
    public void update()
    {
        if (this.location.getBlock().getState() instanceof Sign)
        {
            Sign sign = (Sign)this.location.getBlock().getState();
            ArenaStatus status = this.arena.getStatus();//TODO: why is "setup" in this translation!?
            Object[] args = new Object[] {this.arena.getName(), TranslationUtils.getMessage("status." + status.toString()), Integer.toString(status.isGame() || status == ArenaStatus.FINISHING ? this.arena.getAlivePlayers().size() : this.arena.getPlayers().size()), Integer.toString(this.arena.getMaxPlayers())};
            sign.setLine(0, TranslationUtils.getMessage("setup.sign.arena.line_1", args));
            sign.setLine(1, TranslationUtils.getMessage("setup.sign.arena.line_2", args));
            sign.setLine(2, TranslationUtils.getMessage("setup.sign.arena.line_3", args));
            sign.setLine(3, TranslationUtils.getMessage("setup.sign.arena.line_4", args));
            sign.update();
            LobbySigns.setBlock(this);
        }
    }

    public Arena getArena()
    {
        return this.arena;
    }

    public Location getLocation()
    {
        return this.location;
    }

    public Location getSupport()
    {
        return this.support;
    }

    public int hashCode()
    {
        int i = 1;
        i = 31 * i + (this.location != null ? this.location.hashCode() : 0);
        return i;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (this.getClass() != obj.getClass())
        {
            return false;
        }

        ArenaSign ewsign = (ArenaSign)obj;

        if (this.location == null)
        {
            if (ewsign.location != null)
            {
                return false;
            }
        }
        else if (!this.location.equals(ewsign.location))
        {
            return false;
        }

        return true;
    }
}
