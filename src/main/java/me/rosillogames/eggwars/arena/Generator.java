package me.rosillogames.eggwars.arena;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import com.google.common.collect.Lists;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.objects.Token;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.player.inventory.InventoryController;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.Fireworks;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.WorldController;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class Generator
{
    private static final String TAG_PROGRESS_DOT = "|"/* or "¦" */;
    private static final int TAG_PROGRESS_BAR_LENGHT = 30;
    private final Arena arena;
    private final Location block;
    private final int defLevel;
    private final String type;
    @Nullable
    private GeneratorType cachedType = null;
    private int level;
    @Nullable
    private BukkitTask tickTask = null;
    private int genTicks;
    @Nullable
    private ArmorStand armorStand = null;
    private TranslatableInventory genInv;
    //Awesome Produce Sharing System as described by CubeCraft
    //When more than a single player are next to the generator, its product will be given to the player their turn corresponds with.
    private APSS apss = new APSS();

    public Generator(Location loc, int lvl, String typeIn, Arena arenaIn)
    {
        this.block = Locations.toBlock(loc, true);
        this.defLevel = lvl;
        this.level = lvl;
        this.type = typeIn;
        this.arena = arenaIn;
        this.reloadCache();
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
            this.armorStand = (ArmorStand)this.block.getWorld().spawn(this.block.clone().add(0.5, 0.475, 0.5), org.bukkit.entity.ArmorStand.class);
            ReflectionUtils.setArmorStandInvisible(this.armorStand);
            this.armorStand.setAI(false);
            this.armorStand.setMarker(true);
            this.armorStand.teleport(this.block.clone().add(0.5, 0.475, 0.5));
            //Using stringbuilder for optimization
            StringBuilder sb = new StringBuilder("");

            for (int i = 0; i < TAG_PROGRESS_BAR_LENGHT; ++i)
            {
                sb.append(ChatColor.RED + TAG_PROGRESS_DOT);
            }

            this.armorStand.setCustomName(sb.toString());
            this.armorStand.setCustomNameVisible(true);
        }

        final int tickRate = this.cachedType.tickRate(this.level);
        this.tickTask = (new BukkitRunnable()
        {
            public void run()
            {
                if (!Generator.this.cachedType.requiresNearbyPlayer() || PlayerUtils.getNearbyPlayerAmount(Generator.this.block, 3.0, Generator.this.arena) >= 1)
                {
                    Generator.this.genTicks++;
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
                else
                {
                    if (Generator.this.armorStand != null)
                    {
                        StringBuilder sb = new StringBuilder("");

                        for (int i = 0; i < TAG_PROGRESS_BAR_LENGHT; ++i)
                        {
                            sb.append(((((float)Generator.this.genTicks / (float)tickRate) > ((float)i / (float)TAG_PROGRESS_BAR_LENGHT)) ? ChatColor.GREEN : ChatColor.RED) + TAG_PROGRESS_DOT);
                        }

                        Generator.this.armorStand.setCustomName(sb.toString());
                    }
                }
            }
        }).runTaskTimer(EggWars.instance, 0L, this.level != 0 ? 1L : 20L);
        this.genInv = new TranslatableInventory(27, (player) -> getName(player, this.cachedType, this.level, false));
        TranslatableItem tInfoItem = TranslatableItem.translatableNameLore(new ItemStack(this.cachedType.droppedToken().getMaterial(), this.level <= 0 ? 1 : this.level), (player) ->
        {
            Token token = this.cachedType.droppedToken();
            String interval = TranslationUtils.getMessage("generator.info.interval", player, Double.valueOf((double)this.cachedType.tickRate(this.level) / 20.0));
            String capacity = TranslationUtils.getMessage("generator.info.capacity", player, token.getColor().toString() + this.cachedType.maxItems(this.level), token.getFormattedName(player));
            return TranslationUtils.getMessage("generator.info_lore", player, interval, capacity);
        }, (player) -> getName(player, this.cachedType, this.level, true));
        ItemStack upgradeItem = EggWars.getGeneratorManager().getUpgradeItem();
        upgradeItem.setAmount((this.isMaxLevel()) ? 1 : this.level + 1);
        TranslatableItem tUpgradeItem = TranslatableItem.translatableNameLore(upgradeItem, (player) ->
        {
            if (!this.isMaxLevel())
            {
                int nextLevel = this.level + 1;
                Token token = this.cachedType.droppedToken();
                String interval = TranslationUtils.getMessage("generator.info.interval", player, Double.valueOf((double)this.cachedType.tickRate(nextLevel) / 20.0));
                String capacity = TranslationUtils.getMessage("generator.info.capacity", player, token.getColor().toString() + this.cachedType.maxItems(nextLevel), token.getFormattedName(player));
                Price price = this.cachedType.getPriceFor(nextLevel);
                String cost = TranslationUtils.getMessage("generator.info.cost", player, price.getToken().getColor().toString() + price.getAmount(), price.getToken().getFormattedName(player));
                return TranslationUtils.getMessage("generator.upgrade_lore_normal", player, interval, capacity, cost);
            }

            return TranslationUtils.getMessage("generator.upgrade_lore_max", player);
        }, (player) ->
        {
            if (this.isMaxLevel())
            {
                return TranslationUtils.getMessage("generator.upgrade_name_max", player);
            }

            return TranslationUtils.getMessage("generator.upgrade_title", player, getName(player, this.cachedType, this.level + 1, true));
        });

        this.genInv.setItem(11, tInfoItem);
        this.genInv.setItem(15, tUpgradeItem);
        this.genInv.setItem(22, EwPlayerMenu.getCloseItem());
        InventoryController.updateInventories((p) -> p.getArena() == this.arena && p.getInv() != null && p.getInv().getExtraData() instanceof Generator && ((Generator)p.getInv().getExtraData()).getBlock().equals(this.getBlock()), this.genInv, MenuType.GENERATOR_INFO);
    }

    public void stop()
    {
        if (this.armorStand != null)
        {
            this.armorStand.remove();
            this.armorStand = null;
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
        this.genInv = null;
        this.tickTask = null;
        this.level = this.defLevel;
    }

    private void generate()
    {
        if (this.level <= 0 || !this.hasCachedType())
        {
            return;
        }

        ItemStack stack = new ItemStack(this.cachedType.droppedToken().getMaterial(), 1);

        if (ItemUtils.getNearbyItemCount(this.block, 2.5, stack.getType()) >= this.cachedType.maxItems(this.level))
        {
            return;
        }

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

    public void openInventory(Player player)
    {
        InventoryController.openInventory(player, this.genInv, MenuType.GENERATOR_INFO).setExtraData(this);
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
        firework.detonate();
        this.updateSign();
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
                this.level = Math.min(this.level, newType.getMaxLevel());

                //Checks if there was a tick task active before (by checking if it's not null, because it is removed only on arena reset)
                //to know if it has to start generating again
                if (this.tickTask != null)
                {
                    this.start();
                }
            }
            else
            {
                this.genInv = null;
            }
        }
    }

    public static String getName(Player player, GeneratorType type, int level, boolean color)
    {
        String lvl = level == 0 ? TranslationUtils.getMessage("generator.info.broken", player) : TranslationUtils.getMessage("generator.info.level", player, level);
        return TranslationUtils.getMessage("generator.title", player, color ? (ChatColor.YELLOW + "" + ChatColor.BOLD) : "", (color ? type.droppedToken().getColor() + "" + ChatColor.BOLD : "") + TranslationUtils.getMessage(type.droppedToken().getTypeName(), player), lvl);
    }

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
