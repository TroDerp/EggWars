package me.rosillogames.eggwars.managers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.GeneratorType;
import me.rosillogames.eggwars.utils.GsonHelper;
import me.rosillogames.eggwars.utils.ItemUtils;

public class GenDisplayManager
{
    private Set<ItemFrame> allItemFrames = new HashSet();
    private Map<GeneratorType, Map<Integer, ItemFrameView>> views = new HashMap();

    public void loadDisplays()
    {
        for (Map<Integer, ItemFrameView> fviews : this.views.values())
        {
            for (ItemFrameView ifViews : fviews.values())
            {
                if (ifViews.updateTask != null)
                {
                    ifViews.updateTask.cancel();
                }
            }
        }

        this.views.clear();
        File folder;

        for (GeneratorType type : EggWars.getGeneratorManager().getGenerators().values())
        {
            folder = new File(EggWars.instance.getDataFolder(), "custom/gen_images/" + type.getId());
            File file = new File(folder, "config.json");

            if (!file.exists())
            {
                continue;
            }

            try
            {
                Map<Integer, ItemFrameView> lvlViews = new HashMap();
                BufferedReader reader = Files.newBufferedReader(file.toPath());
                JsonObject configJson = GsonHelper.parse(reader);

                for (int lvl = 0; lvl <= type.getMaxLevel(); lvl++)
                {
                    JsonObject levelsList = GsonHelper.getAsJsonObject(configJson, "level" + lvl);

                    if (levelsList.isEmpty())
                    {
                        continue;
                    }

                    JsonArray frameList = GsonHelper.getAsJsonArray(levelsList, "frames", new JsonArray());

                    if (frameList.isEmpty())
                    {
                        continue;
                    }

                    List<FrameRenderer> images = new ArrayList();

                    for (int i = 0; i < frameList.size(); ++i)
                    {
                        JsonObject frameObj = (JsonObject)GsonHelper.convertToJsonObject(frameList.get(i), "frame");
                        String imgPath = GsonHelper.getAsString(frameObj, "file");
                        File imgFile = new File(folder, imgPath);

                        if (!imgFile.exists())
                        {
                            continue;
                        }

                        MapView map = Bukkit.getServer().createMap((World)Bukkit.getServer().getWorlds().get(0));
                        int tickWait = GsonHelper.getAsInt(frameObj, "next_wait", 20);

                        try
                        {
                            BufferedImage img = ImageIO.read(imgFile);
                            FrameRenderer imgRenderer = new FrameRenderer(img, tickWait);
                            map.addRenderer(imgRenderer);
                            images.add(imgRenderer);
                        }
                        catch (IOException var3)
                        {
                            Bukkit.getLogger().severe("Generator image in path " + imgFile.toString() + " doesn't exist!");
                        }
                    }

                    ItemFrameView view = new ItemFrameView(images, type, lvl);
                    view.initTask();
                    lvlViews.put(lvl, view);
                }

                this.views.put(type, lvlViews);
            }
            catch (IOException exc)
            {
                EggWars.instance.getLogger().log(Level.WARNING, "Error images for generator type " + type.getId() + ": ", exc);
            }
        }

        for (ItemFrame poster : this.allItemFrames)
        {
            this.reloadPoster(poster);
        }
    }

    @Nullable
    public ItemFrame putPoster(GeneratorType type, int lvl, World w, Location loc)
    {
        BlockState state = loc.getBlock().getState();

        if (!(state.getBlockData() instanceof WallSign) || !this.views.containsKey(type) || !this.views.get(type).containsKey(lvl))
        {
            return null;
        }

        BlockFace facing = ((WallSign)state.getBlockData()).getFacing();
        loc.getBlock().setType(Material.AIR);
        ItemFrame poster = loc.getWorld().spawn(loc, ItemFrame.class);
        poster.setFacingDirection(facing);
        ItemFrameView view = this.views.get(type).get(lvl);
        putMapOnFrame(poster, view.getCurrentView());
        ItemUtils.genType.setTo(poster, type.getId());
        ItemUtils.genLevel.setTo(poster, lvl);
        view.addPoster(poster);
        this.allItemFrames.add(poster);
        return poster;
    }

    public boolean updatePoster(ItemFrame poster, GeneratorType type, int lvl)
    {
        if (this.views.containsKey(type) && this.views.get(type).containsKey(lvl))
        {
            ItemFrameView view = this.views.get(type).get(lvl);
            putMapOnFrame(poster, view.getCurrentView());
            ItemUtils.genType.setTo(poster, type.getId());
            ItemUtils.genLevel.setTo(poster, lvl);
            view.addPoster(poster);
            return true;
        }

        this.allItemFrames.remove(poster);
        return false;
    }

    public void reloadPoster(ItemFrame poster)
    {//TODO
        if (!poster.isDead() && ItemUtils.genType.isInHolder(poster) && ItemUtils.genLevel.isInHolder(poster))
        {
            GeneratorType type = EggWars.getGeneratorManager().getType(ItemUtils.genType.getFrom(poster));
            int lvl = ItemUtils.genLevel.getFrom(poster);

            if (this.views.containsKey(type) && this.views.get(type).containsKey(lvl))
            {
                ItemFrameView view = this.views.get(type).get(lvl);
                putMapOnFrame(poster, view.getCurrentView());
                view.addPoster(poster);
                return;
            }
        }

        return;
    }

    private static void putMapOnFrame(ItemFrame frame, MapView view)
    {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta)item.getItemMeta();
        mapMeta.setMapView(view);
        item.setItemMeta(mapMeta);
        frame.setItem(item, false);
    }

    private class ItemFrameView
    {
        private final List<FrameRenderer> frameViews;
        private final GeneratorType genType;
        private final int genLvl;
        private Set<ItemFrame> onUseBy = new HashSet();
        private int currentView = 0;
        private BukkitTask updateTask = null;

        public ItemFrameView(List<FrameRenderer> views, GeneratorType type, int lvl)
        {
            this.genType = type;
            this.genLvl = lvl;
            this.frameViews = views;
        }

        public void addPoster(ItemFrame poster)
        {
            this.onUseBy.add(poster);
        }

        public void initTask()
        {
            this.updateTask = (new BukkitRunnable()
            {
                public void run()
                {
                    ItemFrameView.this.update();
                }
            }).runTaskLater(EggWars.instance, (long)this.frameViews.get(this.currentView).delay);
        }

        public MapView getCurrentView()
        {
            return this.frameViews.get(this.currentView).view;
        }

        public void update()
        {
            this.currentView = (this.currentView + 1) % this.frameViews.size();

            if (this.frameViews.size() > 1 && this.updateTask != null)
            {
                this.initTask();
            }

            HashSet newSet = new HashSet<ItemFrame>();

            for (ItemFrame poster : this.onUseBy)
            {
                if (!poster.isDead() && ItemUtils.genType.valueEquals(poster, this.genType.getId()) && ItemUtils.genLevel.valueEquals(poster, this.genLvl))
                {
                    putMapOnFrame(poster, this.getCurrentView());
                    newSet.add(poster);
                }
            }

            this.onUseBy.clear();
            this.onUseBy = newSet;
        }
    }

    private static class FrameRenderer extends MapRenderer
    {
        private final BufferedImage frame;
        protected MapView view = null;
        protected int delay = 0;

        public FrameRenderer(BufferedImage frameIn, int delayIn)
        {
            this.frame = frameIn;
            this.delay = delayIn;
        }

        @Override
        public void initialize(@NotNull MapView map)
        {
            this.view = map;
        }

        @Override
        public void render(MapView map, MapCanvas canvas, Player player)
        {
            canvas.drawImage(0, 0, this.frame);
        }
    }
}
