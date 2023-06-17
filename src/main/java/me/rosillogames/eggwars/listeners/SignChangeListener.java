package me.rosillogames.eggwars.listeners;

import java.util.Map;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Generator;
import me.rosillogames.eggwars.arena.GeneratorType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.ArenaSign;
import me.rosillogames.eggwars.utils.LobbySigns;
import me.rosillogames.eggwars.utils.NumericUtils;

public class SignChangeListener implements Listener
{
    public SignChangeListener()
    {
        //TODO: remove the update every second all arena signs? it will be necessary to be called from elsewhere
        this.updateClock();
    }

    @EventHandler
    public void sign(SignChangeEvent eventIn)
    {
        if (eventIn.getLine(0).equalsIgnoreCase("[EggWars]"))
        {
            if (!eventIn.getPlayer().hasPermission("eggwars.sign.place"))
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

            if (eventIn.getBlock().getState() instanceof Sign && LobbySigns.isValidWallSign((Sign)eventIn.getBlock().getState()))
            {
                EggWars.signs.add(new ArenaSign(arena, eventIn.getBlock().getLocation()));
                TranslationUtils.sendMessage("setup.sign.arena.added", eventIn.getPlayer());
                EggWars.saveSigns();
            }
        }

        if (eventIn.getLine(0).equalsIgnoreCase("[EggGen]"))
        {
            if (!eventIn.getPlayer().hasPermission("eggwars.gen.place"))
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
            generator.updateSign();
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
