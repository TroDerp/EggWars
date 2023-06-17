package me.rosillogames.eggwars.enums;

import org.bukkit.ChatColor;

public enum TeamType
{
    RED("red", 14, ChatColor.DARK_RED),
    DARK_BLUE("dark_blue", 11, ChatColor.DARK_BLUE),
    GREEN("green", 5, ChatColor.GREEN),
    YELLOW("yellow", 4, ChatColor.YELLOW),
    PURPLE("purple", 2, ChatColor.DARK_PURPLE),
    ORANGE("orange", 1, ChatColor.GOLD),
    DARK_GREEN("dark_green", 13, ChatColor.DARK_GREEN),
    LIGHT_BLUE("light_blue", 3, ChatColor.AQUA),
    PINK("pink", 6, ChatColor.LIGHT_PURPLE),
    GRAY("gray", 8, ChatColor.GRAY),
    CYAN("cyan", 9, ChatColor.DARK_AQUA),
    BLACK("black", 15, ChatColor.DARK_GRAY),
    WHITE("white", 0, ChatColor.WHITE);

    private final String id;
    private final int woolColor;
    private final ChatColor chatColor;

    private TeamType(String s, int wColor, ChatColor color)
    {
        this.id = s;
        this.woolColor = wColor;
        this.chatColor = color;
    }

    public String id()
    {
        return this.id;
    }

    public int woolColor()
    {
        return this.woolColor;
    }

    public ChatColor color()
    {
        return this.chatColor;
    }

    public TeamType next()
    {
        if (this.equals(RED))
        {
            return DARK_BLUE;
        }

        if (this.equals(DARK_BLUE))
        {
            return GREEN;
        }

        if (this.equals(GREEN))
        {
            return YELLOW;
        }

        if (this.equals(YELLOW))
        {
            return PURPLE;
        }

        if (this.equals(PURPLE))
        {
            return ORANGE;
        }

        if (this.equals(ORANGE))
        {
            return DARK_GREEN;
        }

        if (this.equals(DARK_GREEN))
        {
            return LIGHT_BLUE;
        }

        if (this.equals(LIGHT_BLUE))
        {
            return PINK;
        }

        if (this.equals(PINK))
        {
            return GRAY;
        }

        if (this.equals(GRAY))
        {
            return CYAN;
        }

        if (this.equals(CYAN))
        {
            return BLACK;
        }

        if (this.equals(BLACK))
        {
            return WHITE;
        }

        return RED;
    }

    public static TeamType byId(String s) throws IllegalArgumentException
    {
        for (TeamType teams : values())
        {
            if (teams.id().equals(s))
            {
                return teams;
            }
        }

        throw new IllegalArgumentException("Invalid team type: \"" + s + "\"");
    }
}
