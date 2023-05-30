package me.rosillogames.eggwars.enums;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.bukkit.command.CommandSender;
import me.rosillogames.eggwars.EggWars;

public enum ReloadType
{
    ALL("all", sender ->
    {
        EggWars.config.loadConfig();
        EggWars.languageManager().loadLangs();
        EggWars.getKitManager().loadKits();
        EggWars.getTokenManager().loadTokens();
        EggWars.getGeneratorManager().loadGenerators();
        EggWars.getTradingManager().loadTrades();
    }),
    CONFIG("config", sender ->
    {
        EggWars.config.loadConfig();
    }),
    LANGUAGES("languages", sender ->
    {
        EggWars.languageManager().loadLangs();
    }),
    KITS("kits", sender ->
    {
        EggWars.getKitManager().loadKits();
    }),
    GENERATORS("generators", sender ->
    {
        EggWars.getTokenManager().loadTokens();
        EggWars.getGeneratorManager().loadGenerators();
    }),
    TRADES("trades", sender ->
    {
        EggWars.getTokenManager().loadTokens();
        EggWars.getTradingManager().loadTrades();
    });

    private final String nameKey;
    private final Consumer<CommandSender> onReload;

    private ReloadType(String nameIn, Consumer<CommandSender> onReloadIn)
    {
        this.nameKey = nameIn;
        this.onReload = onReloadIn;
    }

    public void reload(CommandSender sender)
    {
        this.onReload.accept(sender);
    }

    public String getNameKey()
    {
        return this.nameKey;
    }

    @Nullable
    public static ReloadType parse(String s)
    {
        for (ReloadType type : values())
        {
            if (type.getNameKey().equalsIgnoreCase(s))
            {
                return type;
            }
        }

        return null;
    }
}
