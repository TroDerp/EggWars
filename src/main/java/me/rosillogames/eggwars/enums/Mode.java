package me.rosillogames.eggwars.enums;

public enum Mode
{
    SOLO("Solo"),
    TEAM("Team");

    private final String name;

    private Mode(String s)
    {
        this.name = s;
    }

    public boolean isSolo()
    {
        return this.equals(SOLO);
    }

    public boolean isTeam()
    {
        return this.equals(TEAM);
    }

    public String getName()
    {
        return this.name;
    }
}
