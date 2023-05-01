package me.rosillogames.eggwars.enums;

public enum ArenaStatus
{
	LOBBY("lobby"),
	STARTING("starting"),
	STARTING_GAME("starting_game"),
	SETTING("setting"),
	IN_GAME("in_game"),
	FINISHING("finishing");
	private final String namespace;

    private ArenaStatus(String s)
    {
        this.namespace = s;
    }

    public boolean isLobby()
    {
        return this.equals(LOBBY) || this.equals(STARTING);
    }

    public boolean isGame()
    {
        return this.equals(STARTING_GAME) || this.equals(IN_GAME);
    }

    @Override
    public String toString()
    {
        return this.namespace;
    }
}
