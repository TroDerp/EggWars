package me.rosillogames.eggwars.enums;

public enum HealthType
{
    HALF(0, "half"),
    NORMAL(1, "normal"),
    DOUBLE(2, "double"),
    TRIPLE(3, "triple");

    private final int numId;
    private final String namespace;
    private final String nameKey;

    private HealthType(int idIn, String nameIn)
    {
        this.numId = idIn;
        this.nameKey = "voting.health." + nameIn;
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
