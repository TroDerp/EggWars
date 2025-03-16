package me.rosillogames.eggwars.arena;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import com.google.common.collect.Lists;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.menu.EwMenu;
import me.rosillogames.eggwars.menu.ProfileMenus;
import me.rosillogames.eggwars.menu.SerializingItems;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.objects.Token;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.MenuPlayer;
import me.rosillogames.eggwars.utils.Fireworks;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.WorldController;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class Generator extends EwMenu
{
    private static final String TAG_PROGRESS_DOT = "|"/* or "¦" */;
    private static final int TAG_PROGRESS_BAR_LENGHT = 30;
    private final Arena arena;
    private final Location block;
    private int defLevel;
    private final String type;
    @Nullable
    private GeneratorType cachedType = null;
    private int level;
    @Nullable
    private BukkitTask tickTask = null;
    //Keep generator ticks (if reloadCache functions during game, they will keep progress)
    private int genTicks;
    @Nullable
    private ArmorStand stand = null;
    //Awesome Produce Sharing System as described by CubeCraft
    //When more than a single player are next to the generator, its product will be given to the player their turn corresponds with.
    private APSS apss = new APSS();

    public Generator(Location loc, int lvl, String typeIn, Arena arenaIn)
    {
        super(MenuType.GENERATOR_INFO);
        this.block = Locations.toBlock(loc, true);
        this.defLevel = lvl;
        this.level = lvl;
        this.type = typeIn;
        this.arena = arenaIn;
    }

    public Location getBlock()
    {
        return this.block.clone();
    }

    public int getDefLevel()
    {
        return this.defLevel;
    }

    public int getLevel()
    {
        return this.level;
    }

    public String getType()
    {
        return this.type;
    }

    public APSS getAPSS()
    {
        return this.apss;
    }

    private boolean isMaxLevel()
    {
        return this.level >= this.cachedType.getMaxLevel();
    }

    public void prepareForGame()
    {
        WorldController.addPluginChunkTicket(this.block);
        this.updateSign();
    }

    public void start()
    {
        if (!this.hasCachedType())
        {
            return;
        }

        if (this.cachedType.showTimeTag())
        {
            this.stand = (ArmorStand)this.block.getWorld().spawn(this.block.clone().add(0.5, 0.475, 0.5), org.bukkit.entity.ArmorStand.class);
            ReflectionUtils.setArmorStandInvisible(this.stand);
            this.stand.setAI(false);
            this.stand.setMarker(true);
            this.stand.teleport(this.block.clone().add(0.5, 0.475, 0.5));
            StringBuilder sb = new StringBuilder();//Using stringbuilder for optimization

            for (int i = 0; i < TAG_PROGRESS_BAR_LENGHT; ++i)
            {
                sb.append(ChatColor.RED).append(TAG_PROGRESS_DOT);
            }

            this.stand.setCustomName(sb.toString());
            this.stand.setCustomNameVisible(true);
        }

        final int tickRate = this.cachedType.tickRate(this.level);
        this.tickTask = (new BukkitRunnable()
        {
            public void run()
            {
                if (!Generator.this.cachedType.requiresNearbyPlayer() || PlayerUtils.getNearbyPlayerAmount(Generator.this.block, 3.0, Generator.this.arena) >= 1)
                {
                    if (ItemUtils.getNearbyItemCount(Generator.this.block, 2.5, Generator.this.cachedType.droppedToken().getMaterial()) < Generator.this.cachedType.maxItems(Generator.this.level))
                    {
                        Generator.this.genTicks++;
                    }
                    else
                    {
                        return;
                    }
                }
                else if (Generator.this.genTicks > 0)
                {
                    Generator.this.genTicks--;
                }

                Generator.this.apss.update();

                if (Generator.this.genTicks >= tickRate)
                {
                    Generator.this.apss.nextTurn();
                    Generator.this.generate();
                    Generator.this.genTicks = 0;
                }

                if (Generator.this.stand != null)
                {
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < TAG_PROGRESS_BAR_LENGHT; ++i)
                    {
                        sb.append(((float)Generator.this.genTicks / (float)tickRate) > ((float)i / (float)TAG_PROGRESS_BAR_LENGHT) ? ChatColor.GREEN : ChatColor.RED).append(TAG_PROGRESS_DOT);
                    }

                    Generator.this.stand.setCustomName(sb.toString());
                }
            }
        }).runTaskTimer(EggWars.instance, 0L, this.level != 0 ? 1L : 20L);
    }

    public void stop()
    {
        if (this.stand != null)
        {
            this.stand.remove();
            this.stand = null;
        }

        if (this.tickTask != null)
        {
            this.tickTask.cancel();
        }
    }

    public void reset()
    {
        this.stop();
        this.genTicks = 0;
        this.tickTask = null;
        this.level = this.defLevel;
        this.closeForEveryone(false);
    }

    private void generate()
    {
        if (this.level <= 0 || !this.hasCachedType())
        {
            return;
        }

        ItemStack stack = new ItemStack(this.cachedType.droppedToken().getMaterial(), 1);
        final Item itemEnt = this.block.getWorld().dropItem(this.getBlock().add(0.5, 0.2, 0.5), stack);

        if (EggWars.config.enableAPSS)
        {
            itemEnt.setThrower(this.apss.uuid);
        }

        itemEnt.setVelocity(new Vector(0, 0, 0));
        itemEnt.setPickupDelay(0);

        if (!EggWars.instance.getConfig().getBoolean("generator.fast_items"))
        {
            //This will make items don't merge, having more impact on performance, enable the option to disable this
            ReflectionUtils.setItemAge(itemEnt, -32768);
            (new BukkitRunnable()
            {
                public void run()
                {
                    if (!itemEnt.isDead())
                    {
                        double dx = (new Random()).nextDouble();

                        if (dx > 0.8)
                        {
                            dx -= 0.15;
                        }

                        if (dx < 0.2)
                        {
                            dx += 0.15;
                        }

                        double dz = (new Random()).nextDouble();

                        if (dz > 0.8)
                        {
                            dz -= 0.15;
                        }

                        if (dz < 0.2)
                        {
                            dz += 0.15;
                        }

                        itemEnt.setVelocity(new Vector(0, 0, 0));
                        itemEnt.teleport(Generator.this.getBlock().add(dx, 0.0, dz));
                    }
                }
            }).runTaskLater(EggWars.instance, 15L);
        }
    }

    @SuppressWarnings("deprecation")
    public void updateSign()
    {
        if (this.block.getBlock().getState() instanceof Sign && this.hasCachedType())
        {
            Sign sign = (Sign)this.block.getBlock().getState();
            Token token = this.cachedType.droppedToken();
            Object[] args = new Object[] {token.getFormattedName((Player)null), this.level != 0 ? TranslationUtils.getMessage("generator.sign.level", new Object[] {Integer.valueOf(this.level)}) : TranslationUtils.getMessage("generator.sign.broken")};
            sign.setLine(0, TranslationUtils.getMessage("generator.sign.line_1", args));
            sign.setLine(1, TranslationUtils.getMessage("generator.sign.line_2", args));
            sign.setLine(2, TranslationUtils.getMessage("generator.sign.line_3", args));
            sign.setLine(3, TranslationUtils.getMessage("generator.sign.line_4", args));
            sign.update();
        }
    }

    public void setArenaWorld()
    {
        this.block.setWorld(this.arena.getWorld());
    }

    public boolean tryUpgrade(Player playerIn)
    {
        if (this.isMaxLevel())
        {
            TranslationUtils.sendMessage("generator.cant_upgrade", playerIn);
            return false;
        }

        Price price = this.cachedType.getPriceFor(this.level + 1);

        if (!Price.canAfford(playerIn, price))
        {
            TranslationUtils.sendMessage("generator.upgrade_cant_afford", playerIn, Price.leftFor(price, playerIn), price.getToken().getFormattedName(playerIn));
            return false;
        }

        Price.sellItems(playerIn, price);
        this.level++;
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(playerIn);

        for (EwPlayer ewplayer1 : ewplayer.getTeam().getPlayers())
        {
            Player teamPlayer = ewplayer1.getPlayer();
            TranslationUtils.sendMessage("generator.team_upgraded", teamPlayer, TeamUtils.colorizePlayerName(ewplayer), this.cachedType.droppedToken().getFormattedName(teamPlayer), this.level);
        }

        this.stop();
        this.start();
        Firework firework = Fireworks.genRawFirework(this.block.clone().add(0.5D, 0.0D, 0.5D));
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Builder fwbuilder = FireworkEffect.builder();
        fwbuilder.with(FireworkEffect.Type.BALL);
        fwbuilder.withColor(Color.fromRGB(this.cachedType.getColor()));
        fwbuilder.withFade(Color.WHITE);
        fwbuilder.withTrail();
        meta.addEffect(fwbuilder.build());
        firework.setFireworkMeta(meta);
        (new BukkitRunnable()
        {//Don't detonate immediately (+2 ticks) so the "launch" sound can play. Can't use "firework.setMaxLife(1)" because it was added in bukkit 1.19.
            public void run()
            {
                firework.detonate();
            }
        }).runTaskLater(EggWars.instance, 2L);
        this.updateSign();

        if (EggWars.instance.getConfig().getBoolean("generator.close_ui_on_upgrade"))
        {
            this.closeForEveryone(true);
        }
        else
        {
            this.sendMenuUpdate(true);
        }

        return true;
    }

    public boolean hasCachedType()
    {
        return this.cachedType != null;
    }

    public void reloadCache()
    {
        GeneratorType newType = EggWars.getGeneratorManager().getType(this.type);

        if ((this.cachedType == null && newType != null) || (this.cachedType != null && !this.cachedType.equals(newType)))//Very important to use 'equals' here!
        {
            if (this.tickTask != null)
            {
                this.stop();
            }

            this.cachedType = newType;

            if (newType != null)
            {
                this.defLevel = Math.min(this.defLevel, newType.getMaxLevel());
                this.level = Math.min(this.level, newType.getMaxLevel());

                //Checks if there was a tick task active before (by checking if it's not null, because it is removed only on arena reset)
                //to know if it has to start generating again (so it can re/spawn armor stand, menu, etc.)
                if (this.tickTask != null)
                {
                    this.start();
                }

                this.updateSign();
                this.sendMenuUpdate(true);
            }
            else
            {
                this.closeForEveryone(false);
            }
        }
    }

    @Nullable
    @Override
    public Inventory translateToPlayer(MenuPlayer menuPly, boolean reopen)
    {
        if (!this.hasCachedType())
        {
            return null;
        }

        Inventory mcInventory;
        Player player = menuPly.getPlayer();

        if (menuPly.getMenu() == this && !reopen)
        {
            mcInventory = menuPly.getCurrentInventory();
        }
        else
        {
            mcInventory = Bukkit.createInventory(null, 27, getGeneratorName(player, this.cachedType, this.level, true));
        } 

        ItemStack infoItem = new ItemStack(this.cachedType.droppedToken().getMaterial(), (this.level <= 0) ? 1 : this.level);
        infoItem.setAmount((this.level <= 0) ? 1 : this.level);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(getGeneratorName(player, this.cachedType, this.level, false));
        infoMeta.setLore(this.getInfoLore(player, this.level, false));
        infoItem.setItemMeta(infoMeta);
        mcInventory.setItem(11, infoItem);
        ItemStack upgradeItem = EggWars.getGeneratorManager().getUpgradeItem().clone();
        upgradeItem.setAmount(this.isMaxLevel() ? 1 : (this.level + 1));
        ItemMeta upgradeMeta = infoItem.getItemMeta();

        if (!this.isMaxLevel())
        {
            upgradeMeta.setDisplayName(TranslationUtils.getMessage("generator.upgrade.name", player, getGeneratorName(player, this.cachedType, this.level + 1, false)));
            upgradeMeta.setLore(this.getInfoLore(player, this.level + 1, true));
        }
        else
        {
            upgradeMeta.setDisplayName(TranslationUtils.getMessage("generator.upgraded_max.name", player));
            upgradeMeta.setLore(Arrays.asList(TranslationUtils.getMessage("generator.upgraded_max.lore", player).split("\n")));
        }

        upgradeItem.setItemMeta(upgradeMeta);
        SerializingItems.UPGRADE_GEN.setItemReference(upgradeItem, null);
        mcInventory.setItem(15, upgradeItem);
        mcInventory.setItem(22, ProfileMenus.getCloseItem(player));
         return mcInventory;
    }

    public List<String> getInfoLore(Player player, int level, boolean upgrade)
    {
        String interval = TranslationUtils.getMessage("generator.info.interval", player, new Object[] { Double.valueOf(this.cachedType.tickRate(level) / 20.0D) });
        String capacity = TranslationUtils.getMessage("generator.info.capacity", player, new Object[] { String.valueOf(this.cachedType.droppedToken().getColor().toString()) + this.cachedType.maxItems(level), this.cachedType.droppedToken().getFormattedName(player) });

        if (upgrade)
        {
            Price price = this.cachedType.getPriceFor(level);
            String cost = TranslationUtils.getMessage("generator.info.cost", player, new Object[] { String.valueOf(price.getToken().getColor().toString()) + price.getAmount(), price.getToken().getFormattedName(player) });
            return Arrays.asList(TranslationUtils.getMessage("generator.upgrade.lore", player, new Object[] { interval, capacity, cost }).split("\n"));
        }

        return Arrays.asList(TranslationUtils.getMessage("generator.info_lore", player, new Object[] { interval, capacity }).split("\n"));
    }


    @Override
    public void clickInventory(InventoryClickEvent clickEvent, MenuPlayer player)
    {
        ItemStack currItem = clickEvent.getCurrentItem();
        SerializingItems type = SerializingItems.getReferenceType(currItem);

        if (SerializingItems.CLOSE_OR_BACK.equals(type))
        {
            clickEvent.setCurrentItem(null);
            this.closeForOpener(player);
            return;
        }

        if (!this.hasCachedType() || !this.arena.getStatus().isGame())
        {
            return;
        }

        if (SerializingItems.UPGRADE_GEN.equals(type))
        {
            this.tryUpgrade(player.getPlayer());
        }
    }

    public static String getGeneratorName(Player player, GeneratorType type, int level, boolean title)
    {
        String lvl = level == 0 ? TranslationUtils.getMessage("generator.info.broken", player) : TranslationUtils.getMessage("generator.info.level", player, level);
        return TranslationUtils.getMessage("generator." + (title ? "title" : "name"), player, TranslationUtils.getMessage(type.droppedToken().getTypeName(), player), lvl, type.droppedToken().getColor());
    }

    @Override
    public int hashCode()
    {
        int i = 1;
        i = 31 * i + (this.block != null ? this.block.hashCode() : 0);
        return i;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (this.getClass() != obj.getClass())
        {
            return false;
        }

        Generator generator = (Generator)obj;

        if (this.block == null)
        {
            if (generator.block != null)
            {
                return false;
            }
        }
        else if (this.block.getBlockX() != generator.block.getBlockX() && this.block.getBlockY() != generator.block.getBlockY() && this.block.getBlockZ() != generator.block.getBlockZ())
        {
            return false;
        }

        if (this.type == null)
        {
            if (generator.type != null)
            {
                return false;
            }
        }
        else if (!this.type.equalsIgnoreCase(generator.type))
        {
            return false;
        }

        if (this.defLevel != generator.defLevel)
        {
            return false;
        }

        return true;
    }

    public class APSS
    {
        public final UUID uuid = UUID.randomUUID();
        public List<EwPlayer> candidates = Lists.newArrayList();
        public int turn = 0;

        //keep update() and nextTurn() separated because update() is used more often to set Turn to 0 when some player candidates in one Gen leave, for the pickup not be disabled for remaining players in slow generators
        public void update()
        {
            this.candidates.clear();

            if (!EggWars.config.enableAPSS)
            {
                return;
            }

            BoundingBox itembox = new BoundingBox(Generator.this.block.getX() - 0.25, Generator.this.block.getY(), Generator.this.block.getZ() - 0.25, Generator.this.block.getX() + 0.25, Generator.this.block.getY() + 0.45, Generator.this.block.getZ() + 0.25);

            for (EwPlayer ewplayer : Generator.this.arena.getAlivePlayers())
            {
                Player player = ewplayer.getPlayer();

                if (player.getBoundingBox().clone().expand(1.0, 0.5, 1.0).overlaps(itembox))
                {
                    this.candidates.add(ewplayer);
                }
            }

            if (this.turn >= this.candidates.size())
            {
                this.turn = 0;
            }
        }

        public void nextTurn()
        {
            if (this.turn < (this.candidates.size() - 1))
            {
                this.turn++;
            }
            else
            {
                this.turn = 0;
            }
        }
    }
}
