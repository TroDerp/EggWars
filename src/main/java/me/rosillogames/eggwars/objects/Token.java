package me.rosillogames.eggwars.objects;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import me.rosillogames.eggwars.language.TranslationUtils;

public class Token
{
    private final Material material;
    private final ChatColor color;
    private final String name;

    public Token(Material matIn, ChatColor colorIn, String nameIn)
    {
        this.material = matIn;
        this.color = colorIn;
        this.name = nameIn;
    }

    public Material getMaterial()
    {
        return this.material;
    }

    public ChatColor getColor()
    {
        return this.color;
    }

    public String getTypeName()
    {
        return "objects." + this.name;
    }

    public String getFormattedName(Player player)
    {
        return this.color.toString() + TranslationUtils.getMessage(this.getTypeName(), player);
    }

    public String translateToken(Player player, int amount)
    {
        String sLetter = amount <= 1 ? "" : "s";
        return TranslationUtils.getMessage("objects." + this.name + "_token" + sLetter, player);
    }

    @Override
    public String toString()
    {
        return "Token[Material=" + this.material.toString() + ",Color=" + this.color.asBungee().getName() + "]";
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

        Token other = (Token)othr;
        //these values *should* never be null so there is no check
        return this.color.equals(other.color) && this.material == other.material && this.name.equalsIgnoreCase(other.name);
    }
}
