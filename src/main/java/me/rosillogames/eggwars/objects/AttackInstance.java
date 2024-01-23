package me.rosillogames.eggwars.objects;

import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.player.EwPlayer;

public class AttackInstance
{
    private final EwPlayer attacker;
    private final TeamType color;//cache because attacker may leave the arena, to prevent a bug with death message
    private final Cooldown remain;
    private float damage;

    public AttackInstance(EwPlayer attacker, Cooldown remain, float damage)
    {
        this.attacker = attacker;
        this.color = attacker.getTeam().getType();
        this.remain = remain;
        this.damage = damage;
    }

    public EwPlayer getAttacker()
    {
        return this.attacker;
    }

    public boolean hasExpired()
    {
        return this.remain.hasFinished();
    }

    public float getDamage()
    {
        return this.damage;
    }

    public void setDamage(float dmg)
    {
        this.damage = dmg;
    }

    public TeamType getTeamColor()
    {
        return this.color;
    }
}
