package me.rosillogames.eggwars.enums;

public enum StatType
{
    WINS(true),
    KILLS(true),
    DEATHS(false),
    EGGS_BROKEN(false),
    GAMES_PLAYED(false),
    ELIMINATIONS(true),
    BLOCKS_PLACED(false),
    BLOCKS_BROKEN(false),
    BLOCKS_WALKED(false),
    TIME_PLAYED(false); //IN SECONDS
    private boolean forTops;

    private StatType(boolean flag)
    {
        this.forTops = flag;
    }
}
