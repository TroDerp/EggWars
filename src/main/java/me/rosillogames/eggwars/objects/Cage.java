package me.rosillogames.eggwars.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.utils.Colorizer;
import me.rosillogames.eggwars.utils.Locations;

public class Cage
{//TODO for big update
    private final Team team;
    private Location location;
    private boolean rotate = true;//whether if cage structure must follow rotation from location
    private boolean mirror = false;//whether to mirror structure or not
    private Player inhabitor;

    public Cage(Team team)
    {
        this.team = team;
    }

    public void setWorld(World world)
    {
        if (this.location != null)
        {
            this.location.setWorld(world);
        }
    }

    public Location getLocation()
    {
        if (this.location != null)
        {
            return this.location.clone();
        }

        return null;
    }

    public void setLocation(Location loc)
    {
        this.location = Locations.toBlock(loc, false);
    }

    public boolean getRotate()
    {
        return rotate;
    }

    public void setRotate(boolean rotate)
    {
        this.rotate = rotate;
    }

    public boolean getMirror()
    {
        return mirror;
    }

    public void setMirror(boolean mirror)
    {
        this.mirror = mirror;
    }

    public void place(Player player)
    {
        this.inhabitor = player;
        Material gls = Colorizer.colorize(Material.GLASS, this.team.getType().woolColor());

        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 3; j++)
            {
                for (int k = -1; k <= 1; k++)
                {
                    BlockState state = this.getLocation().add(i, j, k).getBlock().getState();

                    if ((i > -1 && i < 1 && j > -1 && j < 3 && k > -1 && k < 1) || ((j == -1 || j == 3) && (i == -1 || k == -1 || i == 1 || k == 1)) || ((i == -1 && k == -1) || (i == -1 && k == 1) || (i == 1 && k == -1) || (i == 1 && k == 1)))
                    {
                        state.setType(Material.AIR);
                    }
                    else
                    {
                        state.setType(gls);
                    }

                    state.update(true);
                }
            }
        }

        player.teleport(Locations.toMiddle(this.location));
    }

    public void remove()
    {
        if (this.inhabitor != null)
        {
            for (int i = -1; i <= 1; i++)
            {
                for (int j = -1; j <= 3; j++)
                {
                    for (int k = -1; k <= 1; k++)
                    {
                        BlockState blockstate = this.getLocation().add(i, j, k).getBlock().getState();
                        blockstate.setType(Material.AIR);
                        blockstate.update(true);
                    }
                }
            }

            this.inhabitor = null;
        }
    }

    public Player getInhabitor()
    {
        return this.inhabitor;
    }
}
