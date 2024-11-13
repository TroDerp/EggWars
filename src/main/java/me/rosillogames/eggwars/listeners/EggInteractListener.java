package me.rosillogames.eggwars.listeners;

import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import com.google.common.collect.Lists;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.StatType;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.events.EwPlayerRemoveEggEvent;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class EggInteractListener implements Listener
{
    @EventHandler
    public void interact(PlayerInteractEvent event)
    {
        if ((!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.LEFT_CLICK_BLOCK)) || !event.getClickedBlock().getType().equals(Material.DRAGON_EGG))
        {
            return;
        }

        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());

        if (!ewplayer.isInArena())
        {
            Arena arena = EggWars.getArenaManager().getArenaByWorld(ewplayer.getPlayer().getWorld());

            if (arena != null && arena.getStatus() != ArenaStatus.SETTING)
            {
                event.setCancelled(true);
            }

            return;
        }

        if (ewplayer.isEliminated())
        {
            event.setCancelled(true);
            return;
        }

        Arena arena = ewplayer.getArena();
        Team team = TeamUtils.getTeamByEggLocation(arena, event.getClickedBlock().getLocation());

        if (team == null || team.isEliminated())
        {
            return;
        }

        if (team.equals(ewplayer.getTeam()))
        {
            if (event.getPlayer().isSneaking())
            {
                return;
            }

            event.setCancelled(true);

            for (EquipmentSlot hand : new EquipmentSlot[] {EquipmentSlot.HAND, EquipmentSlot.OFF_HAND})
            {
                if (tryPlaceBlockItem(event, hand))
                {
                    ewplayer.getIngameStats().addStat(StatType.BLOCKS_PLACED, 1);
                    return;
                }
            }

            if (event.getHand() == EquipmentSlot.HAND)
            {
                TranslationUtils.sendMessage("gameplay.ingame.cant_destroy_your_egg", ewplayer.getPlayer());
            }

            return;
        }

        try
        {
            EwPlayerRemoveEggEvent removeEggEvent = new EwPlayerRemoveEggEvent(ewplayer, team);
            Bukkit.getPluginManager().callEvent(removeEggEvent);

            if (removeEggEvent.isCancelled())
            {
                return;
            }
        }
        catch (Exception exception)
        {
        }

        event.setCancelled(true);
        ewplayer.getIngameStats().addStat(StatType.EGGS_BROKEN, 1);

        for (EwPlayer player : arena.getPlayers())
        {
            TranslationUtils.sendMessage("gameplay.ingame.team_egg_destroyed", player.getPlayer(), TeamUtils.translateTeamType(team.getType(), player.getPlayer(), false), TeamUtils.colorizePlayerName(ewplayer));
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100F, 1.0F);

            if (player.getTeam() == team)
            {
                ReflectionUtils.sendTitle(player.getPlayer(), 5, 20, 5, "", TranslationUtils.getMessage("gameplay.ingame.your_egg_destroyed", player.getPlayer()));
                TranslationUtils.sendMessage("gameplay.ingame.your_egg_destroyed", player.getPlayer());
            }
        }

        event.getClickedBlock().setType(Material.AIR);
        PlayerUtils.addPoints(ewplayer, EggWars.instance.getConfig().getInt("game.points.on_egg"));
        arena.getScores().updateScores(false);
    }

    /* There's an unfixable bug that causes main-hand to swing instead of off-hand when placing block from the other hand */
    private static boolean tryPlaceBlockItem(PlayerInteractEvent event, EquipmentSlot hand)
    {
        ItemStack copy = event.getPlayer().getInventory().getItem(hand).clone();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !copy.getType().isBlock() || copy.getType().isAir())
        {
            return false;
        }

        Block placing = event.getClickedBlock().getRelative(event.getBlockFace());

        if (!placing.isEmpty() || (EggWars.serverVersion.ordinal() >= Versions.V_1_18_R1.ordinal() && !placing.canPlace(copy.getType().createBlockData())))
        {//Should add support for replaceable blocks (not possible due to Spigot API)
            return false;
        }

        BlockState replacedBS = placing.getState();
        BlockData data = copy.getType().createBlockData();

        if (data instanceof Directional && ((Directional)data).getFaces().contains(event.getBlockFace()))
        {
            ((Directional)data).setFacing(event.getBlockFace());
        }

        placing.setBlockData(data);
        Collection<BoundingBox> boxes = Lists.newArrayList();

        if (EggWars.serverVersion.ordinal() >= Versions.V_1_17.ordinal())
        {
            boxes.addAll(placing.getCollisionShape().getBoundingBoxes());
        }
        else if (!placing.isPassable())
        {
            boxes.add(new BoundingBox(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D));
        }

        for (BoundingBox box : boxes)
        {//should check for entities with blocksBuilding=true, but cannot due to it being an NMS field, should implement more NMS?
            if (!placing.getWorld().getNearbyEntities(box.shift(placing.getLocation()), entity -> entity instanceof LivingEntity).isEmpty())
            {
                replacedBS.update(true);
                return false;
            }
        }

        BlockPlaceEvent placeEvent = new BlockPlaceEvent(placing, replacedBS, event.getClickedBlock(), copy, event.getPlayer(), true, hand);
        Bukkit.getPluginManager().callEvent(placeEvent);

        if (!placeEvent.isCancelled())
        {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
            {
                int newAmount = copy.getAmount() - 1;

                if (newAmount <= 0)
                {
                    event.getPlayer().getInventory().setItem(hand, null);
                }
                else
                {
                    event.getPlayer().getInventory().getItem(hand).setAmount(newAmount);
                }
            }

            event.setUseItemInHand(Result.ALLOW);
            SoundGroup sound = placing.getBlockData().getSoundGroup();
            placing.getWorld().playSound(placing.getLocation(), sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            return true;
        }
        else
        {
            placing.setType(replacedBS.getType());
            return false;
        }
    }
}
