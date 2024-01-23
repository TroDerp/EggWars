package me.rosillogames.eggwars.enums;

import javax.annotation.Nullable;

public enum MenuType
{
    LEAVE_ARENA("leave_arena"),//TODO: remove this one if item identifiers get added
    TEAM_SELECTION("team_selection"),
    VOTING("voting"),
    ITEM_VOTING("item_voting"),
    HEALTH_VOTING("health_voting"),
    VILLAGER_MENU("villager_menu"),
    VILLAGER_TRADING("villager_trading"),
    KIT_SELECTION("kit_selection"),
    GENERATOR_INFO("generator_info"),
    MENU("menu"),
    STATS("stats"),
    SETTINGS("settings"),
    LANGUAGES("languages"),
    ARENA_SETUP("arena_setup"),
    BASIC_SETTINGS("basic_settings"),
    SETUP_TEAMS("setup_teams"),
    SETUP_SINGLE_TEAM("setup_single_team"),
    SELECT_GENERATOR("select_generator"),
    SELECT_GENERATOR_LEVEL("select_generator_level"),
    TEAM_ENDER_CHEST("team_ender_chest");

    private final String name;

    private MenuType(String nameIn)
    {
        this.name = nameIn;
    }

    @Nullable
    public static MenuType parse(String s)
    {
        for (MenuType type : values())
        {
            if (type.name.equalsIgnoreCase(s))
            {
                return type;
            }
        }

        return null;
    }

    public static boolean isSetupMenu(MenuType type)
    {
        return type == MenuType.ARENA_SETUP || type == MenuType.BASIC_SETTINGS || type == MenuType.SETUP_TEAMS || type == MenuType.SETUP_SINGLE_TEAM;
    }

    public String toString()
    {
        return this.name;
    }
}
