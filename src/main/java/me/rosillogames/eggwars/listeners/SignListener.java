package me.rosillogames.eggwars.listeners;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.arena.GeneratorType;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.ArenaSign;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.utils.LobbySigns;
import me.rosillogames.eggwars.utils.NumericUtils;
import me.rosillogames.eggwars.utils.PlayerUtils;

public class SignListener implements Listener
{
    public SignListener()
    {
        //TODO: remove the update every second all arena signs? it will be necessary to be called from elsewhere
        this.updateClock();
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void write(SignChangeEvent eventIn)
    {
        if (eventIn.getLine(0).equalsIgnoreCase("[EggWars]"))
        {
            if (!eventIn.getPlayer().hasPermission("eggwars.arenaSign.place"))
            {
                return;
            }

            if (EggWars.bungee.isEnabled())
            {
                TranslationUtils.sendMessage("commands.error.bungee_mode", eventIn.getPlayer());
                return;
            }

            Arena arena = EggWars.getArenaManager().getArenaByName(eventIn.getLine(1));

            if (arena == null || EggWars.getArenaManager().getArenaByWorld(eventIn.getBlock().getWorld()) != null)
            {
                return;
            }

            if (LobbySigns.isValidBlockSign(eventIn.getBlock()))
            {//WARNING! updating lines during event doesn't work on 1.20 and after / arena sign uses auto update
                EggWars.signs.add(new ArenaSign(arena, eventIn.getBlock().getLocation()));
                TranslationUtils.sendMessage("setup.sign.arena.added", eventIn.getPlayer());
                EggWars.saveSigns();
            }

            return;
        }

        if (eventIn.getLine(0).equalsIgnoreCase("[EggGen]"))
        {
            if (!eventIn.getPlayer().hasPermission("eggwars.genSign.place"))
            {
                return;
            }

            Arena arena1 = Arena.checkEditArena(eventIn.getPlayer());

            if (arena1 == null)
            {
                return;
            }

            Map<String, GeneratorType> generators = EggWars.getGeneratorManager().getGenerators();

            if (!generators.containsKey(eventIn.getLine(1).toLowerCase()))
            {
                TranslationUtils.sendMessage("commands.error.invalid_gen_type", eventIn.getPlayer());
                return;
            }

            if (!NumericUtils.isInteger(eventIn.getLine(2)) || Integer.parseInt(eventIn.getLine(2)) > generators.get(eventIn.getLine(1).toLowerCase()).getMaxLevel() || Integer.parseInt(eventIn.getLine(2)) < 0)
            {
                TranslationUtils.sendMessage("commands.error.invalid_number", eventIn.getPlayer());
                return;
            }

            Generator generator = new Generator(eventIn.getBlock().getLocation(), Integer.parseInt(eventIn.getLine(2)), eventIn.getLine(1).toLowerCase(), arena1);
            arena1.putGenerator(generator);
            TranslationUtils.sendMessage("setup.generator.added", eventIn.getPlayer());
            generator.reloadCache();

            if (EggWars.serverVersion.ordinal() >= Versions.V_1_20_R1.ordinal())
            {//Updating lines during event doesn't work on 1.20 and after, except if it's the other face
                if (eventIn.getSide() == org.bukkit.block.sign.Side.FRONT)
                {//Get updated block
                    Sign sign = (Sign)eventIn.getBlock().getLocation().getBlock().getState();
                    eventIn.setLine(0, sign.getLine(0));
                    eventIn.setLine(1, sign.getLine(1));
                    eventIn.setLine(2, sign.getLine(2));
                    eventIn.setLine(3, sign.getLine(3));
                }
                else
                {
                    eventIn.setCancelled(true);
                }
            }

            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void click(PlayerInteractEvent event)
    {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getHand() != EquipmentSlot.HAND || event.useInteractedBlock() == Result.DENY)
        {
            return;
        }

        Location clickLoc = event.getClickedBlock().getLocation();
        ArenaSign ewsign = LobbySigns.getSignByLocation(clickLoc, false);
        EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());
        boolean isSign = event.getClickedBlock().getState() instanceof Sign;

        if (ewsign == null)
        {
            Arena arena = EggWars.getArenaManager().getArenaByWorld(clickLoc.getWorld());

            if (arena == null)
            {
                return;
            }

            if (arena.getStatus().equals(ArenaStatus.SETTING))
            {
                if (isSign && arena.getGenerators().containsKey(clickLoc.toVector()))
                {//cancel sign edit for generator signs during setting
                    event.setCancelled(true);
                }

                return;
            }

            if (!arena.getStatus().equals(ArenaStatus.IN_GAME) || ewplayer.isEliminated())
            {
                if (isSign)
                {//cancel sign edit for eliminated players and non-in-game interactions
                    event.setCancelled(true);
                }

                return;
            }

            Vector vector = clickLoc.toVector();

            if (arena.getGenerators().containsKey(vector) || (EggWars.config.useBelowBlock && arena.getGenerators().containsKey(vector = vector.setY(vector.getBlockY() + 1))))
            {
                Generator gen = arena.getGenerators().get(vector);

                if (gen.hasCachedType())
                {
                    if (event.getPlayer().isSneaking())
                    {
                        gen.tryUpgrade(event.getPlayer());
                    }
                    else
                    {
                        gen.addOpener(ewplayer);
                    }
                }//cancel ANY interaction when performing gen action

                event.setCancelled(true);
                return;
            }

            if (isSign && !arena.getReplacedBlocks().containsKey(clickLoc))
            {//cancel sign edit for signs not placed during the game
                event.setCancelled(true);
            }

            return;
        }
        else
        {
            if (!isSign)
            {
                return;
            }//cancel sign edit for arena-joining signs

            event.setCancelled(true);

            if (ewplayer.isInArena() || event.getPlayer().isSneaking())
            {
                return;
            }

            Arena arena1 = ewsign.getArena();

            if (!arena1.isSetup())
            {
                TranslationUtils.sendMessage("commands.error.arena_not_set_up", event.getPlayer(), arena1.getName());
                return;
            }

            if (arena1.getStatus().equals(ArenaStatus.SETTING))
            {
                TranslationUtils.sendMessage("commands.error.arena_in_edit_mode", event.getPlayer());
                return;
            }

            if (arena1.getStatus().isLobby())
            {
                if (arena1.isFull())
                {
                    TranslationUtils.sendMessage("gameplay.lobby.cant_join.full", ewplayer.getPlayer());
                }
                else
                {
                    arena1.joinArena(ewplayer, false, false);
                }
            }
            else if (arena1.getStatus().equals(ArenaStatus.FINISHING) || !EggWars.config.canSpectJoin)
            {
                TranslationUtils.sendMessage("gameplay.lobby.cant_join.ingame", ewplayer.getPlayer());
            }
            else
            {
                arena1.joinArena(ewplayer, false, true);
            }

            return;
        }
    }

    public void updateClock()
    {
        (new BukkitRunnable()
        {
            public void run()
            {
                for (ArenaSign ewsign : EggWars.signs)
                {
                    ewsign.update();
                }
            }
        }).runTaskTimer(EggWars.instance, 0L, 20L);
    }
}
