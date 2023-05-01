package me.rosillogames.eggwars.arena;

import java.util.Map;
import me.rosillogames.eggwars.objects.Price;
import me.rosillogames.eggwars.objects.Token;

public class GeneratorType
{
    private final Map<Integer, Integer> maxItems;
    private final Map<Integer, Integer> tickRates;
    private final Map<Integer, Price> prices;
    private final String name;
    private final boolean showTimeTag;
    private final boolean requiresNearbyPlayer;
    private final Token droppedToken;
    private final int maxLevel;
    private final int color;

    public GeneratorType(Map<Integer, Integer> maxItems, Map<Integer, Integer> tickRates, Map<Integer, Price> prices, String genId, Token tokenDrop, boolean showTag, boolean playersNear, int maximum, int genColor)
    {
        this.maxItems = maxItems;
        this.tickRates = tickRates;
        this.prices = prices;
        this.name = genId;
        this.showTimeTag = showTag;
        this.requiresNearbyPlayer = playersNear;
        this.droppedToken = tokenDrop;
        this.maxLevel = maximum;
        this.color = genColor;
    }

    public Price getPriceFor(int level)
    {
        return this.prices.get(level);
    }

    public int maxItems(int level)
    {
        if (level == 0)
        {
            return 0;
        }

        return this.maxItems.get(level);
    }

    public int tickRate(int level)
    {
        if (level == 0)
        {
            return 0;
        }

        return this.tickRates.get(level);
    }

    public boolean hasLevel(int level)
    {
        return this.maxItems.containsKey(level) && this.tickRates.containsKey(level) && this.maxItems.containsKey(level);
    }

    public int getMaxLevel()
    {
        return this.maxLevel;
    }

    public int getColor()
    {
        return this.color;
    }

    public String getId()
    {
        return this.name;
    }

    public boolean showTimeTag()
    {
        return this.showTimeTag;
    }

    public boolean requiresNearbyPlayer()
    {
        return this.requiresNearbyPlayer;
    }

    public Token droppedToken()
    {
        return this.droppedToken;
    }

    @Override
    public boolean equals(Object othr)
    {
        if (othr == null || this.getClass() != othr.getClass())
        {
            return false;
        }

        GeneratorType other = (GeneratorType)othr;

        //these values *should* never be null so there is no check
        if (!this.maxItems.equals(other.maxItems) || !this.tickRates.equals(other.tickRates) || !this.prices.equals(other.prices) || !this.name.equalsIgnoreCase(other.name) || this.showTimeTag != other.showTimeTag || this.requiresNearbyPlayer != other.requiresNearbyPlayer || !this.droppedToken.equals(other.droppedToken) || this.maxLevel != other.maxLevel || this.color != other.color)
        {
            return false;
        }

        return true;
    }
}
