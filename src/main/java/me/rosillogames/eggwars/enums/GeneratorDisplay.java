package me.rosillogames.eggwars.enums;

public enum GeneratorDisplay
{
    SIGNS("signs"),
    ITEM_FRAMES("item_frames");

    private final String id;

    private GeneratorDisplay(String nameIn)
    {
        this.id = nameIn;
    }

    public String toString()
    {
        return this.id;
    }

    public static GeneratorDisplay getOrDefault(String s, GeneratorDisplay def)
    {
        for (GeneratorDisplay option : values())
        {
            if (option.toString().equals(s))
            {
                return option;
            }
        }

        return def;
    }
}
