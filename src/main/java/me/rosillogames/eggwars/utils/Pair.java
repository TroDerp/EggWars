package me.rosillogames.eggwars.utils;

public class Pair<L, R>
{
    private final L left;
    private final R right;

    public Pair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }

    public L getLeft()
    {
        return this.left;
    }

    public R getRight()
    {
        return this.right;
    }

    public Pair<R, L> swap()
    {
        return new Pair(this.right, this.left);
    }

    @Override
    public String toString()
    {
        return "(" + this.left.toString() + ", " + this.right.toString() + ")";
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass())
        {
            return false;
        }

        return this.left.equals(((Pair)obj).left) && this.right.equals(((Pair)obj).right);
    }
}
