package me.rosillogames.eggwars.arena;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.Colorizer;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.WorldController;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class Team
{
    private final Set<EwPlayer> players = new HashSet();
    private final Set<Entity> villager = new HashSet();
    private final Arena arena;
    private final List<Location> cages = new ArrayList();
    private final Inventory teamChest;
    private Location egg;
    private Location villagerLoc;
    private Location respawn;
    private TeamType type;

    public Team(Arena arena, TeamType color)
    {
        this.arena = arena;
        this.type = color;
        this.teamChest = Bukkit.createInventory(null, InventoryType.ENDER_CHEST);
    }

    public void setArenaWorld()
    {
        if (this.egg != null)
        {
            this.egg.setWorld(this.arena.getWorld());
        }

        if (this.villagerLoc != null)
        {
            this.villagerLoc.setWorld(this.arena.getWorld());
        }

        if (this.respawn != null)
        {
            this.respawn.setWorld(this.arena.getWorld());
        }

        for (Location cage : this.cages)
        {
            cage.setWorld(this.arena.getWorld());
        }
    }

    public Set<EwPlayer> getPlayers()
    {
        return new HashSet(this.players);
    }

    public Set<EwPlayer> getPlayersAlive()
    {
        Set<EwPlayer> set = new HashSet();

        for (EwPlayer ewplayer : this.players)
        {
            if (!ewplayer.isEliminated())
            {
                set.add(ewplayer);
            }
        }

        return set;
    }

    public Arena getArena()
    {
        return this.arena;
    }

    /** Returns a copy of the list of all glass locations.
     ** WARNING: The locations on the list aren't clones!
     **/
    public List<Location> getCages()
    {
        return new ArrayList(this.cages);
    }

    /** Adds a glass location. The parameter location is cloned and moved to
     ** the raw block position without the decimals.
     **/
    public void addCage(Location location)
    {
        this.cages.add(Locations.toBlock(location, false));
    }

    public boolean removeLastCage()
    {
        if (!this.cages.isEmpty())
        {
            this.cages.remove(this.cages.size() - 1);
            return true;
        }

        return false;
    }

    /** Returns a copy of the raw respawn position, without middling it **/
    public Location getRespawn()
    {
        if (this.respawn != null)
        {
            return this.respawn.clone();
        }

        return null;
    }

    /** Sets the respawn location. The parameter location is cloned and moved to
     ** the raw block position without the decimals.
     **/
    public void setRespawn(Location location)
    {
        this.respawn = Locations.toBlock(location, false);
    }

    /** Returns a copy of the raw egg position, without middling it **/
    public Location getEgg()
    {
        if (this.egg != null)
        {
            return this.egg.clone();
        }

        return null;
    }

    /** Sets the egg location. The parameter location is cloned and moved to
     ** the raw block position without the decimals.
     **/
    public void setEgg(Location location)
    {
        this.egg = Locations.toBlock(location, true);
    }

    /** Returns a copy of the raw villager position, without middling it **/
    public Location getVillager()
    {
        if (this.villagerLoc != null)
        {
            return this.villagerLoc.clone();
        }

        return null;
    }

    /** Sets the villager location. The parameter location is cloned and moved to
     ** the raw block position without the decimals.
     **/
    public void setVillager(Location location)
    {
        this.villagerLoc = Locations.toBlock(location, false);
    }

    public TeamType getType()
    {
        return this.type;
    }

    public void setType(TeamType typeIn)
    {
        this.type = typeIn;
    }

    public Inventory getEnderChest()
    {
        return this.teamChest;
    }

    public boolean canJoin()
    {
        if (this.players.size() >= this.arena.getMaxTeamPlayers())
        {
            return false;
        }

        if (!EggWars.config.balanceTeams)
        {
            return true;
        }
        else
        {
            int i = (int)Math.floor(this.arena.getPlayers().size() / this.arena.getTeams().size());
            return this.players.size() <= i && this.arena.getMaxTeamPlayers() > this.players.size();
        }
    }

    public boolean canRespawn()
    {
        return this.egg.getBlock().getType().equals(Material.DRAGON_EGG);
    }

    public void addPlayer(EwPlayer ewplayer)
    {
        this.players.add(ewplayer);
        ewplayer.setTeam(this);
    }

    public void removePlayer(EwPlayer ewplayer)
    {
        this.players.remove(ewplayer);

        if (ewplayer.getTeam().equals(this))
        {
            ewplayer.setTeam(null);
        }
    }

    public void prepareForGame()
    {
        WorldController.addPluginChunkTicket(this.villagerLoc);

        //Egg

        if (!this.players.isEmpty())
        {
            this.egg.getBlock().setType(Material.DRAGON_EGG);
        }

        //Villager

        this.removeVillager();

        Location middleLoc = Locations.toMiddle(this.villagerLoc);

        Villager vill = middleLoc.getWorld().spawn(middleLoc, Villager.class);
        vill.setAdult();
        vill.setProfession(Villager.Profession.NONE);
        vill.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false));
        vill.setGravity(false);
        vill.setCustomName(TranslationUtils.getMessage("gameplay.villager.name"));
        vill.setCustomNameVisible(true);
        this.villager.add(vill);

        ArmorStand sup = middleLoc.getWorld().spawn(middleLoc, ArmorStand.class);
        ReflectionUtils.setArmorStandInvisible(sup);
        sup.setSmall(true);
        sup.setAI(false);
        sup.setMarker(true);
        vill.addPassenger(sup);
        this.villager.add(sup);

        ArmorStand up = middleLoc.getWorld().spawn(middleLoc.clone().add(0.0, 1.975, 0.0), ArmorStand.class);
        ReflectionUtils.setArmorStandInvisible(up);
        up.setAI(false);
        up.setMarker(true);
        up.setCustomName(TranslationUtils.getMessage("gameplay.villager.name"));
        up.setCustomNameVisible(true);
        this.villager.add(up);

        ArmorStand blw = middleLoc.getWorld().spawn(middleLoc.clone().add(0.0, 1.715, 0.0), ArmorStand.class);
        ReflectionUtils.setArmorStandInvisible(blw);
        blw.setAI(false);
        blw.setMarker(true);
        blw.setCustomName(TranslationUtils.getMessage("gameplay.villager.below"));
        blw.setCustomNameVisible(true);
        this.villager.add(blw);
    }

    public void tpPlayersToCages()
    {
        int spawned = 0;

        for (EwPlayer ewplayer : this.players)
        {
            if (spawned < this.cages.size())
            {
                ewplayer.getPlayer().teleport(Locations.toMiddle(this.cages.get(spawned)));
            }
            else
            {
                ewplayer.getPlayer().teleport(Locations.toMiddle(this.respawn));
            }

            spawned++;
        }
    }

    public void placeCages()
    {
        Material gls = Colorizer.colorize(Material.GLASS, this.getType().woolColor());

        if (!this.players.isEmpty() && (this.arena.getReleaseCountdown() != 0 || this.arena.skipSoloLobby()))
        {
            for (Location cage : this.cages)
            {
                for (int i = -1; i <= 1; i++)
                {
                    for (int j = -1; j <= 3; j++)
                    {
                        for (int k = -1; k <= 1; k++)
                        {
                            BlockState state = cage.clone().add(i, j, k).getBlock().getState();

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
            }
        }
    }

    public void removeCages()
    {
        for (Location spawn : this.cages)
        {
            for (int i = -1; i <= 1; i++)
            {
                for (int j = -1; j <= 3; j++)
                {
                    for (int k = -1; k <= 1; k++)
                    {
                        BlockState blockstate = spawn.clone().add(i, j, k).getBlock().getState();
                        blockstate.setType(Material.AIR);
                        blockstate.update(true);
                    }
                }
            }
        }
    }

    public void removeVillager()
    {
        for (Entity entity : this.villager)
        {
            entity.remove();
        }
    }

    public boolean isEliminated()
    {
        for (EwPlayer ewplayer : this.players)
        {
            if (!ewplayer.isEliminated())
            {
                return false;
            }
        }

        return true;
    }

    public void reset()
    {
        this.removeVillager();
        this.players.clear();
        this.removeCages();
        this.egg.getBlock().setType(Material.AIR);
        this.teamChest.clear();

        //removeFromScore
        for (EwPlayer ewplayer : this.arena.getPlayers())
        {
            Scoreboard scoreboard = ewplayer.getPlayer().getScoreboard();
            scoreboard.getTeam(this.type.id()).unregister();
        }
    }

    public void broadcastEliminated()
    {
        for (EwPlayer ewpl : this.arena.getPlayers())
        {
            Player pl = ewpl.getPlayer();
            TranslationUtils.sendMessage("gameplay.ingame.team_eliminated", pl, TeamUtils.translateTeamType(this.type, pl, false));
            pl.playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 0.0F);
        }
    }

    public int hashCode()
    {
        int i = 1;
        i = 31 * i + (this.arena != null ? this.arena.hashCode() : 0);
        i = 31 * i + this.type.ordinal();
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

        Team team = (Team)obj;

        if (this.arena == null)
        {
            if (team.arena != null)
            {
                return false;
            }
        }
        else if (!this.arena.equals(team.arena))
        {
            return false;
        }

        return this.type == team.type;
    }
}
