package me.rosillogames.eggwars.enums;

public enum ItemType
{
    HARDCORE(0, "hardcore"),
    NORMAL(1, "normal"),
    OVERPOWERED(2, "overpowered");

    private final int numId;
    private final String namespace;
    private final String nameKey;

    private ItemType(int idIn, String nameIn)
    {
        this.numId = idIn;
        this.nameKey = "voting.items." + nameIn;
        this.namespace = nameIn;
    }

    public String getNameKey()
    {
        return this.nameKey;
    }

    public int getNumericalId()
    {
        return this.numId;
    }

    @Override
    public String toString()
    {
        return this.namespace;
    }
}
