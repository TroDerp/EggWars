package me.rosillogames.eggwars.objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.google.gson.JsonObject;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.utils.GsonHelper;
import me.rosillogames.eggwars.utils.ItemUtils;

public class Price
{
    private final Token token;
    private final int amount;

    public Price(Token tokenIn, int i)
    {
        this.token = tokenIn;
        this.amount = i;
    }

    public Material getMaterial()
    {
        return this.token.getMaterial();
    }

    public Token getToken()
    {
        return this.token;
    }

    /** Amount of items required from the price **/
    public int getAmount()
    {
        return this.amount;
    }

    public String getReqName(Player player)
    {
        return this.token.translateToken(player, this.amount);
    }

    @Override
    public String toString()
    {
        return "Price[Token=" + this.token.toString() + ",Amount=" + this.amount + "]";
    }

    public boolean equals(Object othr)
    {
        if (this == othr)
        {
            return true;
        }

        if (othr == null || this.getClass() != othr.getClass())
        {
            return false;
        }

        Price other = (Price)othr;
        //these values *should* never be null so there is no check
        return this.amount == other.amount && this.token.equals(other.token);
    }

    public static Price parse(JsonObject pricejson)
    {
        return new Price(EggWars.getTokenManager().byName(GsonHelper.getAsString(pricejson, "token")), GsonHelper.getAsInt(pricejson, "amount", 1));
    }

    public static boolean canAfford(Player player, Price price)
    {//don't use inventory.contains(item, amount), use this instead
        return ItemUtils.countItems(player, price.getMaterial()) >= price.amount;
    }

    public static void sellItems(Player player, Price price)
    {
        ItemUtils.removeItems(player, price.getMaterial(), price.amount);
    }

    public static int amountOf(Price price, Player player)
    {
        return ItemUtils.countItems(player, price.getMaterial());
    }

    public static int leftFor(Price price, Player player)
    {
        return price.getAmount() - ItemUtils.countItems(player, price.getMaterial());
    }
}
