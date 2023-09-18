package me.rosillogames.eggwars.player;

import java.util.Collection;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TempGameData
{
    private final Player player;
    private final ItemStack items[];
    private final ItemStack armor[];
    private final ItemStack extra[];
    private final ItemStack enderChest[];
    private final double health;
    private final double maxHealth;
    private final GameMode gamemode;
    private final Collection effects;
    private final float xp;
    private final int xpLevel;
    private final int foodLevel;
    private final boolean flying;
    private final boolean allowFlight;
    private final Location compassTarget;
    private final String customName;

    public TempGameData(Player playerIn)
    {
        this.player = playerIn;
        this.armor = playerIn.getInventory().getArmorContents().clone();
        this.items = playerIn.getInventory().getContents().clone();
        this.extra = playerIn.getInventory().getExtraContents().clone();
        this.enderChest = playerIn.getEnderChest().getContents().clone();
        this.maxHealth = playerIn.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        this.health = playerIn.getHealth();
        this.foodLevel = playerIn.getFoodLevel();
        this.gamemode = playerIn.getGameMode();
        this.effects = playerIn.getActivePotionEffects();
        this.xp = playerIn.getExp();
        this.xpLevel = playerIn.getLevel();
        this.allowFlight = playerIn.getAllowFlight();
        this.flying = playerIn.isFlying();
        this.compassTarget = playerIn.getCompassTarget();
        this.customName = playerIn.getDisplayName();
    }

    public void sendToPlayer()
    {
        this.player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.maxHealth);
        this.player.setHealth(this.health);
        this.player.setFoodLevel(this.foodLevel);
        this.player.getInventory().setContents(this.items);
        this.player.getInventory().setArmorContents(this.armor);
        this.player.getInventory().setExtraContents(this.extra);
        this.player.getEnderChest().setContents(this.enderChest);
        this.player.updateInventory();
        this.player.setGameMode(this.gamemode);
        this.player.addPotionEffects(this.effects);
        this.player.setLevel(this.xpLevel);
        this.player.setExp(this.xp);
        this.player.setAllowFlight(this.allowFlight);
        this.player.setFlying(this.flying);

        if (this.compassTarget != null)//for some reason compass target CAN be null
        {
            this.player.setCompassTarget(this.compassTarget);
        }

        this.player.setDisplayName(this.customName);
    }
}
