package me.rosillogames.eggwars.objects;

public class Cooldown
{
    private long finishMillis = -1L;

    public void setFinish(int time)
    {
        this.finishMillis = System.currentTimeMillis() + (long)(time * 1000);
    }

    public boolean hasFinished()
    {
        return System.currentTimeMillis() > this.finishMillis;
    }

    public int timeUntilFinish()
    {
        return (int)(this.finishMillis - System.currentTimeMillis()) / 1000;
    }

    public void clear()
    {
        this.finishMillis = -1L;
    }
}
