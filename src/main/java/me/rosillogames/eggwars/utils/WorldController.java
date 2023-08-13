package me.rosillogames.eggwars.utils;

import java.io.File;
import java.util.List;
import java.util.Random;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.FileUtil;
import com.google.common.collect.Lists;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class WorldController
{
    private static final String EGGWARS_MAP_TEMP = "eggwars-map-%s-tmp";

    public static String formatTmpWorldName(String arenaName)
    {
        return String.format(EGGWARS_MAP_TEMP, arenaName.toLowerCase());
    }

    public static World createArenaInitWorld(String arenaName)
    {
        String tempWorld = formatTmpWorldName(arenaName);
        boolean existed = false;

        if ((existed = worldFolderExists(arenaName)))
        {
            File file0 = new File(EggWars.instance.getServer().getWorldContainer(), arenaName);
            File file1 = new File(EggWars.instance.getServer().getWorldContainer(), tempWorld);
            copyFiles(file0, file1);
        }

        return createWorld(tempWorld, existed);
    }

    public static World createBungeeArenaTempWorld(String arenaName)
    {
        String fileName = formatTmpWorldName(arenaName);
        copyFiles(new File(EggWars.instance.getServer().getWorldContainer(), arenaName), new File(EggWars.instance.getServer().getWorldContainer(), fileName));
        return createWorld(fileName, true);
    }

    @SuppressWarnings("deprecation")
    public static World createWorld(String s, boolean existedBefore)
    {
        WorldCreator worldcreator = new WorldCreator(s);
        worldcreator.environment(org.bukkit.World.Environment.NORMAL);
        worldcreator.generateStructures(false);
        worldcreator.generator(new ChunkGenerator()
        {
            @Override
            public final ChunkGenerator.ChunkData generateChunkData(World world, Random random, int x, int z, ChunkGenerator.BiomeGrid chunkGererator)
            {
                ChunkGenerator.ChunkData chunkData = this.createChunkData(world);

                for (int i = 0; i < 16; i++)
                {
                    for (int j = 0; j < 4; j++)
                    {
                        for (int k = 0; k < 16; k++)
                        {
                            chunkGererator.setBiome(i, j, k, Biome.THE_VOID);
                        }
                    }
                }

                return chunkData;
            }
        });
        World world = worldcreator.createWorld();
        world.setDifficulty(Difficulty.NORMAL);
        world.setSpawnFlags(true, true);
        world.setPVP(true);
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(false);

        if (!existedBefore)
        {
            world.setTime(6000L);
        }

        world.setTicksPerAnimalSpawns(1);
        world.setTicksPerMonsterSpawns(1);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, Boolean.valueOf(false));
        world.setGameRule(GameRule.DISABLE_RAIDS, Boolean.valueOf(true));
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, Boolean.valueOf(false));
        world.setGameRule(GameRule.DO_MOB_SPAWNING, Boolean.valueOf(false));
        world.setGameRule(GameRule.DO_FIRE_TICK, Boolean.valueOf(false));
        world.setGameRule(GameRule.DO_INSOMNIA, Boolean.valueOf(false));
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, Boolean.valueOf(false));
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, Boolean.valueOf(false));
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, Boolean.valueOf(false));
        world.setGameRule(GameRule.MOB_GRIEFING, Boolean.valueOf(false));
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, Boolean.valueOf(false));
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, Boolean.valueOf(false));
        return world;
    }

    public static boolean copyFiles(File in, File out)
    {
        List<String> list = Lists.<String>newArrayList("uid.dat", "session.dat");
        boolean copied = true;

        if (!list.contains(in.getName()))
        {
            if (in.isDirectory())
            {
                if (!out.exists())
                {
                    out.mkdirs();
                }

                for (String s : in.list())
                {
                    if (!copyFiles(new File(in, s), new File(out, s)))
                    {
                        copied = false;
                    }
                }
            }
            else
            {
                copied = FileUtil.copy(in, out);
            }
        }

        return copied;
    }

    /** Unloads and deletes a world **/
    public static void deleteWorld(String worldName)
    {
        World world = EggWars.instance.getServer().getWorld(worldName);

        if (world != null)
        {
            EggWars.instance.getServer().unloadWorld(world, true);
        }

        deleteFiles(new File(EggWars.instance.getServer().getWorldContainer().getAbsolutePath(), worldName));
    }

    public static boolean deleteFiles(File file)
    {
        if (file.exists())
        {
            File afile[] = file.listFiles();

            for (int i = 0; i < afile.length; i++)
            {
                if (afile[i].isDirectory())
                {
                    deleteFiles(afile[i]);
                }
                else
                {
                    afile[i].delete();
                }
            }
        }

        return file.delete();
    }

    public static void saveArenaWorld(Arena arena)
    {
        World world = arena.getWorld();

        for (Entity entity : world.getEntities())
        {
            if (entity instanceof Player)
            {
                PlayerUtils.getEwPlayer((Player)entity).setSettingArena(null);
                PlayerUtils.tpToLobby((Player)entity, true);
            }
            else
            {
                entity.remove();
            }
        }

        ReflectionUtils.saveFullWorld(world);//use this because bukkit method does not save the full world at once, causing file corruption
        copyFiles(new File(EggWars.instance.getServer().getWorldContainer(), formatTmpWorldName(arena.getId())), new File(arena.arenaFolder, "world"));
    }

    public static World regenArena(Arena arena)
    {
        String fileName = formatTmpWorldName(arena.getId());
        deleteWorld(fileName);
        File worldSave = new File(arena.arenaFolder, "world");
        copyFiles(worldSave, new File(EggWars.instance.getServer().getWorldContainer(), fileName));
        return createWorld(fileName, new File(worldSave, "level.dat").exists());
    }

    public static boolean worldFolderExists(String s)
    {
        File worldFolder = new File(EggWars.instance.getServer().getWorldContainer(), s);
        return new File(worldFolder, "level.dat").exists();
    }

    public static void addPluginChunkTicket(Location loc)
    {
        loc.getWorld().addPluginChunkTicket(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, EggWars.instance);
    }
}
