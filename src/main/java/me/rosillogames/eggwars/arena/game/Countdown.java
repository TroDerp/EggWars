package me.rosillogames.eggwars.arena.game;

import org.bukkit.Sound;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.language.TranslationUtils;

public class Countdown
{
    private boolean full;
    private int countDown;

    public Countdown(int countDownIn)
    {
        this.countDown = countDownIn;
    }

    public int getCountdown()
    {
        return this.countDown;
    }

    public boolean isFullCountdown()
    {
        return this.full;
    }

    public void setFullCountdown(int fullCD)
    {
        this.full = true;

        if (this.countDown > fullCD)
        {
            this.countDown = fullCD;
        }
    }

    public void setCountdown(int i)
    {
        this.countDown = i;
    }

    public void decrease()
    {
        this.countDown--;
    }

    public static void playCountDownSound(Arena arenaIn)
    {
        arenaIn.broadcastSound(Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
    }

    public static void playCountDownSoundAndSendText(Arena arenaIn, String type, int countdown)
    {
        arenaIn.getPlayers().forEach((ewplayer) ->
        {
            TranslationUtils.sendMessage("gameplay.lobby." + type + "_countdown", ewplayer.getPlayer(), TranslationUtils.translateTime(ewplayer.getPlayer(), countdown, true));
        });
        arenaIn.broadcastSound(Sound.UI_BUTTON_CLICK, 1.0F, 2.0F);
    }
}
