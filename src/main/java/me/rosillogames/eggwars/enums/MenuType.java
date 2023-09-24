package me.rosillogames.eggwars.enums;

public enum MenuType
{
    LEAVE_ARENA("leave_arena"),//TODO: remove this one when item identifiers are added
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
    TEAMS_SETUP("teams_setup"),
    SINGLE_TEAM_SETUP("single_team_setup"),
    SELECT_GENERATOR("select_generator"),
    SELECT_GENERATOR_LEVEL("select_generator_level"),
    TEAM_ENDER_CHEST("team_ender_chest");

    private final String nameKey;

    private MenuType(String nameIn)
    {
        this.nameKey = nameIn;
    }

    public String toString()
    {
        return this.nameKey;
    }
}
