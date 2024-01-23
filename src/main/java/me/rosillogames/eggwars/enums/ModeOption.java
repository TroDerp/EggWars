package me.rosillogames.eggwars.enums;

public enum ModeOption
{
    NONE("never"),
    SOLO("when_solo"),
    TEAM("when_team"),
    ALL("always");

    private final String id;

    private ModeOption(String nameIn)
    {
        this.id = nameIn;
    }

    public boolean applies(Mode mode)
    {
        return this == ALL || (mode == Mode.SOLO && this == SOLO) || (mode == Mode.TEAM && this == TEAM);
    }

    public String toString()
    {
        return this.id;
    }

    public static ModeOption getOrDefault(String s, ModeOption def)
    {
        for (ModeOption option : values())
        {
            if (option.toString().equals(s))
            {
                return option;
            }
        }

        //EggWars.instance.getLogger().log(Level.WARNING, "Couldn't parse ModeOption \"" + s + "\", valid values are \"none\", \"solo\", \"teams\" and \"all\". Now using fallback value instead (\"" + def.toString() + "\")");
        return def;
    }
}
