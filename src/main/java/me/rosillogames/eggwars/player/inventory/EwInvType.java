package me.rosillogames.eggwars.player.inventory;

import java.util.List;
import com.google.common.collect.Lists;

public class EwInvType
{
    private static final List<EwInvType> INVENTORIES = Lists.<EwInvType>newArrayList();
    public static final EwInvType TEAM_SELECTION = new EwInvType("team_selection");
    public static final EwInvType VOTING = new EwInvType("voting");
    public static final EwInvType ITEM_VOTING = new EwInvType("item_voting");
    public static final EwInvType HEALTH_VOTING = new EwInvType("health_voting");
    public static final EwInvType VILLAGER_MENU = new EwInvType("villager_menu");
    public static final EwInvType VILLAGER_TRADING = new EwInvType("villager_trading");
    public static final EwInvType KIT_SELECTION = new EwInvType("kit_selection");
    public static final EwInvType GENERATOR_INFO = new EwInvType("generator_info");
    public static final EwInvType MENU = new EwInvType("menu");
    public static final EwInvType STATS = new EwInvType("stats");
    public static final EwInvType SETTINGS = new EwInvType("settings");
    public static final EwInvType LANGUAGES = new EwInvType("languages");
    public static final EwInvType ARENA_SETUP = new EwInvType("arena_setup");
    public static final EwInvType MAIN_SETUP = new EwInvType("main_setup");
    public static final EwInvType TEAMS_SETUP = new EwInvType("teams_setup");
    public static final EwInvType SINGLE_TEAM_SETUP = new EwInvType("single_team_setup");
    public static final EwInvType GENERATORS_SETUP = new EwInvType("generators_setup");
    public static final EwInvType GENERATOR_LEVELS_SETUP = new EwInvType("generator_levels_setup");

    private final String nameSpace;

    private EwInvType(String nameIn)
    {
        this.nameSpace = nameIn;
        INVENTORIES.add(this);
    }

    public String toString()
    {
        return this.nameSpace;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (other == null)
        {
            return false;
        }

        if (this.getClass() != other.getClass())
        {
            return false;
        }

        return this.nameSpace.equals(((EwInvType)other).nameSpace);
    }
}
